package eu.luminis.jmeter.wssampler;

import eu.luminis.websocket.WebSocketClient;

public class SharedContext {

    static final ThreadLocal<WebSocketClient> threadLocalCachedConnection = new ThreadLocal<>();

}
