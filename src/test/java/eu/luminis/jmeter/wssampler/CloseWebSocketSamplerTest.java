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
import eu.luminis.websocket.MockWebSocketClientCreator;
import eu.luminis.websocket.WebSocketClient;
import org.apache.jmeter.samplers.SampleResult;
import org.junit.Test;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;

public class CloseWebSocketSamplerTest {

    MockWebSocketClientCreator mocker = new MockWebSocketClientCreator();

    @Test
    public void testFrameFilter() {
        CloseWebSocketSampler sampler = new CloseWebSocketSampler() {
            @Override
            protected WebSocketClient prepareWebSocketClient(SampleResult result) {
                return mocker.createMultipleTextFollowedByCloseClient(3);
            }
        };
        TextFrameFilter filter = new TextFrameFilter();
        filter.setComparisonType(ComparisonType.NotStartsWith);
        filter.setMatchValue("response 7");
        sampler.addTestElement(filter);

        SampleResult result = sampler.sample(null);
        assertTrue(result.isSuccessful());
        assertEquals("1001: bye", result.getResponseDataAsString());
        assertEquals(3, result.getSubResults().length);
    }

    @Test
    public void sendingFrameShouldSetResultSentSize() {
        CloseWebSocketSampler sampler = new CloseWebSocketSampler() {
            @Override
            protected WebSocketClient prepareWebSocketClient(SampleResult result) {
                return mocker.createSingleFrameClient(new CloseFrame());
            }
        };

        SampleResult result = sampler.sample(null);
        assertTrue(result.isSuccessful());
        assertEquals(0 + 6 + 2 + 23, result.getSentBytes());  // 0: no http header (because of mock); 6: frame overhead (client mask = 4 byte); 7: payload
    }
}
