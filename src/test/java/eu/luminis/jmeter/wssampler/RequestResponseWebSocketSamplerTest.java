package eu.luminis.jmeter.wssampler;

import eu.luminis.websocket.WebSocketClient;
import org.apache.jmeter.protocol.http.control.Header;
import org.apache.jmeter.protocol.http.control.HeaderManager;
import org.apache.jmeter.samplers.SampleResult;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.net.URL;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

public class RequestResponseWebSocketSamplerTest {

    @Test
    public void testNormalRequestResponseSamplerSample() throws Exception {

        RequestResponseWebSocketSampler sampler = new RequestResponseWebSocketSampler() {
            @Override
            protected WebSocketClient prepareWebSocketClient(SampleResult result) {
                return createDefaultWsClientMock();
            }
        };

        SampleResult result = sampler.sample(null);
        assertTrue(result.getTime() > 300);
        assertTrue(result.getTime() < 400);  // A bit tricky of course, but on decent computers the call should not take more than 100 ms....
        assertEquals("ws-response-data", result.getResponseDataAsString());
    }

    @Test
    public void testSamplerThatReusesConnectionShouldntReportHeaders() throws Exception {

        WebSocketClient mockWsClient = createDefaultWsClientMock();
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




    WebSocketClient createDefaultWsClientMock() {
        try {
            WebSocketClient mockWsClient = Mockito.mock(WebSocketClient.class);
            when(mockWsClient.getConnectUrl()).thenReturn(new URL("http://nowhere.com"));
            //when(mockWsClient.receiveText(anyInt())).thenReturn("ws-response-data");
            when(mockWsClient.receiveText(anyInt())).thenAnswer(new Answer<String>(){
                @Override
                public String answer(InvocationOnMock invocation) throws Throwable {
                    Thread.sleep(300);
                    return "ws-response-data";
                }
            });
            return mockWsClient;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    HeaderManager createSingleHeaderHeaderManager() {
        HeaderManager headerMgr = Mockito.mock(HeaderManager.class);
        when(headerMgr.size()).thenReturn(1);
        when(headerMgr.get(0)).thenReturn(new Header("header-key", "header-value"));
        return headerMgr;
    }
}