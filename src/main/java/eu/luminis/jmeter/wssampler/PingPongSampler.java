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

import eu.luminis.websocket.UnexpectedFrameException;
import eu.luminis.websocket.WebSocketClient;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

import java.io.IOException;
import java.net.MalformedURLException;

public class PingPongSampler extends WebsocketSampler {

    private static final Logger log = LoggingManager.getLoggerForClass();

    @Override
    public SampleResult sample(Entry entry) {
        SampleResult result = new SampleResult();
        result.setSampleLabel(getName());
        String validationError = validateArguments();
        if (validationError != null) {
            result.setResponseCode("Sampler error");
            result.setResponseMessage("Sampler error: " + validationError);
            return result;
        }

        WebSocketClient wsClient = threadLocalCachedConnection.get();
        if (wsClient == null) {
            log.error("There is no connection to re-use");
            result.setResponseCode("Sampler error");
            result.setResponseMessage("Sampler must use existing connection, but there is no connection");
            return result;
        }

        result.sampleStart(); // Start timing
        try {
            int readTimeout = Integer.parseInt(getReadTimeout());
            wsClient.sendPingFrame();
            wsClient.receivePong(readTimeout);
            result.sampleEnd(); // End timimg

            result.setResponseCodeOK();
            result.setResponseMessage("OK");
            result.setSuccessful(true);
        }
        catch (UnexpectedFrameException e) {
            result.sampleEnd(); // End timimg
            log.error("Unexpected frame type received: " + e.getReceivedFrame());
            result.setResponseCode("Sampler error: unexpected frame type.");
            result.setResponseMessage("Received: " + e.getReceivedFrame());
        }
        catch (MalformedURLException e) {
            // Impossible
            throw new RuntimeException(e);
        }
        catch (IOException ioExc) {
            result.sampleEnd(); // End timimg
            log.error("Error during sampling", ioExc);
            result.setResponseCode("Websocket error");
            result.setResponseMessage("WebSocket error: " + ioExc);
        }
        catch (Exception error) {
            result.sampleEnd(); // End timimg
            log.error("Unhandled error during sampling", error);
            result.setResponseCode("Sampler error");
            result.setResponseMessage("Sampler error: " + error);
        }

        return result;
    }

    @Override
    protected Logger getLogger() {
        return log;
    }

    private String validateArguments() {
        String errorMsg = validateReadTimeout(getReadTimeout());
        return errorMsg;
    }

    public String getReadTimeout() {
        return getPropertyAsString("readTimeout", "" + WebSocketClient.DEFAULT_READ_TIMEOUT).trim();
    }

    public void setReadTimeout(String readTimeout) {
        setProperty("readTimeout", readTimeout, "" + WebSocketClient.DEFAULT_READ_TIMEOUT);
    }

}
