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
package eu.luminis.websocket;

import org.apache.jmeter.protocol.http.control.Header;
import org.apache.jmeter.protocol.http.control.HeaderManager;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.mockito.stubbing.OngoingStubbing;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
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
            when(mockWsClient.getConnectUrl()).thenReturn(new URL("http://nowhere.com:80"));
            when(mockWsClient.connect(anyInt(), anyInt())).thenReturn(new WebSocketClient.HttpResult());
            when(mockWsClient.sendClose(anyInt(), anyString())).thenReturn(new CloseFrame(1000, "sampler requested close"));
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
            when(mockWsClient.sendTextFrame(anyString())).thenAnswer(new Answer<TextFrame>() {
                @Override
                public TextFrame answer(InvocationOnMock invocation) {
                    return new TextFrame(invocation.getArgument(0));
                }
            });
            when(mockWsClient.sendPingFrame()).thenReturn(new PingFrame(new byte[0]));
            when(mockWsClient.receiveText(anyInt())).thenAnswer(new Answer<TextFrame>(){
                @Override
                public TextFrame answer(InvocationOnMock invocation) throws Throwable {
                    Thread.sleep(300);
                    return new TextFrame("ws-response-data");
                }
            });
            when(mockWsClient.receiveFrame(anyInt())).thenAnswer(new Answer<TextFrame>(){
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
     * Creates (mock) WebSocketClient that returns the given frames.
     */
    public WebSocketClient createMultipleFrameClient(Frame[] frames) {
        try {
            WebSocketClient mockWsClient = Mockito.mock(WebSocketClient.class);
            when(mockWsClient.getConnectUrl()).thenReturn(new URL("http://nowhere.com:80"));
            when(mockWsClient.connect(anyInt(), anyInt())).thenReturn(new WebSocketClient.HttpResult());
            when(mockWsClient.sendPongFrame(any())).thenReturn(new PongFrame(new byte[0], 2));
            OngoingStubbing<Frame> when = when(mockWsClient.receiveFrame(anyInt()));
            for (Frame f: frames) {
                when = when.thenReturn(f);
            }
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
            when(mockWsClient.sendTextFrame(anyString())).thenAnswer(new Answer<TextFrame>() {
                @Override
                public TextFrame answer(InvocationOnMock invocation) {
                    return new TextFrame(invocation.getArgument(0));
                }
            });
            when(mockWsClient.receiveFrame(anyInt())).thenAnswer(new Answer<Frame>(){
                private int callCount = 0;

                @Override
                public Frame answer(InvocationOnMock invocation) throws Throwable {
                    String payload = "response " + callCount++;
                    return new TextFrame(true, payload.getBytes(), 2 + payload.getBytes().length);  // This is the constructor Frame.parseFrame would call...
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
            when(mockWsClient.sendClose(anyInt(), anyString())).thenReturn(new CloseFrame(1000, "sampler requested close"));
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
            when(mockWsClient.sendPingFrame()).thenReturn(new PingFrame(new byte[0]));
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

    /**
     * Creates (mock) WebSocketClient that writes its output (http request, frames sent) to the given output buffer
     * and reads the response (http response, frames received) from the given response.
     */
    public WebSocketClient createMockWebSocketClientWithResponse(ByteArrayOutputStream outputBuffer, byte[] response) throws MalformedURLException {
        return new WebSocketClient(new URL("http", "nowhere.com", 80, "/")) {
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

    /**
     * Creates (mock) WebSocketClient that writes its output (http request, frames sent) to the given output buffer
     * and has no response.
     */
    public WebSocketClient createMockWebSocketClientWithOutputBuffer(String host, int port, ByteArrayOutputStream outputBuffer) throws MalformedURLException {
        return new WebSocketClient(new URL("http", host, port, "/")) {
            protected Socket createSocket(String host, int port, int connectTimeout, int readTimeout) throws IOException {
                Socket socket = Mockito.mock(Socket.class);
                when(socket.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[0]));
                when(socket.getOutputStream()).thenReturn(outputBuffer);
                return socket;
            }
        };
    }

    /**
     * Creates (mock) WebSocketClient that, when receiveFrame is called, throws a SocketTimeoutException
     */
    public WebSocketClient createNoResponseWsClientMock() {
        try {
            WebSocketClient mockWsClient = Mockito.mock(WebSocketClient.class);
            when(mockWsClient.getConnectUrl()).thenReturn(new URL("http://nowhere.com:80"));
            when(mockWsClient.connect(anyInt(), anyInt())).thenReturn(new WebSocketClient.HttpResult());
            when(mockWsClient.receiveText(anyInt())).thenThrow(new SocketTimeoutException("timeout"));
            return mockWsClient;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates (mock) WebSocketClient that throws an EndOfStreamException when attempting to send a text or binary frame.
     */
    public WebSocketClient createErrorOnWriteWsClientMock() {
        try {
            WebSocketClient mockWsClient = Mockito.mock(WebSocketClient.class);
            when(mockWsClient.getConnectUrl()).thenReturn(new URL("http://nowhere.com:80"));
            when(mockWsClient.connect(anyInt(), anyInt())).thenReturn(new WebSocketClient.HttpResult());
            Mockito.doThrow(new EndOfStreamException("connection close")).when(mockWsClient).sendTextFrame(anyString());
            Mockito.doThrow(new EndOfStreamException("connection close")).when(mockWsClient).sendBinaryFrame(any());
            return mockWsClient;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates (mock) WebSocketClient that throws the given exception when attempting to send a text or binary frame.
     */
    public WebSocketClient createDefaultWsClientMock(Exception exception) {
        try {
            WebSocketClient mockWsClient = Mockito.mock(WebSocketClient.class);
            when(mockWsClient.getConnectUrl()).thenReturn(new URL("http://nowhere.com"));
            when(mockWsClient.connect(anyInt(), anyInt())).thenReturn(new WebSocketClient.HttpResult());
            when(mockWsClient.receiveText(anyInt())).thenAnswer(new Answer<String>(){
                @Override
                public String answer(InvocationOnMock invocation) throws Throwable {
                    Thread.sleep(300);
                    throw exception;
                }
            });
            when(mockWsClient.receiveFrame(anyInt())).thenAnswer(new Answer<String>(){
                @Override
                public String answer(InvocationOnMock invocation) throws Throwable {
                    Thread.sleep(300);
                    throw exception;
                }
            });
            return mockWsClient;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates a mock WebSocketClient that mocks the connection setup process. Only the send and receive methods
     * need to be mocked by the caller.
     * @return
     */
    public WebSocketClient createSimpleMock() {
        WebSocketClient mock = Mockito.mock(WebSocketClient.class);
        try {
            when(mock.connect(anyMap(), anyInt(), anyInt())).thenReturn(new WebSocketClient.HttpResult());
            when(mock.isConnected()).thenReturn(true);
            when(mock.getConnectUrl()).thenReturn(new URL("http://nowwhere.com"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return mock;
    }

}
