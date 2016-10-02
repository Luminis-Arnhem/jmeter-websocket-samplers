package eu.luminis.jmeter.wssampler;

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
import java.util.Random;

public class WebSocketClient {

    private Socket wsSocket;
    private Random randomGenerator = new Random();

    public void connect(URL wsURL) throws IOException {
        wsSocket = new Socket();
        int connectTimeout = 5000;
        wsSocket.connect(new InetSocketAddress(wsURL.getHost(), wsURL.getPort()), connectTimeout);
        InputStream istream = wsSocket.getInputStream();
        OutputStream ostream = wsSocket.getOutputStream();

        PrintWriter httpWriter = new PrintWriter(ostream);
        httpWriter.println("GET / HTTP/1.1\r");
        httpWriter.println("Host: " + wsURL.getHost() + "\r");
        httpWriter.println("Upgrade: websocket\r");
        httpWriter.println("Connection: Upgrade\r");
        byte[] nonce = new byte[16];
        randomGenerator.nextBytes(nonce);
        httpWriter.println("Sec-WebSocket-Key: " + new String(Base64.getEncoder().encode(nonce)) + "\r");
        httpWriter.println("Sec-WebSocket-Version: 13\r");
        httpWriter.println("\r");
        httpWriter.flush();

        BufferedReader httpReader = new BufferedReader(new InputStreamReader(istream));
        String line;
        do {
            line = httpReader.readLine();
        }
        while (line != null && line.trim().length() > 0);  // HTTP response ends with an empty line.

        // TODO: check http response for (correctly) accepting WS upgrade
    }

    public void sendTextFrame(String requestData) throws IOException {
        wsSocket.getOutputStream().write(createTextFrame(requestData));
    }

    public String receiveText() throws IOException {
        // TODO: check that received frame is text-frame indeed.
        return parseFrame(wsSocket.getInputStream());
    }

    private byte[] createTextFrame(String text) {
        byte length = (byte) text.length();
        int maskedLength = 0x80 | length;

        byte[] mask = new byte[4];
        randomGenerator.nextBytes(mask);
        byte[] payload = text.getBytes();
        byte[] masked = new byte[payload.length];
        for (int i = 0; i < payload.length; i++) {
            masked[i] = (byte) (payload[i] ^ mask[i%4]);
        }
        byte[] frame = new byte[payload.length + 2 + 4];
        frame[0] = (byte) (0x80 | 0x01);
        frame[1] = (byte) (0x80 | maskedLength);
        System.arraycopy(mask, 0, frame, 2, 4);
        System.arraycopy(masked, 0, frame, 6, payload.length);
        return frame;
    }

    private String parseFrame(InputStream istream) throws IOException {
        int opCode = istream.read() & 0x0f;
        int length = istream.read() & 0x7f;
        byte[] payload = new byte[length];
        istream.read(payload);
        return new String(payload);
    }

}



