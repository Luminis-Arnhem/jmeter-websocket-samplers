package eu.luminis.websocket;

import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

public abstract class Frame {

    public static final int OPCODE_CONT = 0x00;
    public static final int OPCODE_TEXT = 0x01;
    public static final int OPCODE_BINARY = 0x02;
    public static final int OPCODE_CLOSE = 0x08;
    public static final int OPCODE_PING = 0x09;
    public static final int OPCODE_PONG = 0x0a;
    public static final int FIN_BIT_ON = 0x80;
    public static final int MASK_BIT_MASKED = 0x80;

    private Random randomGenerator = new Random();


    static Frame parseFrame(InputStream istream) throws IOException {
        int byte1 = istream.read();
        if (byte1 == -1)
            throw new EndOfStreamException("end of stream");
        int byte2 = istream.read();
        if (byte2 == -1)
            throw new EndOfStreamException("end of stream");

        int opCode = byte1 & 0x0f;
        int length = byte2 & 0x7f;
        byte[] payload = new byte[length];
        int bytesRead = istream.read(payload);
        if (bytesRead == -1)
            throw new EndOfStreamException("end of stream");
        if (bytesRead != length)
            throw new EndOfStreamException("WebSocket protocol error: expected payload of length " + length + ", but can only read " + bytesRead + " bytes");
        switch (opCode) {
            case OPCODE_TEXT:
                return new TextFrame(payload);
            case OPCODE_BINARY:
                return new BinaryFrame(payload);
            case OPCODE_CLOSE:
                return new CloseFrame(payload);
            default:
                throw new RuntimeException("unsupported frame type: " + opCode);
        }
    }

    protected Frame() {
    }

    public byte[] getFrameBytes() {
        // Generate mask
        byte[] mask = new byte[4];
        randomGenerator.nextBytes(mask);
        // Mask payload
        byte[] payload = getPayload();
        byte[] masked = new byte[payload.length];
        for (int i = 0; i < payload.length; i++) {
            masked[i] = (byte) (payload[i] ^ mask[i%4]);
        }
        // Create frame bytes
        byte[] frame = new byte[payload.length + 2 + 4];
        frame[0] = (byte) (FIN_BIT_ON | getOpCode());
        frame[1] = (byte) (MASK_BIT_MASKED | payload.length);
        System.arraycopy(mask, 0, frame, 2, 4);
        System.arraycopy(masked, 0, frame, 6, payload.length);
        return frame;
    }

    public boolean isText() {
        return false;
    }

    public boolean isBinary() {
        return false;
    }

    public boolean isClose() {
        return false;
    }

    protected abstract byte[] getPayload();

    protected abstract byte getOpCode();
}
