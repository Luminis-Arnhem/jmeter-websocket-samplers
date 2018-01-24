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
import eu.luminis.websocket.UnexpectedFrameException;
import eu.luminis.websocket.WebSocketClient;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

import java.io.IOException;


public class PingPongSampler extends WebsocketSampler {

    enum Type {
        PingPong,
        Pong
    }

    private static final Logger log = LoggingManager.getLoggerForClass();

    @Override
    protected WebSocketClient prepareWebSocketClient(SampleResult result) {
        WebSocketClient wsClient = threadLocalCachedConnection.get();
        if (wsClient == null) {
            log.error("Sampler '"+ getName() + "': there is no connection to re-use");
            result.setResponseCode("Sampler error");
            result.setResponseMessage("Sampler must use existing connection, but there is no connection");
            return null;
        }
        else
            return wsClient;
    }

    @Override
    public Frame doSample(WebSocketClient wsClient, SampleResult result) throws IOException, UnexpectedFrameException {
        if (getType().equals(Type.PingPong)) {
            Frame sentFrame = wsClient.sendPingFrame();
            result.setSentBytes(sentFrame.getSize());

            Frame receivedFrame;
            if (!frameFilters.isEmpty()) {
                receivedFrame = frameFilters.get(0).receiveFrame(frameFilters.subList(1, frameFilters.size()), wsClient, readTimeout, result);
                if (receivedFrame.isPong())
                    return receivedFrame;
                else
                    throw new UnexpectedFrameException(receivedFrame);
            } else
                return wsClient.receivePong(readTimeout);
        }
        else {
            Frame sentFrame = wsClient.sendPongFrame();
            result.setSentBytes(sentFrame.getSize());
            return null;
        }
    }

    @Override
    protected Logger getLogger() {
        return log;
    }

    @Override
    protected String validateArguments() {
        String errorMsg = validateReadTimeout(getReadTimeout());
        return errorMsg;
    }

    public Type getType() {
        return Type.valueOf(getPropertyAsString("type", Type.PingPong.name()));
    }

    public void setType(Type type) {
        setProperty("type", type.name());
    }

}
