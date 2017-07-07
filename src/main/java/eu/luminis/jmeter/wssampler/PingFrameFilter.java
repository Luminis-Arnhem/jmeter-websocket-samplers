package eu.luminis.jmeter.wssampler;

import eu.luminis.websocket.Frame;
import eu.luminis.websocket.WebSocketClient;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

import java.io.IOException;

public class PingFrameFilter extends FrameFilter {

    private static Logger log = LoggingManager.getLoggerForClass();

    @Override
    protected boolean matchesFilter(Frame receivedFrame) {
        return receivedFrame.isPing() || receivedFrame.isPong();
    }

    @Override
    public String toString() {
        return "Ping Frame Filter '" + getName() + "'";
    }

    @Override
    protected void performReplyAction(WebSocketClient wsClient, Frame receivedFrame) throws IOException {
        if (getReplyToPing()) {
            log.debug("Automatically replying to ping with a pong.");
            wsClient.sendPongFrame();
        }

    }

    @Override
    protected Logger getLogger() {
        return log;
    }

    public boolean getReplyToPing() {
        return getPropertyAsBoolean("replyToPing");
    }

    public void setReplyToPing(boolean value) {
        setProperty("replyToPing", value);
    }

}
