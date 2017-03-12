package eu.luminis.jmeter.wssampler;

import eu.luminis.websocket.EndOfStreamException;
import eu.luminis.websocket.Frame;
import eu.luminis.websocket.WebSocketClient;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FrameTest {

    WebSocketClient singleFrameClient(Frame frame) throws IOException {
        WebSocketClient mockWsClient = mock(WebSocketClient.class);
        when(mockWsClient.receiveFrame(anyInt()))
                .thenReturn(frame)
                .thenThrow(new EndOfStreamException("end of stream"));
        return mockWsClient;
    }
}
