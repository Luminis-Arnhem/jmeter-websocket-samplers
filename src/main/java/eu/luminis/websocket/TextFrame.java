package eu.luminis.websocket;

import java.nio.charset.StandardCharsets;

public class TextFrame extends Frame {

    private String text;

    public TextFrame(String text) {
        this.text = text;
    }

    public TextFrame(byte[] payload) {
        text = new String(payload, StandardCharsets.UTF_8);
    }

    public String getText() {
        return text;
    }

    public boolean isText() {
        return true;
    }

    @Override
    public String toString() {
        return "Text frame with text '" + text + "'";
    }

    @Override
    protected byte[] getPayload() {
        return text.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    protected byte getOpCode() {
        return OPCODE_TEXT;
    }
}
