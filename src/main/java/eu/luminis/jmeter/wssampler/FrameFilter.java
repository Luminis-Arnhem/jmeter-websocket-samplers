/*
 * Copyright 2016, 2017 Peter Doornbosch
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
import org.apache.jmeter.config.ConfigElement;
import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

import java.io.IOException;

public class FrameFilter extends ConfigTestElement {

    private static final Logger log = LoggingManager.getLoggerForClass();

    public FrameFilter() {
        super();
    }

    public Frame receiveFrame(WebSocketClient wsClient, int readTimeout) throws IOException {
        Frame receivedFrame;
        do {
            receivedFrame = wsClient.receiveFrame(readTimeout);
        }
        while (matchesFilter(receivedFrame));
        return receivedFrame;
    }

    private boolean matchesFilter(Frame receivedFrame) {
        boolean match = receivedFrame.isPing() || receivedFrame.isPong();
        if (match)
            log.debug("Filter discards frame " + receivedFrame);
        return match;
    }

    @Override
    public boolean expectsModification() {
        return super.expectsModification();
    }

    @Override
    public void addTestElement(TestElement parm1) {
        super.addTestElement(parm1);
    }

    @Override
    public void addConfigElement(ConfigElement config) {
        super.addConfigElement(config);
    }

    @Override
    public String toString() {
        return "Frame Filter '" + getName() + "'";
    }

}
