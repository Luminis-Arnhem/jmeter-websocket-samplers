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

import eu.luminis.websocket.EndOfStreamException;
import eu.luminis.websocket.MockWebSocketClientCreator;
import eu.luminis.websocket.WebSocketClient;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.net.SocketTimeoutException;
import java.net.URL;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

public class SingleReadWebSocketSamplerTest {

    MockWebSocketClientCreator mocker = new MockWebSocketClientCreator();

    @BeforeClass
    public static void initJMeterContext() {
        JMeterContextService.getContext().setVariables(new JMeterVariables());
    }

    @Test
    public void testSingleReadSamplerSample() throws Exception {

        SingleReadWebSocketSampler sampler = new SingleReadWebSocketSampler() {
            @Override
            protected WebSocketClient prepareWebSocketClient(SampleResult result) {
                return mocker.createTextReceiverClient();
            }
        };

        SampleResult result = sampler.sample(null);
        assertTrue(result.isSuccessful());
        assertTrue(result.getTime() >= 300);
        assertTrue(result.getTime() < 400);  // A bit tricky of course, but on decent computers the call should not take more than 100 ms....
        assertEquals("ws-response-data", result.getResponseDataAsString());
        assertFalse(result.getSamplerData().contains("Request data:"));
        assertFalse(result.getSamplerData().contains("ws-response-data"));
    }

    @Test
    public void readTimeoutLeadsToUnsccessfulResult() {
        SingleReadWebSocketSampler sampler = new SingleReadWebSocketSampler() {
            @Override
            protected WebSocketClient prepareWebSocketClient(SampleResult result) {
                return mocker.createDefaultWsClientMock(new SocketTimeoutException("Read timed out"));
            }
        };

        SampleResult result = sampler.sample(null);
        assertTrue(result.getTime() >= 300);
        assertTrue(result.getTime() < 400);
        assertFalse(result.isSuccessful());
    }

    @Test
    public void lostConnectionLeadsToUnsuccessfulResult() {
        SingleReadWebSocketSampler sampler = new SingleReadWebSocketSampler() {
            @Override
            protected WebSocketClient prepareWebSocketClient(SampleResult result) {
                return mocker.createDefaultWsClientMock(new EndOfStreamException("end of stream"));
            }
        };

        SampleResult result = sampler.sample(null);
        assertTrue(result.getTime() >= 300);
        assertTrue(result.getTime() < 400);
        assertFalse(result.isSuccessful());
    }

    @Test
    public void optionalReadShouldBeSuccessfulOnReadTimeout() {
        SingleReadWebSocketSampler sampler = new SingleReadWebSocketSampler() {
            @Override
            protected WebSocketClient prepareWebSocketClient(SampleResult result) {
                return mocker.createDefaultWsClientMock(new SocketTimeoutException("Read timed out"));
            }
        };
        sampler.setOptional(true);

        SampleResult result = sampler.sample(null);
        assertTrue(result.isSuccessful());
        assertTrue(result.getTime() >= 300);
        assertTrue(result.getTime() < 400);
        assertTrue(result.isSuccessful());
        assertEquals("No response", result.getResponseCode());
        assertEquals("Read timeout, no response received.", result.getResponseMessage());
    }

    @Test
    public void lostConnectionOnOptionalReadShouldResultInUnsuccessfulResult() {
        SingleReadWebSocketSampler sampler = new SingleReadWebSocketSampler() {
            @Override
            protected WebSocketClient prepareWebSocketClient(SampleResult result) {
                return mocker.createDefaultWsClientMock(new EndOfStreamException("end of stream"));
            }
        };
        sampler.setOptional(true);

        SampleResult result = sampler.sample(null);
        assertTrue(result.getTime() >= 300);
        assertTrue(result.getTime() < 400);
        assertFalse(result.isSuccessful());
    }

    @Test
    public void testFrameFilter() {
        SingleReadWebSocketSampler sampler = new SingleReadWebSocketSampler() {
            @Override
            protected WebSocketClient prepareWebSocketClient(SampleResult result) {
                return mocker.createMultipleTextReceivingClient();
            }
        };
        TextFrameFilter filter = new TextFrameFilter();
        filter.setComparisonType(ComparisonType.NotStartsWith);
        filter.setMatchValue("response 7");
        sampler.addTestElement(filter);

        SampleResult result = sampler.sample(null);
        assertTrue(result.isSuccessful());
        assertEquals("response 7", result.getResponseDataAsString());
        assertEquals(7, result.getSubResults().length);
    }

    @Test
    public void shouldSupportMultipleFilters() {
        SingleReadWebSocketSampler sampler = new SingleReadWebSocketSampler() {
            @Override
            protected WebSocketClient prepareWebSocketClient(SampleResult result) {
                return mocker.createMultipleTextReceivingClient();
            }
        };

        TextFrameFilter filter0 = new TextFrameFilter();
        filter0.setComparisonType(ComparisonType.Contains);
        filter0.setMatchValue("0");
        TextFrameFilter filter1 = new TextFrameFilter();
        filter1.setComparisonType(ComparisonType.Contains);
        filter1.setMatchValue("1");
        TextFrameFilter filter2 = new TextFrameFilter();
        filter2.setComparisonType(ComparisonType.Contains);
        filter2.setMatchValue("2");

        sampler.addTestElement(filter0);
        sampler.addTestElement(filter1);
        sampler.addTestElement(filter2);

        SampleResult result = sampler.sample(null);
        assertTrue(result.isSuccessful());
        assertEquals("response 3", result.getResponseDataAsString());
        assertEquals(3, result.getSubResults().length);
    }

}