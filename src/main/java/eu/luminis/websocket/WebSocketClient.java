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
import java.util.Base64;
import java.util.Collections;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WebSocketClient {

    // TODO: status: when closed / closing, don't accept anything.

    public static final int OPCODE_CLOSE = 0x08;
    private Socket wsSocket;
    private Random randomGenerator = new Random();

    public void connect(URL wsURL) throws IOException, HttpException {
        connect(wsURL, Collections.EMPTY_MAP);
    }

    public void connect(URL wsURL, Map<String, String> headers) throws IOException, HttpException {
        boolean connected = false;
        wsSocket = new Socket();
        InputStream inputStream = null;
        OutputStream outputStream = null;

        try {

            int connectTimeout = 5000;
            wsSocket.connect(new InetSocketAddress(wsURL.getHost(), wsURL.getPort()), connectTimeout);
            inputStream = wsSocket.getInputStream();
            outputStream = wsSocket.getOutputStream();

            String path = wsURL.getPath();
            if (path == null || path.trim().length() == 0)
                path = "/";
            PrintWriter httpWriter = new PrintWriter(outputStream);
            httpWriter.println("GET " + path + " HTTP/1.1\r");
            httpWriter.println("Host: " + wsURL.getHost() + "\r");
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
            httpWriter.println("Sec-WebSocket-Key: " + new String(Base64.getEncoder().encode(nonce)) + "\r");
            httpWriter.println("Sec-WebSocket-Version: 13\r");
            httpWriter.println("\r");
            httpWriter.flush();

            BufferedReader httpReader = new BufferedReader(new InputStreamReader(inputStream));
            String line = httpReader.readLine();
            checkHttpStatus(line, 101);
            do {
                line = httpReader.readLine();
            }
            while (line != null && line.trim().length() > 0);  // HTTP response ends with an empty line.
            connected = true;
        }
        finally {
            if (! connected) {
                if (inputStream != null)
                    inputStream.close();
                if (outputStream != null)
                    outputStream.close();
                if (wsSocket != null)
                    wsSocket.close();
            }
        }
    }

    /**
     * Close the websocket connection property, i.e. send a close frame and wait for a close confirm.
     */
    public CloseFrame close(int status, String requestData) throws IOException, UnexpectedFrameException {
        wsSocket.getOutputStream().write(new CloseFrame(status, requestData).getFrameBytes());
        return receiveClose();
    }

    public CloseFrame receiveClose() throws IOException, UnexpectedFrameException {
        Frame frame = Frame.parseFrame(wsSocket.getInputStream());
        if (frame.isClose())
            return (CloseFrame) frame;
        else
            throw new UnexpectedFrameException(frame);
    }

    public void sendTextFrame(String requestData) throws IOException {
        wsSocket.getOutputStream().write(new TextFrame(requestData).getFrameBytes());
    }

    public void sendBinaryFrame(byte[] requestData) throws IOException {
        wsSocket.getOutputStream().write(new BinaryFrame(requestData).getFrameBytes());
    }

    public String receiveText() throws IOException, UnexpectedFrameException {
        Frame frame = Frame.parseFrame(wsSocket.getInputStream());
        if (frame.isText())
            return ((TextFrame) frame).getText();
        else
            throw new UnexpectedFrameException(frame);
    }

    public byte[] receiveBinaryData() throws IOException, UnexpectedFrameException {
        Frame frame = Frame.parseFrame(wsSocket.getInputStream());
        if (frame.isBinary())
            return ((BinaryFrame) frame).getData();
        else
            throw new UnexpectedFrameException(frame);
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
}



