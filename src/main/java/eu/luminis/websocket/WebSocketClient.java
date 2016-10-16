package eu.luminis.websocket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WebSocketClient {

    enum WebSocketState {
        CLOSED,
        CLOSING,
        CONNECTED,
        CONNECTING
    }

    private final URL connectUrl;
    private Socket wsSocket;
    private Random randomGenerator = new Random();
    private volatile WebSocketState state = WebSocketState.CLOSED;

    public WebSocketClient(URL wsURL) {
        connectUrl = wsURL;
    }

    public URL getConnectUrl() {
        return connectUrl;
    }

    public void connect() throws IOException, HttpException {
        connect(Collections.EMPTY_MAP);
    }

    public void connect(Map<String, String> headers) throws IOException, HttpException {
        if (state != WebSocketState.CLOSED) {
            throw new IllegalStateException("Cannot connect when state is " + state);
        }
        state = WebSocketState.CONNECTING;

        boolean connected = false;
        wsSocket = new Socket();
        InputStream inputStream = null;
        OutputStream outputStream = null;

        try {
            int connectTimeout = 5000;
            wsSocket.connect(new InetSocketAddress(connectUrl.getHost(), connectUrl.getPort()), connectTimeout);
            inputStream = wsSocket.getInputStream();
            outputStream = wsSocket.getOutputStream();

            String path = connectUrl.getFile();  // getFile includes path and query string
            if (path == null || !path.trim().startsWith("/"))
                path = "/" + path;
            PrintWriter httpWriter = new PrintWriter(outputStream);
            httpWriter.println("GET " + path + " HTTP/1.1\r");
            httpWriter.println("Host: " + connectUrl.getHost() + "\r");
            for (Map.Entry<String, String> header : headers.entrySet()) {
                String headerLine = header.getKey() + ": " + header.getValue();
                // Ensure header line does _not_ contain new line
                if (!headerLine.contains("\r") && !headerLine.contains("\n"))
                    httpWriter.println(headerLine + "\r");
                else {
                    throw new IllegalArgumentException("Invalid header; contains new line.");
                }
            }
            httpWriter.println("Upgrade: websocket\r");
            httpWriter.println("Connection: Upgrade\r");
            byte[] nonce = new byte[16];
            randomGenerator.nextBytes(nonce);
            String encodeNonce = new String(Base64.getEncoder().encode(nonce));
            httpWriter.println("Sec-WebSocket-Key: " + encodeNonce + "\r");
            httpWriter.println("Sec-WebSocket-Version: 13\r");
            httpWriter.println("\r");
            httpWriter.flush();

            checkServerResponse(inputStream, encodeNonce);
            connected = true;
            state = WebSocketState.CONNECTED;
        }
        finally {
            if (! connected) {
                if (inputStream != null)
                    inputStream.close();
                if (outputStream != null)
                    outputStream.close();
                if (wsSocket != null)
                    wsSocket.close();
                state = WebSocketState.CLOSED;
            }
        }
    }

    public void dispose() {
        try {
            if (wsSocket.getInputStream() != null)
                wsSocket.getInputStream().close();
            if (wsSocket.getOutputStream() != null)
                wsSocket.getOutputStream().close();
            if (wsSocket != null)
                wsSocket.close();
            state = WebSocketState.CLOSED;
        } catch (IOException e) {}
    }

    /**
     * Close the websocket connection properly, i.e. send a close frame and wait for a close confirm.
     */
    public CloseFrame close(int status, String requestData) throws IOException, UnexpectedFrameException {
        if (state != WebSocketState.CONNECTED) {
            throw new IllegalStateException("Cannot close when state is " + state);
        }
        state = WebSocketState.CLOSED;
        wsSocket.getOutputStream().write(new CloseFrame(status, requestData).getFrameBytes());
        return receiveClose();
    }

    public CloseFrame receiveClose() throws IOException, UnexpectedFrameException {
        Frame frame = Frame.parseFrame(wsSocket.getInputStream());
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

        wsSocket.getOutputStream().write(new TextFrame(requestData).getFrameBytes());
    }

    public void sendBinaryFrame(byte[] requestData) throws IOException {
        if (state != WebSocketState.CONNECTED) {
            throw new IllegalStateException("Cannot send data frame when state is " + state);
        }

        wsSocket.getOutputStream().write(new BinaryFrame(requestData).getFrameBytes());
    }

    public String receiveText() throws IOException, UnexpectedFrameException {
        if (state != WebSocketState.CONNECTED) {
            throw new IllegalStateException("Cannot receive data frame when state is " + state);
        }

        Frame frame = Frame.parseFrame(wsSocket.getInputStream());
        if (frame.isText())
            return ((TextFrame) frame).getText();
        else
            throw new UnexpectedFrameException(frame);
    }

    public byte[] receiveBinaryData() throws IOException, UnexpectedFrameException {
        if (state != WebSocketState.CONNECTED) {
            throw new IllegalStateException("Cannot receive data frame when state is " + state);
        }

        Frame frame = Frame.parseFrame(wsSocket.getInputStream());
        if (frame.isBinary())
            return ((BinaryFrame) frame).getData();
        else
            throw new UnexpectedFrameException(frame);
    }

    protected void checkServerResponse(InputStream inputStream, String nonce) throws IOException {
        BufferedReader httpReader = new BufferedReader(new InputStreamReader(inputStream));
        String line = httpReader.readLine();
        if (line != null)
            checkHttpStatus(line, 101);
        else
            throw new HttpProtocolException("Missing status line in response");

        Map<String, String> serverHeaders = new HashMap<>();
        do {
            line = httpReader.readLine();
            if (line != null) {
                String[] values = line.split(": ");
                if (values.length > 1)
                    serverHeaders.put(values[0], values[1]);
            }
        }
        while (line != null && line.trim().length() > 0);  // HTTP response ends with an empty line.

        // Check server response for mandatory headers
        if (! "websocket".equals(getLowerCase(serverHeaders.get("Upgrade"))))  // Specification is not clear about whether the check should be case-insensative...
            throw new HttpUpgradeException("Server response should contain 'Upgrade' header with value 'websocket'");
        if (! "Upgrade".equals(serverHeaders.get("Connection")))
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
}



