package eu.luminis.websocket;

import eu.luminis.websocket.HttpException;

public class HttpUpgradeException extends HttpException {

    private int statusCode;

    public HttpUpgradeException(int statusCode) {
        super("Http Upgrade failed");
        this.statusCode = statusCode;
    }

    public String getStatusCode() {
        return "" + statusCode;
    }
}
