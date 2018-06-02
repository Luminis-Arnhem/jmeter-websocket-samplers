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
import eu.luminis.websocket.PingFrame;
import eu.luminis.websocket.WebSocketClient;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

import java.io.IOException;

public class PingFrameFilter extends FrameFilter {

    public enum PingFilterType {
        FilterAll,
        FilterPingOnly,
        FilterPongOnly
    }

    private static Logger log = LoggingManager.getLoggerForClass();

    @Override
    protected boolean matchesFilter(Frame receivedFrame) {
        switch (getFilterType()) {
            case FilterAll:
                return receivedFrame.isPing() || receivedFrame.isPong();
            case FilterPingOnly:
                return receivedFrame.isPing();
            case FilterPongOnly:
                return receivedFrame.isPong();
            default:
                throw new RuntimeException("Unknown filter type");
        }

    }

    @Override
    public String toString() {
        return "Ping Frame Filter '" + getName() + "'";
    }

    @Override
    protected Frame performReplyAction(WebSocketClient wsClient, Frame receivedFrame) throws IOException {
        if (receivedFrame.isPing() && getReplyToPing()) {
            log.debug("Automatically replying to ping with a pong.");
            return wsClient.sendPongFrame(((PingFrame) receivedFrame).getData());
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

    public PingFilterType getFilterType() {
        return PingFilterType.valueOf(getPropertyAsString("filterType", "FilterAll"));
    }

    public void setFilterType(PingFilterType type) {
        setProperty("filterType", type.toString());
    }
}
