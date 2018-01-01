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

import eu.luminis.websocket.MockWebSocketClientCreator;
import eu.luminis.websocket.WebSocketClient;
import org.apache.jmeter.samplers.SampleResult;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.net.MalformedURLException;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;

public class OpenWebsocketSamplerTest {

    MockWebSocketClientCreator mocker = new MockWebSocketClientCreator();

    @Test
    public void openingConnectionShouldSetResultSentSize() {
        String serverResponse = "HTTP/1.1 101 Switching Protocols\r\nUpgrade: websocket\r\nConnection: Upgrade\r\n\r\n";
        ByteArrayOutputStream outputBuffer = new ByteArrayOutputStream();
        OpenWebSocketSampler sampler = new OpenWebSocketSampler() {
            @Override
            protected WebSocketClient prepareWebSocketClient(SampleResult result) {
                try {
                    return mocker.createMockWebSocketClientWithResponse(outputBuffer, serverResponse.getBytes());
                } catch (MalformedURLException e) {
                    throw new RuntimeException();
                }
            }
        };

        SampleResult result = sampler.sample(null);
        assertTrue(result.isSuccessful());
        assertEquals(outputBuffer.size(), result.getSentBytes());
    }


}
