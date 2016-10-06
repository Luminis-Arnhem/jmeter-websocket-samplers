package eu.luminis.websocket;

import java.io.IOException;

public class HttpException extends IOException {

    public HttpException() {}

    public HttpException(String msg) {
        super(msg);
    }
}
