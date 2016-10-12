package eu.luminis.websocket;

public class HttpUpgradeException extends HttpException {

    private Integer statusCode;

    public HttpUpgradeException(int statusCode) {
        super("Http Upgrade failed");
        this.statusCode = statusCode;
    }

    public HttpUpgradeException(String msg) {
        super(msg);
    }

    public String getStatusCode() {
        return "" + (statusCode != null? statusCode: "");
    }
}
