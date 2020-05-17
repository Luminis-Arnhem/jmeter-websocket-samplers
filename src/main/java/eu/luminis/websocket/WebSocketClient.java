/*
 * Copyright © 2016, 2017, 2018 Peter Doornbosch
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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.SSLSocketFactory;

import org.apache.commons.io.IOUtils;
import org.apache.jmeter.util.JsseSSLManager;
import org.apache.jmeter.util.SSLManager;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

import eu.luminis.websocket.Frame.DataFrameType;


public class WebSocketClient {

    private static final Logger log = LoggingManager.getLoggerForClass();
    private static final String NEW_LINE = "\r\n";
    private static final Pattern HTTP_STATUS_PATTERN = Pattern.compile("^HTTP/\\d\\.\\d (\\d{3}?)");
    public static final int DEFAULT_CONNECT_TIMEOUT = 20 * 1000;
    public static final int DEFAULT_READ_TIMEOUT = 6 * 1000;
    public static Set<String> UPGRADE_HEADERS;

    static {
        UPGRADE_HEADERS = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        UPGRADE_HEADERS.addAll(Arrays.asList(new String[] { "Host", "Upgrade", "Connection", "Sec-WebSocket-Key", "Sec-WebSocket-Version" }));
    }

    private DataFrameType lastDataFrameStatus = DataFrameType.NONE;

    enum WebSocketState {
        CLOSED,
        CLOSED_CLIENT,  // This side has closed
        CLOSED_SERVER,  // Other side has closed
        CONNECTED,
        CONNECTING;

        public boolean isClosing() {
            return this == CLOSED_CLIENT || this == CLOSED_SERVER;
        }
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
    private String proxyUsername;
    private String proxyPassword;

    public WebSocketClient(URL wsURL) {
        connectUrl = correctUrl(wsURL);
    }

    public URL getConnectUrl() {
        return connectUrl;
    }

    public void setAdditionalUpgradeRequestHeaders(Map<String, String> additionalHeaders) {
        this.additionalHeaders = additionalHeaders;
    }

    public void useProxy(String host, int port, String user, String password) {
        useProxy = true;
        proxyHost = host;
        proxyPort = port;
        proxyUsername = user;
        proxyPassword = password;
    }

    public HttpResult connect() throws IOException, HttpException {
        return connect(Collections.emptyMap(), DEFAULT_CONNECT_TIMEOUT, DEFAULT_READ_TIMEOUT);
    }

    public HttpResult connect(int connectTimeout, int readTimeout) throws IOException, HttpException {
        return connect(Collections.emptyMap(), connectTimeout, readTimeout);
    }

    public HttpResult connect(Map<String, String> headers) throws IOException, HttpException {
        if (additionalHeaders != null && ! additionalHeaders.isEmpty() && headers != null && !headers.isEmpty())
            throw new IllegalArgumentException("Cannot pass headers when setAdditionalUpgradeRequestHeaders is called before");
        return connect(headers, DEFAULT_CONNECT_TIMEOUT, DEFAULT_READ_TIMEOUT);
    }

    public HttpResult connect(Map<String, String> headers, int connectTimeout, int readTimeout) throws IOException, HttpException {
        if (headers != null && ! headers.isEmpty()) {
            if (additionalHeaders != null && !additionalHeaders.isEmpty())
                throw new IllegalArgumentException("Cannot pass headers when setAdditionalUpgradeRequestHeaders is called before");
        }
        else {
            headers = additionalHeaders;
        }
        if (headers == null) {
            headers = Collections.emptyMap();
        }

        if (state != WebSocketState.CLOSED) {
            throw new IllegalStateException("Cannot connect when state is " + state);
        }
        state = WebSocketState.CONNECTING;

        boolean connected = false;
        log.debug("Creating connection with " + connectUrl.getHost() + ":" + connectUrl.getPort());
        if (System.getProperty("socksProxyHost",null) != null)
            log.warn("Socks proxy host is set, but socks proxy is not officially supported.");

        wsSocket = createSocket(connectUrl.getHost(), connectUrl.getPort(), connectTimeout, readTimeout);
        Map<String, String> responseHeaders = null;
        CountingOutputStream outStream = null;
        CountingInputStream inStream = null;
        PrintWriter httpWriter = null;
        try {
            wsSocket.setSoTimeout(readTimeout);
            socketOutputStream = wsSocket.getOutputStream();

            String path = connectUrl.getFile();  // getFile includes path and query string
            if (path == null || !path.trim().startsWith("/"))
                path = "/" + path;
            outStream = new CountingOutputStream(socketOutputStream);
            httpWriter = new PrintWriter(outStream);
            // Fragment identifiers are never sent in HTTP requests and RFC 6455 explicitly states that fragment identifiers must not be used.
            httpWriter.print("GET " + (useProxy? connectUrl.toString(): path) + " HTTP/1.1\r\n");
            log.debug(    ">> GET " + (useProxy? connectUrl.toString(): path) + " HTTP/1.1");
            httpWriter.print("Host: " + connectUrl.getHost() + ":" + connectUrl.getPort() + NEW_LINE);
            log.debug(    ">> Host: " + connectUrl.getHost() + ":" + connectUrl.getPort());
            for (Map.Entry<String, String> header : headers.entrySet()) {
                if (! UPGRADE_HEADERS.contains(header.getKey())) {
                    String headerLine = header.getKey() + ": " + header.getValue();
                    // Ensure header line does _not_ contain new line
                    if (!headerLine.contains("\r") && !headerLine.contains("\n")) {
                        httpWriter.print(headerLine + NEW_LINE);
                        log.debug(">> " + headerLine);
                    }
                    else {
                        throw new IllegalArgumentException("Invalid header; contains new line.");
                    }
                }
                else
                    log.error("Ignoring user supplied header '" + header + "'");
            }
            httpWriter.print("Upgrade: websocket\r\n");
            log.debug(">> Upgrade: websocket");
            httpWriter.print("Connection: Upgrade\r\n");
            log.debug(">> Connection: Upgrade");
            byte[] nonce = new byte[16];
            randomGenerator.nextBytes(nonce);
            String encodeNonce = new String(Base64.getEncoder().encode(nonce));
            httpWriter.print("Sec-WebSocket-Key: " + encodeNonce + NEW_LINE);
            log.debug(">> Sec-WebSocket-Key: " + encodeNonce);
            httpWriter.print("Sec-WebSocket-Version: 13\r\n");
            log.debug(">> Sec-WebSocket-Version: 13");
            httpWriter.print(NEW_LINE);
            log.debug(">>");
            httpWriter.flush();
            
            socketInputStream = new BufferedInputStream(wsSocket.getInputStream());
            inStream = new CountingInputStream(socketInputStream);
            responseHeaders = checkServerResponse(inStream, encodeNonce);
            connected = true;
            state = WebSocketState.CONNECTED;
            return new HttpResult(responseHeaders, outStream.getCount(), inStream.getCount());
        }
        finally {
            if (! connected) {
            	IOUtils.closeQuietly(socketInputStream);
            	IOUtils.closeQuietly(socketOutputStream);
            	IOUtils.closeQuietly(wsSocket);
            	IOUtils.closeQuietly(httpWriter);
                state = WebSocketState.CLOSED;
            }
        }
    }

    public boolean isConnected() {
        return state == WebSocketState.CONNECTED;
    }

    protected Socket createSocket(String host, int port, int connectTimeout, int readTimeout) throws IOException {
        Socket baseSocket = new Socket();
        if (useProxy) {
            log.debug("Using http proxy " + proxyHost + ":" + proxyPort + " for " + connectUrl);
            setupProxyConnection(baseSocket, connectTimeout);
        }
        else
            baseSocket.connect(new InetSocketAddress(host, port), connectTimeout);

        if ("https".equals(connectUrl.getProtocol())) {
            baseSocket.setSoTimeout(readTimeout);

            JsseSSLManager sslMgr = (JsseSSLManager) SSLManager.getInstance();
            try {
                SSLSocketFactory tlsSocketFactory = sslMgr.getContext().getSocketFactory();
                log.debug("Starting TLS connection.");
                return tlsSocketFactory.createSocket(baseSocket, host, port, true);
            } catch (GeneralSecurityException e) {
                throw new IOException(e);
            }
        }
        else {
            return baseSocket;
        }
    }

    private void setupProxyConnection(Socket socket, int connectTimeout) throws IOException {
        try {
            socket.connect(new InetSocketAddress(proxyHost, proxyPort), connectTimeout);
            PrintWriter proxyWriter = new PrintWriter(socket.getOutputStream());
            proxyWriter.print("CONNECT " + connectUrl.getHost() + ":" + connectUrl.getPort() + " HTTP/1.1\r\n");
            log.debug(">proxy> CONNECT " + connectUrl.getHost() + ":" + connectUrl.getPort() + " HTTP/1.1");
            proxyWriter.print("Host: " + connectUrl.getHost() + NEW_LINE);
            log.debug(">proxy> Host: " + connectUrl.getHost());
            if (proxyUsername != null && proxyPassword != null) {
                String authentication = proxyUsername + ":" + proxyPassword;
                proxyWriter.print("Proxy-Authorization: Basic " + Base64.getEncoder().encodeToString(authentication.getBytes()) + NEW_LINE);
                log.debug(">proxy> Proxy-Authorization: Basic " + Base64.getEncoder().encodeToString(authentication.getBytes()));
            }
            proxyWriter.print(NEW_LINE);
            proxyWriter.flush();

            HttpLineReader httpReader = new HttpLineReader(socket.getInputStream());
            String statusLine = httpReader.readLine();

            String line;
            do {
                line = httpReader.readLine();
                log.debug("<proxy< " + line);
            }
            while (line != null && line.trim().length() > 0);  // HTTP response ends with an empty line.
            checkHttpStatus(statusLine, 200);
        }
        catch (HttpUpgradeException httpError) {
            log.error("Proxy connection error", httpError);
            throw new HttpUpgradeException("Connecting proxy failed with status code " + httpError.getStatusCodeAsString(), httpError.getStatusCode());
        }
        catch (SocketTimeoutException timeout) {
            log.error("Proxy connection timeout");
            throw timeout;
        }
        catch (ConnectException cantConnect) {
            log.error("Proxy connection setup error: ", cantConnect);
            throw new ConnectException("Proxy connection setup error: " + cantConnect.getMessage());
        }
        catch (IOException ioError) {
            log.error("Proxy connection setup error: ", ioError);
            throw ioError;
        }
    }

    public void dispose() {
    	IOUtils.closeQuietly(socketInputStream);
    	IOUtils.closeQuietly(socketOutputStream);
    	IOUtils.closeQuietly(wsSocket);
    	state = WebSocketState.CLOSED;
    }

    public void finalize() {
        log.debug("WebSocket client is being garbage collected; underlying TCP connection will be closed");
        // If this object is being gc'd, it's very likely that the streams and socket used by this object will also be gc'd and thus closed,
        // but we'll make it explicit anyway so the side effect of closing the TCP connection when this class is gc'd, is deterministic.
        try {
            dispose();
        }
        catch (Exception error) {
            // Ensure no exceptions thrown by the finalizer ever.
            log.error("Exception thrown during finalize", error);
        }
    }

    /**
     * Close the websocket connection properly, i.e. send a close frame and wait for a close confirm.
     * @param status the status to send in the close frame
     * @param requestData the close reason to send as part of the close frame
     * @param readTimeout read timeout used when reading close response
     * @return the close frame received as response from the server
     * @throws IOException when an IO exception occurs on the underlying connection
     * @throws UnexpectedFrameException when a frame is received that is not a close frame
     */
    public CloseFrame close(int status, String requestData, int readTimeout) throws IOException, UnexpectedFrameException {
        if (state != WebSocketState.CONNECTED) {
            throw new IllegalStateException("Cannot close when state is " + state);
        }
        sendClose(status, requestData);
        return receiveClose(readTimeout);
    }

    public TextFrame sendTextFrame(String requestData) throws IOException {
        if (state != WebSocketState.CONNECTED) {
            throw new IllegalStateException("Cannot send data frame when state is " + state);
        }

        TextFrame frame = new TextFrame(requestData);
        socketOutputStream.write(frame.getFrameBytes());
        return frame;
    }

    public BinaryFrame sendBinaryFrame(byte[] requestData) throws IOException {
        if (state != WebSocketState.CONNECTED) {
            throw new IllegalStateException("Cannot send data frame when state is " + state);
        }

        BinaryFrame frame = new BinaryFrame(requestData);
        socketOutputStream.write(frame.getFrameBytes());
        return frame;
    }

    public Frame sendPingFrame() throws IOException {
        return sendPingFrame(new byte[0]);
    }

    public Frame sendPingFrame(byte[] applicationData) throws IOException {
        if (state != WebSocketState.CONNECTED) {
            throw new IllegalStateException("Cannot send ping frame when state is " + state);
        }

        PingFrame ping = new PingFrame(applicationData);
        socketOutputStream.write(ping.getFrameBytes());
        return ping;
    }

    public Frame sendPongFrame() throws IOException {
        return sendPongFrame(new byte[0]);
    }

    public Frame sendPongFrame(byte[] applicationData) throws IOException {
        if (state != WebSocketState.CONNECTED) {
            throw new IllegalStateException("Cannot send pong frame when state is " + state);
        }

        PongFrame pongFrame = new PongFrame(applicationData);
        socketOutputStream.write(pongFrame.getFrameBytes());
        return pongFrame;
    }

    public Frame sendClose(int closeStatus, String reason) throws IOException {
        if (state != WebSocketState.CONNECTED && state != WebSocketState.CLOSED_SERVER) {
            throw new IllegalStateException("Cannot close when state is " + state);
        }
        CloseFrame closeFrame = new CloseFrame(closeStatus, reason);
        socketOutputStream.write(closeFrame.getFrameBytes());

        if (state == WebSocketState.CONNECTED)
            state = WebSocketState.CLOSED_CLIENT;
        else {
            state = WebSocketState.CLOSED;
            dispose();
        }
        return closeFrame;
    }

    public CloseFrame receiveClose(int timeout) throws IOException, UnexpectedFrameException {
        Frame frame = receiveFrame(timeout);
        if (frame.isClose()) {
            if (state == WebSocketState.CONNECTED)
                state = WebSocketState.CLOSED_SERVER;
            else {
                state = WebSocketState.CLOSED;
                dispose();
            }
            return (CloseFrame) frame;
        }
        else
            throw new UnexpectedFrameException(frame);
    }

    public Frame receiveFrame(int readTimeout) throws IOException {
        if (state != WebSocketState.CONNECTED && state != WebSocketState.CLOSED_CLIENT) {
            throw new IllegalStateException("Cannot receive data frame when state is " + state);
        }

        wsSocket.setSoTimeout(readTimeout);

        Frame receivedFrame = Frame.parseFrame(lastDataFrameStatus, socketInputStream, log);
        if (lastDataFrameStatus == DataFrameType.NONE && receivedFrame.isData() && !((DataFrame) receivedFrame).isFinalFragment()) {
            lastDataFrameStatus = receivedFrame.isText()? DataFrameType.TEXT: DataFrameType.BIN;
        }
        else if (lastDataFrameStatus != DataFrameType.NONE && receivedFrame.isData()) {
            if (((DataFrame) receivedFrame).isContinuationFrame() && ((DataFrame) receivedFrame).isFinalFragment()) {
                lastDataFrameStatus = DataFrameType.NONE;
            }
            else if (! ((DataFrame) receivedFrame).isContinuationFrame())
                throw new ProtocolException("missing continuation frame");
        }

        if (receivedFrame.isClose()) {
            if (state == WebSocketState.CONNECTED)
                state = WebSocketState.CLOSED_SERVER;
            else {
                state = WebSocketState.CLOSED;
                dispose();
            }
        }

        return receivedFrame;
    }

    public TextFrame receiveText(int timeout) throws IOException, UnexpectedFrameException {
        Frame frame = receiveFrame(timeout);
        if (frame.isText())
            return ((TextFrame) frame);
        else
            throw new UnexpectedFrameException(frame);
    }

    public BinaryFrame receiveBinaryData(int timeout) throws IOException, UnexpectedFrameException {
        Frame frame = receiveFrame(timeout);
        if (frame.isBinary())
            return ((BinaryFrame) frame);
        else
            throw new UnexpectedFrameException(frame);
    }

    public PongFrame receivePong(int timeout) throws IOException, UnexpectedFrameException {
        Frame frame = receiveFrame(timeout);
        if (frame.isPong())
            return (PongFrame) frame;
        else
            throw new UnexpectedFrameException(frame);
    }

    protected Map<String, String> checkServerResponse(InputStream inputStream, String nonce) throws IOException {
        HttpLineReader httpReader = new HttpLineReader(inputStream);
        String line = httpReader.readLine();
        if (line != null) {
            log.debug("<< " + line);
            checkHttpStatus(line, 101);
        }
        else
            throw new HttpProtocolException("Empty response; connection closed.");

        Map<String, String> serverHeaders = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);  // Field names of Http Headers are case-insensitive, see https://tools.ietf.org/html/rfc2616#section-4.2
        do {
            line = httpReader.readLine();
            log.debug("<< " + line);
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
        Matcher matcher = HTTP_STATUS_PATTERN.matcher(statusLine);
        if (matcher.find()) {
            int statusCode = Integer.parseInt(matcher.group(1));
            if (statusCode != expectedStatusCode)
                throw new HttpUpgradeException("Got unexpected status " + statusCode 
                		+ " with statusLine:"+statusLine, statusCode);
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
                String path = wsURL.getFile();  // getPath only returns the path, getFile includes the query string!
                if (!path.trim().startsWith("/"))
                    path = "/" + path.trim();
                if (wsURL.getRef() != null) {
                    path = path + "#" + wsURL.getRef();
                }
                return new URL(wsURL.getProtocol(), wsURL.getHost(), wsURL.getPort(), path);
            } catch (MalformedURLException e) {
                // Impossible
                throw new RuntimeException(e);
            }
    }

    public static class HttpResult {
        public Map<String, String> responseHeaders;
        public int requestSize;
        public int responseSize;

        public HttpResult() {
            responseHeaders = Collections.emptyMap();
        }

        public HttpResult(Map<String, String> responseHeaders, int requestSize, int responseSize) {
            this.responseHeaders = responseHeaders;
            this.requestSize = requestSize;
            this.responseSize = responseSize;
        }
    }

}



