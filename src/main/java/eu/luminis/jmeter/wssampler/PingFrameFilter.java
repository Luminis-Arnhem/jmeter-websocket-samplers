/*
 * Copyright © 2016, 2017, 2018 Peter Doornbosch
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

import eu.luminis.websocket.Frame;
import eu.luminis.websocket.WebSocketClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class PingFrameFilter extends FrameFilter {

    private static Logger log = LoggerFactory.getLogger(PingFrameFilter.class);

    @Override
    protected boolean matchesFilter(Frame receivedFrame) {
        return receivedFrame.isPing() || receivedFrame.isPong();
    }

    @Override
    public String toString() {
        return "Ping Frame Filter '" + getName() + "'";
    }

    @Override
    protected Frame performReplyAction(WebSocketClient wsClient, Frame receivedFrame) throws IOException {
        if (getReplyToPing()) {
            log.debug("Automatically replying to ping with a pong.");
            return wsClient.sendPongFrame();
        }
        else
            return null;
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
