package eu.luminis.websocket;

import eu.luminis.websocket.*;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.util.Collections;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MockWebSocketClientCreator {

    /**
     * Creates (mock) WebSocketClient that just returns one frame when receiveFrame is called.
     * When receiveFrame is called multiple times, all but the first invocation will throw an EndOfStreamException.
     */
    public WebSocketClient createSingleFrameClient(Frame frame) {
        try {
            WebSocketClient mockWsClient = mock(WebSocketClient.class);
            when(mockWsClient.receiveFrame(anyInt()))
                    .thenReturn(frame)
                    .thenThrow(new EndOfStreamException("end of stream"));
            return mockWsClient;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates (mock) WebSocketClient that just returns some text when receiveText is called.
     */
    public WebSocketClient createTextReceiverClient() {
        try {
            WebSocketClient mockWsClient = mock(WebSocketClient.class);
            when(mockWsClient.getConnectUrl()).thenReturn(new URL("http://nowhere.com:80"));
            when(mockWsClient.connect(anyInt(), anyInt())).thenReturn(new WebSocketClient.HttpResult());
            when(mockWsClient.receiveText(anyInt())).thenAnswer(new Answer<TextFrame>(){
                @Override
                public TextFrame answer(InvocationOnMock invocation) throws Throwable {
                    Thread.sleep(300);
                    return new TextFrame("ws-response-data");
                }
            });
            return mockWsClient;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates (mock) WebSocketClient that returns different message for each call of receiveFrame: "response x",
     * where x is a increasing number.
     */
    public WebSocketClient createMultipleTextReceivingClient() {
        try {
            WebSocketClient mockWsClient = Mockito.mock(WebSocketClient.class);
            when(mockWsClient.getConnectUrl()).thenReturn(new URL("http://nowhere.com:80"));
            when(mockWsClient.connect(anyInt(), anyInt())).thenReturn(new WebSocketClient.HttpResult());
            when(mockWsClient.receiveFrame(anyInt())).thenAnswer(new Answer<Frame>(){
                private int callCount = 0;

                @Override
                public Frame answer(InvocationOnMock invocation) throws Throwable {
                    return new TextFrame("response " + callCount++);
                }
            });
            return mockWsClient;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates (mock) WebSocketClient that, when receiveFrame is called, returns a number of text frames ended by a close frame.
     */
    public WebSocketClient createMultipleTextFollowedByCloseClient(int numberOfTextFrames) {
        try {
            WebSocketClient mockWsClient = Mockito.mock(WebSocketClient.class);
            when(mockWsClient.getConnectUrl()).thenReturn(new URL("http://nowhere.com:80"));
            when(mockWsClient.connect(anyInt(), anyInt())).thenReturn(new WebSocketClient.HttpResult());
            when(mockWsClient.receiveFrame(anyInt())).thenAnswer(new Answer<Frame>(){
                private int callCount = 0;

                @Override
                public Frame answer(InvocationOnMock invocation) throws Throwable {
                    if (callCount < numberOfTextFrames)
                        return new TextFrame("response " + callCount++);
                    else
                        return new CloseFrame(1001, "bye");
                }
            });
            return mockWsClient;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates (mock) WebSocketClient that, when receiveFrame is called, returns a given number of text frames and one pong frame.
     */
    public WebSocketClient createTextFollowedByPongFrameReturningClient(int numberOfTextFrames) {
        try {
            WebSocketClient mockWsClient = Mockito.mock(WebSocketClient.class);
            when(mockWsClient.getConnectUrl()).thenReturn(new URL("http://nowhere.com:80"));
            when(mockWsClient.connect(anyInt(), anyInt())).thenReturn(new WebSocketClient.HttpResult());
            when(mockWsClient.receiveFrame(anyInt())).thenAnswer(new Answer<Frame>(){
                private int callCount = 0;

                @Override
                public Frame answer(InvocationOnMock invocation) throws Throwable {
                    if (callCount < numberOfTextFrames)
                        return new TextFrame("response " + callCount++);
                    else
                        return new PongFrame(new byte[0]);
                }
            });
            return mockWsClient;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public WebSocketClient createMockWebSocketClientWithResponse(String host, int port, ByteArrayOutputStream outputBuffer, byte[] response) throws MalformedURLException {
        return new WebSocketClient(new URL("http", host, port, "/")) {
            protected Socket createSocket(String host, int port, int connectTimeout, int readTimeout) throws IOException {
                Socket socket = Mockito.mock(Socket.class);
                when(socket.getInputStream()).thenReturn(new ByteArrayInputStream(response));
                when(socket.getOutputStream()).thenReturn(outputBuffer);
                return socket;
            }

            @Override
            protected Map<String, String> checkServerResponse(InputStream inputStream, String nonce) throws IOException {
                try {
                    super.checkServerResponse(inputStream, nonce);
                }
                catch (HttpUpgradeException ignore) {}
                return Collections.emptyMap();
            }
        };
    }


}
