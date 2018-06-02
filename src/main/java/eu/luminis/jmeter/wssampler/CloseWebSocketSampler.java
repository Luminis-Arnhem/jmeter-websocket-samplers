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

import eu.luminis.websocket.CloseFrame;
import eu.luminis.websocket.Frame;
import eu.luminis.websocket.UnexpectedFrameException;
import eu.luminis.websocket.WebSocketClient;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class CloseWebSocketSampler extends WebsocketSampler {

    private static final Logger log = LoggingManager.getLoggerForClass();

    @Override
    protected WebSocketClient prepareWebSocketClient(SampleResult result) {
        WebSocketClient wsClient = threadLocalCachedConnection.get();
        if (wsClient == null) {
            log.warn("Sampler '"+ getName() + "': there is no connection; nothing to close.");
            result.setSamplerData("No request sent.");
            result.setResponseMessage("No connection; nothing to close.");
            return null;
        }
        else
            return wsClient;
    }

    @Override
    protected Frame doSample(WebSocketClient wsClient, SampleResult result) throws IOException, UnexpectedFrameException {
        int closeStatus = Integer.parseInt(getStatusCode());
        String reason = "sampler requested close";
        result.setSamplerData("Requested connection close with status " + closeStatus + " and reason '" + reason + "'.");

        Frame frameSent = wsClient.sendClose(closeStatus, reason);
        result.setSentBytes(frameSent.getSize());

        Frame receivedFrame = null;
        if (!frameFilters.isEmpty()) {
            receivedFrame = frameFilters.get(0).receiveFrame(frameFilters.subList(1, frameFilters.size()), wsClient, readTimeout, result);
        }
        else
            receivedFrame = wsClient.receiveFrame(readTimeout);

        if (receivedFrame.isClose())
            return receivedFrame;
        else
            throw new UnexpectedFrameException(receivedFrame);
    }

    @Override
    protected void postProcessResponse(Frame response, SampleResult result) {
        CloseFrame frame = (CloseFrame) response;
        result.setResponseMessage("Connection closed" + (frame.getCloseReason() != null? "; close reason: '" + frame.getCloseReason() + "'.": "."));
        result.setResponseCode(frame.getCloseStatus() != null? frame.getCloseStatus().toString(): "");
        result.setResponseData("" + (frame.getCloseStatus() != null? frame.getCloseStatus(): "") +
                (frame.getCloseReason() != null? ": " + frame.getCloseReason(): ""), StandardCharsets.UTF_8.name());
        result.setDataType(SampleResult.TEXT);
    }

    @Override
    protected void handleUnexpectedFrameException(UnexpectedFrameException e, SampleResult result) {
        log.debug("Sampler '"+ getName() + "': close request was not answered with close response, but " + e.getReceivedFrame());
        result.setResponseCode("WebSocket error: unsuccesful close.");
        result.setResponseMessage("WebSocket error: received not a close frame, but " + e.getReceivedFrame());
    }

    @Override
    protected Logger getLogger() {
        return log;
    }

    @Override
    protected String validateArguments() {
        String errorMsg = null;
        errorMsg = validateReadTimeout(getReadTimeout());
        return errorMsg;
    }

    public String getStatusCode() {
        return getPropertyAsString("statusCode", "1000");
    }

    public void setStatusCode(String status) {
        setProperty("statusCode", status);
    }

    @Override
    public void setTLS(boolean value) {
        throw new UnsupportedOperationException();
    }

}
