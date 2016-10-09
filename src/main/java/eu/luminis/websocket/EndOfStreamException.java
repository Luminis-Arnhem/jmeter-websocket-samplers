package eu.luminis.websocket;

import java.io.IOException;

public class EndOfStreamException extends IOException {

    public EndOfStreamException(String message) {
        super(message);
    }
}
