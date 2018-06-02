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
import org.apache.jmeter.protocol.http.control.Header;
import org.apache.jmeter.protocol.http.control.HeaderManager;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.*;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.nio.file.Files;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.offset;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RequestResponseWebSocketSamplerTest {

    MockWebSocketClientCreator mocker = new MockWebSocketClientCreator();

    @BeforeClass
    public static void initJMeterContext() {
        JMeterContextService.getContext().setVariables(new JMeterVariables());
    }

    @Test
    public void testNormalRequestResponseSamplerSample() throws Exception {

        RequestResponseWebSocketSampler sampler = new RequestResponseWebSocketSampler() {
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
    }

    @Test
    public void testSamplerThatReusesConnectionShouldntReportHeaders() throws Exception {

        WebSocketClient mockWsClient = mocker.createTextReceiverClient();
        when(mockWsClient.isConnected()).thenReturn(true);

        RequestResponseWebSocketSampler sampler = new RequestResponseWebSocketSampler() {
            @Override
            protected WebSocketClient prepareWebSocketClient(SampleResult result) {
                return mockWsClient;
            }
        };
        sampler.headerManager = createSingleHeaderHeaderManager();

        SampleResult result = sampler.sample(null);
        assertTrue(result.getRequestHeaders().isEmpty());
    }

    @Test
    public void testFailingUpgradeRequest() throws Exception {

        WebSocketClient mockWsClient = mocker.createTextReceiverClient();
        when(mockWsClient.connect(anyInt(), anyInt())).thenThrow(new HttpUpgradeException(404));

        RequestResponseWebSocketSampler sampler = new RequestResponseWebSocketSampler() {
            @Override
            protected WebSocketClient prepareWebSocketClient(SampleResult result) {
                return mockWsClient;
            }
        };

        SampleResult result = sampler.sample(null);
        assertFalse(result.isSuccessful());
        assertTrue(result.getSamplerData().contains("Connect URL:\nws://nowhere.com"));
    }

    @Test
    public void testFrameFilter() {
        RequestResponseWebSocketSampler sampler = new RequestResponseWebSocketSampler() {
            @Override
            protected WebSocketClient prepareWebSocketClient(SampleResult result) {
                return mocker.createMultipleTextReceivingClient();
            }
        };
        TextFrameFilter filter = new TextFrameFilter();
        filter.setComparisonType(ComparisonType.EqualsRegex);
        filter.setMatchValue("response \\d");
        sampler.addTestElement(filter);
        SampleResult result = sampler.sample(null);
        assertTrue(result.isSuccessful());
        assertEquals("response 10", result.getResponseDataAsString());
        assertEquals(10, result.getSubResults().length);
    }

    @Test
    public void shouldSupportMultipleFilters() {
        RequestResponseWebSocketSampler sampler = new RequestResponseWebSocketSampler() {
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

    @Test
    public void samplerResultShouldContainConnectInfoAndRequestData() {
        RequestResponseWebSocketSampler sampler = new RequestResponseWebSocketSampler() {
            @Override
            protected WebSocketClient prepareWebSocketClient(SampleResult result) {
                return mocker.createTextReceiverClient();
            }
        };
        sampler.setRequestData("goodbye");

        SampleResult result = sampler.sample(null);
        assertTrue(result.isSuccessful());
        assertTrue(result.getSamplerData().contains("Connect URL:\nws://nowhere.com"));
        assertTrue(result.getSamplerData().contains("Request data:\ngoodbye"));
    }

    @Test
    public void readTimeoutShouldShowRequestDataInResult() {
        RequestResponseWebSocketSampler sampler = new RequestResponseWebSocketSampler() {
            @Override
            protected WebSocketClient prepareWebSocketClient(SampleResult result) {
                return mocker.createNoResponseWsClientMock();
            }
        };
        sampler.setRequestData("goodbye");

        SampleResult result = sampler.sample(null);
        assertFalse(result.isSuccessful());
        assertTrue(result.getSamplerData().contains("Request data:\ngoodbye"));
    }

    @Test
    public void failingToSendTextMessageShouldShowRequestDataInResult() {
        RequestResponseWebSocketSampler sampler = new RequestResponseWebSocketSampler() {
            @Override
            protected WebSocketClient prepareWebSocketClient(SampleResult result) {
                return mocker.createErrorOnWriteWsClientMock();
            }
        };
        sampler.setBinary(false);
        sampler.setRequestData("goodbye");

        SampleResult result = sampler.sample(null);
        assertFalse(result.isSuccessful());
        assertTrue(result.getSamplerData().contains("Request data:\ngoodbye"));
    }

    @Test
    public void failingToSendBinaryMessageShouldShowRequestDataInResult() {
        RequestResponseWebSocketSampler sampler = new RequestResponseWebSocketSampler() {
            @Override
            protected WebSocketClient prepareWebSocketClient(SampleResult result) {
                return mocker.createErrorOnWriteWsClientMock();
            }
        };
        sampler.setBinary(true);
        sampler.setRequestData("0xba 0xbe");

        SampleResult result = sampler.sample(null);
        assertFalse(result.isSuccessful());
        assertTrue(result.getSamplerData().contains("Request data:\n0xba 0xbe"));
    }

    @Test
    public void sendingFrameShouldSetResultSentSize() {
        RequestResponseWebSocketSampler sampler = new RequestResponseWebSocketSampler() {
            @Override
            protected WebSocketClient prepareWebSocketClient(SampleResult result) {
                return mocker.createTextReceiverClient();
            }
        };
        sampler.setRequestData("1234567");

        SampleResult result = sampler.sample(null);
        assertTrue(result.isSuccessful());
        assertEquals(0 + 6 + 7, result.getSentBytes());  // 0: no http header (because of mock); 6: frame overhead (client mask = 4 byte); 7: payload
    }

    @Test
    public void fileOptionShouldReadPayloadFromFile() throws Exception {
        WebSocketClient mock = mocker.createSimpleMock();
        when(mock.sendTextFrame(anyString())).thenReturn(new TextFrame("whatever"));
        when(mock.receiveText(anyInt())).thenReturn(new TextFrame("whatever"));

        RequestResponseWebSocketSampler sampler = new RequestResponseWebSocketSampler() {
            @Override
            protected WebSocketClient prepareWebSocketClient(SampleResult result) {
                return mock;
            }
        };

        File dataFile = File.createTempFile("data", "txt");
        dataFile.deleteOnExit();
        DataOutputStream dataOut = new DataOutputStream(new FileOutputStream(dataFile));
        dataOut.writeBytes("this is a file with text content");
        dataOut.close();
        sampler.setDataFile(dataFile.getAbsolutePath());
        sampler.setRequestData("0x11 0x22");
        sampler.setLoadDataFromFile(true);

        SampleResult result = sampler.sample(null);
        verify(mock).sendTextFrame("this is a file with text content");
    }

    @Test
    public void whenFileOptionNotSetDoNotReadPayloadFromFile() throws Exception {
        WebSocketClient mock = mocker.createSimpleMock();
        when(mock.sendTextFrame(anyString())).thenReturn(new TextFrame("whatever"));
        when(mock.receiveText(anyInt())).thenReturn(new TextFrame("whatever"));

        RequestResponseWebSocketSampler sampler = new RequestResponseWebSocketSampler() {
            @Override
            protected WebSocketClient prepareWebSocketClient(SampleResult result) {
                return mock;
            }
        };

        File dataFile = File.createTempFile("data", "txt");
        dataFile.deleteOnExit();
        DataOutputStream dataOut = new DataOutputStream(new FileOutputStream(dataFile));
        dataOut.writeBytes("this is a file with text content");
        dataOut.close();
        sampler.setDataFile(dataFile.getAbsolutePath());
        sampler.setRequestData("sampler data");
        sampler.setLoadDataFromFile(false);  // Intentionally set to false

        SampleResult result = sampler.sample(null);
        verify(mock).sendTextFrame("sampler data");
    }

    @Test
    public void fileOptionShouldReadBinaryPayloadFromFile() throws Exception {
        WebSocketClient mock = mocker.createSimpleMock();
        when(mock.sendBinaryFrame(any(byte[].class))).thenReturn(new BinaryFrame(new byte[0]));
        when(mock.receiveBinaryData(anyInt())).thenReturn(new BinaryFrame(new byte[0]));

        RequestResponseWebSocketSampler sampler = new RequestResponseWebSocketSampler() {
            @Override
            protected WebSocketClient prepareWebSocketClient(SampleResult result) {
                return mock;
            }
        };
        sampler.setBinary(true);

        File dataFile = File.createTempFile("data", ".bin");
        dataFile.deleteOnExit();
        DataOutputStream dataOut = new DataOutputStream(new FileOutputStream(dataFile));
        dataOut.writeBytes("this is a file with binary content (really!)");
        dataOut.close();
        sampler.setDataFile(dataFile.getAbsolutePath());
        sampler.setRequestData("0x11 0x22");
        sampler.setLoadDataFromFile(true);

        SampleResult result = sampler.sample(null);
        verify(mock).sendBinaryFrame("this is a file with binary content (really!)".getBytes());
    }


    /**
     * Creates a JMeter HeaderManager that provides exactly one (dummy) header.
     */
    HeaderManager createSingleHeaderHeaderManager() {
        HeaderManager headerMgr = Mockito.mock(HeaderManager.class);
        when(headerMgr.size()).thenReturn(1);
        when(headerMgr.get(0)).thenReturn(new Header("header-key", "header-value"));
        return headerMgr;
    }

}