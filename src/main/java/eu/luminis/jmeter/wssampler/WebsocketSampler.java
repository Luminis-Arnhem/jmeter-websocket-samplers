package eu.luminis.jmeter.wssampler;

import eu.luminis.websocket.WebSocketClient;
import org.apache.jmeter.samplers.AbstractSampler;

abstract public class WebsocketSampler extends AbstractSampler {

    public static final int MIN_CONNECTION_TIMEOUT = 1;
    public static final int MAX_CONNECTION_TIMEOUT = 999999;
    public static final int MIN_READ_TIMEOUT = 0;
    public static final int MAX_READ_TIMEOUT = 999999;
    public static final int DEFAULT_WS_PORT = 80;

    protected static final ThreadLocal<WebSocketClient> threadLocalCachedConnection = new ThreadLocal<>();

    protected String validatePortNumber(String value) {
        try {
            int port = Integer.parseInt(value);
            if (port <= 0 || port > 65535)
                return "Port number '" + value + "' is not valid.";
        } catch (NumberFormatException notAnumber) {
            return "Port number '" + value + "' is not a number.";
        }
        return null;
    }

    protected String validateConnectionTimeout(String value) {
        try {
            int connectTimeout = Integer.parseInt(value);
            if (connectTimeout < RequestResponseWebSocketSampler.MIN_CONNECTION_TIMEOUT || connectTimeout > RequestResponseWebSocketSampler.MAX_CONNECTION_TIMEOUT)
                return "Connection timeout '" + connectTimeout + "' is not valid; should between " + RequestResponseWebSocketSampler.MIN_CONNECTION_TIMEOUT + " and " + RequestResponseWebSocketSampler.MAX_CONNECTION_TIMEOUT;
        } catch (NumberFormatException notAnumber) {
            return "Connection timeout '" + value + "' is not a number.";
        }
        return null;
    }

    protected String validateReadTimeout(String value) {
        try {
            int readTimeout = Integer.parseInt(value);
            if (readTimeout < RequestResponseWebSocketSampler.MIN_READ_TIMEOUT || readTimeout > RequestResponseWebSocketSampler.MAX_READ_TIMEOUT)
                return "Read timeout '" + readTimeout + "' is not valid; should between " + RequestResponseWebSocketSampler.MIN_READ_TIMEOUT + " and " + RequestResponseWebSocketSampler.MAX_READ_TIMEOUT;
        }
        catch (NumberFormatException notAnumber) {
            return "Read timeout '" + value + "' is not a number.";
        }

        return null;
    }
}
