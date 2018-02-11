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

import eu.luminis.websocket.BinaryFrame;
import eu.luminis.websocket.Frame;
import eu.luminis.websocket.WebSocketClient;
import junit.framework.AssertionFailedError;
import org.apache.jmeter.samplers.SampleResult;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.URL;


public class FrameFilterTest {

    @Test
    public void receiveOnFilterWillNotLastMuchLongerThanTimeout() throws IOException {
        FrameFilter filter = new BinaryFrameFilter();

        WebSocketClient wsClientStub = new WebSocketClient(new URL("http://whatever")) {
            @Override
            public Frame receiveFrame(int readTimeout) throws IOException {
                try {
                    int waitTime = Math.min(100, readTimeout);
                    if (waitTime == 0) {
                        // Socket timeout of 0 means infinite
                        throw new AssertionFailedError("Would block forever");
                    }
                    else if (waitTime < 0) {
                        throw new IllegalArgumentException("timeout cannot < 0");
                    }
                    Thread.sleep(waitTime);
                } catch (InterruptedException e) {}

                return new BinaryFrame(new byte[0]);
            }
        };

        int readTimeout = 250;
        SampleResult result = new SampleResult();
        long start = System.currentTimeMillis();
        try {
            // Filter will filter all frames and thus never return a frame.
            // Eventually, the readTimeout used in the filter will drop to zero (or negative) and the filter should throw a SocketTimeout
            filter.receiveFrame(wsClientStub, readTimeout, result);
        }
        catch (SocketTimeoutException timeout) {
            // expected
        }
        long duration = System.currentTimeMillis() - start;
        // Should have three filtered frames: two after 100 ms and the last after +- 50 ms.
        Assert.assertEquals(3, result.getSubResults().length);
        Assert.assertTrue(duration >= readTimeout);
        Assert.assertTrue(duration - readTimeout < 50);  // Risky, but reasonable.
    }
}
