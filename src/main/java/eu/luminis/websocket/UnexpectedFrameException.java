package eu.luminis.websocket;

public class UnexpectedFrameException extends Exception {

    private Frame frame;

    public UnexpectedFrameException(Frame frame) {
        this.frame = frame;
    }

    public Frame getReceivedFrame() {
        return frame;
    }
}
