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

import eu.luminis.websocket.CloseFrame;
import eu.luminis.websocket.UnexpectedFrameException;
import eu.luminis.websocket.WebSocketClient;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class CloseWebSocketSampler extends WebsocketSampler {

    private static final Logger log = LoggingManager.getLoggerForClass();


    @Override
    public SampleResult sample(Entry entry) {
        SampleResult result = new SampleResult();
        result.setSampleLabel(getName());
        result.setSuccessful(false);
        int closeStatus = 1000;
        String reason = "sampler requested close";
        result.setSamplerData("Requested connection close with status " + closeStatus + " and reason '" + reason + "'.");

        WebSocketClient wsClient = threadLocalCachedConnection.get();
        if (wsClient != null) {
            try {
                result.sampleStart();
                CloseFrame frame = wsClient.close(closeStatus, reason, getReadTimeout());
                result.sampleEnd();
                result.setSuccessful(true);
                result.setResponseMessage("Connection closed.");
                result.setResponseCode(frame.getCloseStatus().toString());
                result.setResponseData("" + frame.getCloseStatus() + ": " + frame.getCloseReason(), StandardCharsets.UTF_8.name());
                result.setDataType(SampleResult.TEXT);
            }
            catch (IOException e) {
                result.sampleEnd();
                log.error(e.toString());
                result.setResponseCode("WebSocket error.");
                result.setResponseMessage("WebSocket error: " + e);
            }
            catch (UnexpectedFrameException e) {
                result.sampleEnd();
                log.error("Close request was not answered with close response, but " + e.getReceivedFrame());
                result.setResponseCode("WebSocket error: unsuccesful close.");
                result.setResponseMessage("WebSocket error: received not a close frame, but " + e.getReceivedFrame());
            }
            threadLocalCachedConnection.set(null);
        }
        else {
            log.warn("There is no connection; nothing to close.");
            result.setSamplerData("No request sent.");
            result.setResponseMessage("No connection; nothing to close.");
        }

        return result;
    }

    public int getReadTimeout() {
        return getPropertyAsInt("readTimeout", WebSocketClient.DEFAULT_READ_TIMEOUT);
    }

    public void setReadTimeout(int readTimeout) {
        setProperty("readTimeout", readTimeout, WebSocketClient.DEFAULT_READ_TIMEOUT);
    }

    @Override
    protected Logger getLogger() {
        return log;
    }
}
