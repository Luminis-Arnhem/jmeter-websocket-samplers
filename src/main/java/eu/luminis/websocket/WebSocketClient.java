/*
 * Copyright 2016, 2017 Peter Doornbosch
 *
 * This file is part of JMeter-WebSocket-Samplers, a JMeter add-on for load-testing WebSocket applications.
 *
 * JMeter-WebSocket-Samplers is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your option)
 * any later version.
 *
 * JMeter-WebSocket-Samplers is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package eu.luminis.websocket;

import org.apache.jmeter.util.JsseSSLManager;
import org.apache.jmeter.util.SSLManager;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.*;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Collections;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WebSocketClient {

    private static final Logger log = LoggingManager.getLoggerForClass();

    public static int DEFAULT_CONNECT_TIMEOUT = 20 * 1000;
    public static int DEFAULT_READ_TIMEOUT = 6 * 1000;

    enum WebSocketState {
        CLOSED,
        CLOSING,
        CONNECTED,
        CONNECTING
    }

    private final URL connectUrl;
    private Socket wsSocket;
    private InputStream socketInputStream;
    private OutputStream socketOutputStream;
    private Random randomGenerator = new Random();
    private volatile WebSocketState state = WebSocketState.CLOSED;
    private Map<String, String> additionalHeaders;
    private boolean useProxy;
    private String proxyHost;
    private int proxyPort;

    public WebSocketClient(URL wsURL) {
        connectUrl = correctUrl(wsURL);
    }

    public URL getConnectUrl() {
        return connectUrl;
    }

    public void setAdditionalUpgradeRequestHeaders(Map<String, String> additionalHeaders) {
        this.additionalHeaders = additionalHeaders;
    }

    public void useProxy(String host, int port) {
        useProxy = true;
        proxyHost = host;
        proxyPort = port;
    }

    public Map<String, String>  connect() throws IOException, HttpException {
        return connect(Collections.EMPTY_MAP, DEFAULT_CONNECT_TIMEOUT, DEFAULT_READ_TIMEOUT);
    }

    public Map<String, String> connect(int connectTimeout, int readTimeout) throws IOException, HttpException {
        return connect(Collections.EMPTY_MAP, connectTimeout, readTimeout);
    }

    public Map<String, String> connect(Map<String, String> headers) throws IOException, HttpException {
        if (additionalHeaders != null && ! additionalHeaders.isEmpty() && headers != null && !headers.isEmpty())
            throw new IllegalArgumentException("Cannot pass headers when setAdditionalUpgradeRequestHeaders is called before");
        return connect(headers, DEFAULT_CONNECT_TIMEOUT, DEFAULT_READ_TIMEOUT);
    }

    public Map<String, String> connect(Map<String, String> headers, int connectTimeout, int readTimeout) throws IOException, HttpException {
        if (headers != null && ! headers.isEmpty()) {
            if (additionalHeaders != null && !additionalHeaders.isEmpty())
                throw new IllegalArgumentException("Cannot pass headers when setAdditionalUpgradeRequestHeaders is called before");
        }
        else {
            headers = additionalHeaders;
        }
        if (headers == null) {
            headers = Collections.EMPTY_MAP;
        }

        if (state != WebSocketState.CLOSED) {
            throw new IllegalStateException("Cannot connect when state is " + state);
        }
        state = WebSocketState.CONNECTING;

        boolean connected = false;
        wsSocket = createSocket(connectUrl.getHost(), connectUrl.getPort(), connectTimeout, readTimeout);
        Map<String, String> responseHeaders = null;

        try {
            wsSocket.setSoTimeout(readTimeout);
            socketOutputStream = wsSocket.getOutputStream();

            String path = connectUrl.getFile();  // getFile includes path and query string
            if (path == null || !path.trim().startsWith("/"))
                path = "/" + path;
            PrintWriter httpWriter = new PrintWriter(socketOutputStream);
            httpWriter.print("GET " + (useProxy? connectUrl.toString(): path) + " HTTP/1.1\r\n");
            httpWriter.print("Host: " + connectUrl.getHost() + "\r\n");
            for (Map.Entry<String, String> header : headers.entrySet()) {
                String headerLine = header.getKey() + ": " + header.getValue();
                // Ensure header line does _not_ contain new line
                if (!headerLine.contains("\r") && !headerLine.contains("\n"))
                    httpWriter.print(headerLine + "\r\n");
                else {
                    throw new IllegalArgumentException("Invalid header; contains new line.");
                }
            }
            httpWriter.print("Upgrade: websocket\r\n");
            httpWriter.print("Connection: Upgrade\r\n");
            byte[] nonce = new byte[16];
            randomGenerator.nextBytes(nonce);
            String encodeNonce = new String(Base64.getEncoder().encode(nonce));
            httpWriter.print("Sec-WebSocket-Key: " + encodeNonce + "\r\n");
            httpWriter.print("Sec-WebSocket-Version: 13\r\n");
            httpWriter.print("\r\n");
            httpWriter.flush();

            socketInputStream = wsSocket.getInputStream();
            responseHeaders = checkServerResponse(socketInputStream, encodeNonce);
            connected = true;
            state = WebSocketState.CONNECTED;
        }
        finally {
            if (! connected) {
                if (socketInputStream != null)
                    socketInputStream.close();
                if (socketOutputStream != null)
                    socketOutputStream.close();
                if (wsSocket != null)
                    wsSocket.close();
                state = WebSocketState.CLOSED;
            }
        }
        return responseHeaders;
    }

    public boolean isConnected() {
        return state == WebSocketState.CONNECTED;
    }


    private Socket createSocket(String host, int port, int connectTimeout, int readTimeout) throws IOException {
        Socket baseSocket = new Socket();
        if (useProxy) {
            baseSocket.connect(new InetSocketAddress(proxyHost, proxyPort), connectTimeout);
            setupProxyConnection(baseSocket);
        }
        else
            baseSocket.connect(new InetSocketAddress(host, port), connectTimeout);

        if ("https".equals(connectUrl.getProtocol())) {
            baseSocket.setSoTimeout(readTimeout);

            JsseSSLManager sslMgr = (JsseSSLManager) SSLManager.getInstance();
            try {
                SSLSocketFactory tlsSocketFactory = sslMgr.getContext().getSocketFactory();
                return tlsSocketFactory.createSocket(baseSocket, host, port, true);
            } catch (GeneralSecurityException e) {
                throw new IOException(e);
            }
        }
        else {
            return baseSocket;
        }
    }

    private void setupProxyConnection(Socket socket) throws IOException {
        PrintWriter proxyWriter = new PrintWriter(socket.getOutputStream());
        proxyWriter.print("CONNECT " + connectUrl.getHost() + ":" + connectUrl.getPort() + " HTTP/1.1\r\n");
        proxyWriter.print("Host: " + connectUrl.getHost() + "\r\n");
        proxyWriter.print("\r\n");
        proxyWriter.flush();

        try {
            HttpLineReader httpReader = new HttpLineReader(socket.getInputStream());
            String statusLine = httpReader.readLine();
            checkHttpStatus(statusLine, 200);

            String line;
            do {
                line = httpReader.readLine();
            }
            while (line != null && line.trim().length() > 0);  // HTTP response ends with an empty line.
            log.debug("Using proxy " + proxyHost + ":" + proxyPort + " for " + connectUrl);
        }
        catch (HttpUpgradeException httpError) {
            log.error("Proxy connection error", httpError);
            throw new HttpProtocolException("Connecting proxy failed with status code " + httpError.getStatusCode());
        }
        catch (SocketTimeoutException timeout) {
            log.error("Proxy connection timeout");
            throw timeout;
        }
        catch (IOException ioError) {
            log.error("Proxy connection setup error: ", ioError);
            throw ioError;
        }
    }

    public void dispose() {
        try {
            if (socketInputStream != null)
                socketInputStream.close();
            if (socketOutputStream != null)
                socketOutputStream.close();
            if (wsSocket != null)
                wsSocket.close();
            state = WebSocketState.CLOSED;
        } catch (IOException e) {}
    }

    /**
     * Close the websocket connection properly, i.e. send a close frame and wait for a close confirm.
     */
    public CloseFrame close(int status, String requestData, int readTimeout) throws IOException, UnexpectedFrameException {
        if (state != WebSocketState.CONNECTED) {
            throw new IllegalStateException("Cannot close when state is " + state);
        }
        state = WebSocketState.CLOSED;
        socketOutputStream.write(new CloseFrame(status, requestData).getFrameBytes());
        return receiveClose(readTimeout);
    }

    public CloseFrame receiveClose(int timeout) throws IOException, UnexpectedFrameException {

        wsSocket.setSoTimeout(timeout);

        Frame frame = Frame.parseFrame(socketInputStream);
        if (frame.isClose()) {
            state = WebSocketState.CLOSED;
            return (CloseFrame) frame;
        }
        else
            throw new UnexpectedFrameException(frame);
    }

    public void sendTextFrame(String requestData) throws IOException {
        if (state != WebSocketState.CONNECTED) {
            throw new IllegalStateException("Cannot send data frame when state is " + state);
        }

        socketOutputStream.write(new TextFrame(requestData).getFrameBytes());
    }

    public void sendBinaryFrame(byte[] requestData) throws IOException {
        if (state != WebSocketState.CONNECTED) {
            throw new IllegalStateException("Cannot send data frame when state is " + state);
        }

        socketOutputStream.write(new BinaryFrame(requestData).getFrameBytes());
    }

    public void sendPingFrame() throws IOException {
        sendPingFrame(new byte[0]);
    }

    public void sendPingFrame(byte[] requestData) throws IOException {
        if (state != WebSocketState.CONNECTED) {
            throw new IllegalStateException("Cannot send data frame when state is " + state);
        }

        socketOutputStream.write(new PingFrame(requestData).getFrameBytes());
    }

    public String receiveText(int timeout) throws IOException, UnexpectedFrameException {
        if (state != WebSocketState.CONNECTED) {
            throw new IllegalStateException("Cannot receive data frame when state is " + state);
        }

        wsSocket.setSoTimeout(timeout);

        Frame frame = Frame.parseFrame(socketInputStream);
        if (frame.isText())
            return ((TextFrame) frame).getText();
        else
            throw new UnexpectedFrameException(frame);
    }

    public byte[] receiveBinaryData(int timeout) throws IOException, UnexpectedFrameException {
        if (state != WebSocketState.CONNECTED) {
            throw new IllegalStateException("Cannot receive data frame when state is " + state);
        }

        wsSocket.setSoTimeout(timeout);

        Frame frame = Frame.parseFrame(socketInputStream);
        if (frame.isBinary())
            return ((BinaryFrame) frame).getData();
        else
            throw new UnexpectedFrameException(frame);
    }

    public byte[] receivePong(int timeout) throws IOException, UnexpectedFrameException {
        if (state != WebSocketState.CONNECTED) {
            throw new IllegalStateException("Cannot receive data frame when state is " + state);
        }

        wsSocket.setSoTimeout(timeout);

        Frame frame = Frame.parseFrame(socketInputStream);
        if (frame.isPong())
            return ((PongFrame) frame).getData();
        else
            throw new UnexpectedFrameException(frame);
    }

    protected Map<String, String> checkServerResponse(InputStream inputStream, String nonce) throws IOException {
        HttpLineReader httpReader = new HttpLineReader(inputStream);
        String line = httpReader.readLine();
        if (line != null)
            checkHttpStatus(line, 101);
        else
            throw new HttpProtocolException("Empty response; connection closed.");

        Map<String, String> serverHeaders = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);  // Field names of Http Headers are case-insensitive, see https://tools.ietf.org/html/rfc2616#section-4.2
        do {
            line = httpReader.readLine();
            if (line != null) {
                String[] values = line.split(":", 2);
                if (values.length > 1) {
                    String key = values[0];
                    String value = values[1].trim();
                    if (serverHeaders.containsKey(key))
                        serverHeaders.put(key, serverHeaders.get(key) + ", " + value);
                    else
                        serverHeaders.put(key, value);
                }
            }
        }
        while (line != null && line.trim().length() > 0);  // HTTP response ends with an empty line.

        // Check server response for mandatory headers
        if (! "websocket".equals(getLowerCase(serverHeaders.get("Upgrade"))))  // According to RFC 6455 section 4.1, client must match case-insensative
            throw new HttpUpgradeException("Server response should contain 'Upgrade' header with value 'websocket'");
        if (! "upgrade".equals(getLowerCase(serverHeaders.get("Connection"))))  // According to RFC 6455 section 4.1, client must match case-insensative
            throw new HttpUpgradeException("Server response should contain 'Connection' header with value 'Upgrade'");
        if (! serverHeaders.containsKey("Sec-WebSocket-Accept"))
            throw new HttpUpgradeException("Server response should contain 'Sec-WebSocket-Accept' header");

        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA-1");
            String expectedAcceptHeaderValue = new String(Base64.getEncoder().encode(md.digest((nonce + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11").getBytes())));
            if (! serverHeaders.get("Sec-WebSocket-Accept").equals(expectedAcceptHeaderValue))
                throw new HttpUpgradeException("Server response header 'Sec-WebSocket-Accept' has incorrect value.");
        }
        catch (NoSuchAlgorithmException e) {
            // Impossible
        }
        // If it gets here, server response is ok.
        return serverHeaders;
    }

    private void checkHttpStatus(String statusLine, int expectedStatusCode) throws HttpException {
        Matcher matcher = Pattern.compile("^HTTP/\\d\\.\\d (\\d{3}?)").matcher(statusLine);
        if (matcher.find()) {
            int statusCode = Integer.parseInt(matcher.group(1));
            if (statusCode != expectedStatusCode)
                throw new HttpUpgradeException(statusCode);
        }
        else
            throw new HttpProtocolException("Invalid status line");
    }

    private String getLowerCase(String value) {
        if (value != null)
            return value.toLowerCase();
        else return null;
    }

    private URL correctUrl(URL wsURL) {
        if (wsURL.getPath().startsWith("/"))
            return wsURL;
        else
            try {
                String path = wsURL.getPath();
                if (!path.trim().startsWith("/"))
                    path = "/" + path.trim();
                return new URL(wsURL.getProtocol(), wsURL.getHost(), wsURL.getPort(), path);
            } catch (MalformedURLException e) {
                // Impossible
                throw new RuntimeException();
            }
    }

}



