package eu.luminis.websocket;

public class BinaryFrame extends Frame {

    private byte[] data;

    public BinaryFrame(byte[] payload) {
        data = payload;
    }

    public byte[] getData() {
        return data;
    }

    public boolean isBinary() {
        return true;
    }

    @Override
    public String toString() {
        return "Binary frame with length " + data.length;
    }

    @Override
    protected byte[] getPayload() {
        return data;
    }

    @Override
    protected byte getOpCode() {
        return OPCODE_BINARY;
    }
}

