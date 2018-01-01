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

import eu.luminis.websocket.*;
import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.log.Logger;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.Collections;
import java.util.List;

/**
 * Base class for frame filters.
 * This class (and thus it's descendants) are not thread-safe: they don't have to, because JMeter accesses filters only
 * from one thread.
 */
public abstract class FrameFilter extends ConfigTestElement {

    public final static String RESULT_SIZE_INCLUDES_FILTERED_FRAMES = "websocket.result.size_includes_filtered_frames";

    private static boolean includeFilteredFramesInSize;

    static {
        initStaticFilterOptions();
    }

    public FrameFilter() {
        super();
    }

    public Frame receiveFrame(WebSocketClient wsClient, int readTimeout, SampleResult result) throws IOException {
        return receiveFrame(Collections.emptyList(), wsClient, readTimeout, result);
    }

    public Frame receiveFrame(List<FrameFilter> filterList, WebSocketClient wsClient, int readTimeout, SampleResult result) throws IOException {
        prepareFilter();

        Frame receivedFrame;
        int socketTimeout = readTimeout;
        boolean matchesFilter;
        do {
            SampleResult subResult = new SampleResult();
            subResult.sampleStart();
            long start = System.currentTimeMillis();

            receivedFrame =
                    !filterList.isEmpty()?
                            filterList.get(0).receiveFrame(filterList.subList(1, filterList.size()), wsClient, socketTimeout, result):
                            wsClient.receiveFrame(socketTimeout);
            long timeSpent = System.currentTimeMillis() - start;

            matchesFilter = matchesFilter(receivedFrame);
            if (matchesFilter) {
                subResult.sampleEnd();
                getLogger().debug("Filter discards " + receivedFrame);
                Frame sentFrame = performReplyAction(wsClient, receivedFrame);

                subResult.setSampleLabel("Discarded " + receivedFrame.getTypeAsString() + " frame (by filter '" + getName() + "')");
                subResult.setSuccessful(true);
                subResult.setResponseMessage("Received " + receivedFrame);
                subResult.setHeadersSize(receivedFrame.getSize() - receivedFrame.getPayloadSize());
                subResult.setBodySize(receivedFrame.getPayloadSize());
                if (sentFrame != null)
                    subResult.setSentBytes(sentFrame.getSize());
                if (includeFilteredFramesInSize) {
                    result.setHeadersSize(result.getHeadersSize() + receivedFrame.getSize() - receivedFrame.getPayloadSize());
                    result.setBodySize(result.getBodySize() + receivedFrame.getPayloadSize());
                    if (sentFrame != null)
                        result.setSentBytes(result.getSentBytes() + sentFrame.getSize());
                }
                if (receivedFrame.isText())
                    subResult.setResponseData(((TextFrame) receivedFrame).getText(), null);
                else if (receivedFrame.isBinary())
                    subResult.setResponseData(((BinaryFrame) receivedFrame).getBinaryData());

                result.addRawSubResult(subResult);
            }

            if (timeSpent < socketTimeout)
                socketTimeout -= timeSpent;
            else {
                // Time spent waiting for a valid frame (one that passed the filter) is now equal to original read timeout, so do not wait any longer.
                throw new SocketTimeoutException("Read timed out");
            }
        }
        while (matchesFilter);

        return receivedFrame;
    }

    protected void prepareFilter() {}

    abstract protected boolean matchesFilter(Frame receivedFrame);

    protected Frame performReplyAction(WebSocketClient wsClient, Frame receivedFrame) throws IOException {
        return null;
    }

    @Override
    public boolean expectsModification() {
        return false;
    }

    abstract protected Logger getLogger();

    @Override
    public String toString() {
        return "Frame Filter '" + getName() + "'";
    }

    static void initStaticFilterOptions() {
        includeFilteredFramesInSize = Boolean.parseBoolean(JMeterUtils.getPropDefault(RESULT_SIZE_INCLUDES_FILTERED_FRAMES, "false"));
    }
}
