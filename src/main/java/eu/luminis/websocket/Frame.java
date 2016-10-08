package eu.luminis.websocket;

import java.io.IOException;
import java.io.InputStream;

public abstract class Frame {

    public static final int OPCODE_CLOSE = 0x08;

    static Frame parseFrame(InputStream istream) throws IOException {
        int opCode = istream.read() & 0x0f;
        int length = istream.read() & 0x7f;
        byte[] payload = new byte[length];
        istream.read(payload);
        switch (opCode) {
            case OPCODE_CLOSE:
                return new CloseFrame(payload);
            default:
                throw new RuntimeException("unsupported frame type: " + opCode);
        }
    }

}
