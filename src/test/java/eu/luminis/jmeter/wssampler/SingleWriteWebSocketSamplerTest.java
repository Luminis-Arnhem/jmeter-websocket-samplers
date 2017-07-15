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

import eu.luminis.websocket.EndOfStreamException;
import eu.luminis.websocket.WebSocketClient;
import org.apache.jmeter.samplers.SampleResult;
import org.junit.Test;
import org.mockito.Mockito;

import java.net.URL;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

public class SingleWriteWebSocketSamplerTest {

    @Test
    public void partialTextWriteShouldShowRequestDataInResult() {
        SingleWriteWebSocketSampler sampler = new SingleWriteWebSocketSampler() {
            @Override
            protected WebSocketClient prepareWebSocketClient(SampleResult result) {
                return createErrorOnWriteWsClientMock();
            }
        };
        sampler.setBinary(false);
        sampler.setRequestData("goodbye");

        SampleResult result = sampler.sample(null);
        assertFalse(result.isSuccessful());
        assertTrue(result.getSamplerData().contains("Connect URL:\nws://nowhere.com"));
        assertTrue(result.getSamplerData().contains("Request data:\ngoodbye"));
    }

    @Test
    public void partialBinaryWriteShouldShowRequestDataInResult() {
        SingleWriteWebSocketSampler sampler = new SingleWriteWebSocketSampler() {
            @Override
            protected WebSocketClient prepareWebSocketClient(SampleResult result) {
                return createErrorOnWriteWsClientMock();
            }
        };
        sampler.setBinary(true);
        sampler.setRequestData("0xca 0xfe 0xba 0xbe");

        SampleResult result = sampler.sample(null);
        assertFalse(result.isSuccessful());
        assertTrue(result.getSamplerData().contains("Connect URL:\nws://nowhere.com"));
        assertTrue(result.getSamplerData().contains("Request data:\n0xca 0xfe 0xba 0xbe"));
    }

    WebSocketClient createErrorOnWriteWsClientMock() {
        try {
            WebSocketClient mockWsClient = Mockito.mock(WebSocketClient.class);
            when(mockWsClient.getConnectUrl()).thenReturn(new URL("http://nowhere.com:80"));
            Mockito.doThrow(new EndOfStreamException("connection close")).when(mockWsClient).sendTextFrame(anyString());
            Mockito.doThrow(new EndOfStreamException("connection close")).when(mockWsClient).sendBinaryFrame(any());
            return mockWsClient;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
