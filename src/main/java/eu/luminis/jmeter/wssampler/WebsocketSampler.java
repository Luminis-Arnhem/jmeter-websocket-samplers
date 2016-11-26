/*
 * Copyright 2016 Peter Doornbosch
 *
 * This file is part of JMeter-WebSocket-Samplers, a JMeter add-on for load-testing WebSocket applications.
 *
 * JMeter-WebSocket-Samplers is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your option)
 * any later version.
 *
 * JMeter-WebSocket-Samplers is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package eu.luminis.jmeter.wssampler;

import eu.luminis.websocket.WebSocketClient;
import org.apache.jmeter.samplers.AbstractSampler;
import org.apache.log.Logger;

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

    protected void dispose(WebSocketClient webSocketClient) {
        if (webSocketClient != null) {
            getLogger().debug("Closing streams for existing websocket connection");
            webSocketClient.dispose();
        }
    }

    abstract protected Logger getLogger();
}
