package eu.luminis.jmeter.wssampler;

import eu.luminis.websocket.EndOfStreamException;
import eu.luminis.websocket.Frame;
import eu.luminis.websocket.WebSocketClient;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.IOException;
import java.net.URL;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MockWebSocketClientCreator {

    /**
     * Creates (mock) WebSocketClient that just returns one frame when receiveFrame is called.
     * When receiveFrame is called multiple times, all but the first invocation will throw an EndOfStreamException.
     */
    public WebSocketClient createSingleFrameClient(Frame frame) throws IOException {
        WebSocketClient mockWsClient = mock(WebSocketClient.class);
        when(mockWsClient.receiveFrame(anyInt()))
                .thenReturn(frame)
                .thenThrow(new EndOfStreamException("end of stream"));
        return mockWsClient;
    }

    /**
     * Creates (mock) WebSocketClient that just returns some text when receiveText is called.
     */
    public WebSocketClient createTextReceiverClient() {
        try {
            WebSocketClient mockWsClient = mock(WebSocketClient.class);
            when(mockWsClient.getConnectUrl()).thenReturn(new URL("http://nowhere.com:80"));
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


}
