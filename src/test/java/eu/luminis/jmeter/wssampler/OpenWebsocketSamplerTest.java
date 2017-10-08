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
