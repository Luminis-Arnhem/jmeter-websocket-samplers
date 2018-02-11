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
import eu.luminis.websocket.PongFrame;
import eu.luminis.websocket.WebSocketClient;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.junit.Test;

import java.net.SocketTimeoutException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class PingPongSamplerTest {

    MockWebSocketClientCreator mocker = new MockWebSocketClientCreator();

    @Test
    public void samplerShouldSendPingAndReceivePong() throws Exception {
        WebSocketClient webSocketClient = mocker.createTextReceiverClient();

        PingPongSampler sampler = new PingPongSampler() {
            @Override
            protected WebSocketClient prepareWebSocketClient(SampleResult result) {
                return webSocketClient;
            }
        };
        sampler.setReadTimeout("500");

        SampleResult result = sampler.sample(new Entry());

        assertThat(result.isSuccessful()).isTrue();
        verify(webSocketClient).sendPingFrame();
        verify(webSocketClient).receivePong(500);
    }

    @Test
    public void samplerShouldFailWhenNoPongReceived() throws Exception {
        WebSocketClient webSocketClient = mocker.createTextReceiverClient();

        PingPongSampler sampler = new PingPongSampler() {
            @Override
            protected WebSocketClient prepareWebSocketClient(SampleResult result) {
                return webSocketClient;
            }
        };
        sampler.setReadTimeout("500");
        when(webSocketClient.receivePong(anyInt())).thenThrow(new SocketTimeoutException());

        SampleResult result = sampler.sample(new Entry());

        assertThat(result.isSuccessful()).isFalse();
        verify(webSocketClient).sendPingFrame();
        verify(webSocketClient).receivePong(500);
    }

    @Test
    public void testFrameFilter() {
        PingPongSampler sampler = new PingPongSampler() {
            @Override
            protected WebSocketClient prepareWebSocketClient(SampleResult result) {
                return mocker.createTextFollowedByPongFrameReturningClient(1);
            }
        };

        TextFrameFilter filter = new TextFrameFilter();
        filter.setComparisonType(ComparisonType.IsPlain);
        sampler.addTestElement(filter);

        SampleResult result = sampler.sample(null);
        assertThat(result.getResponseDataAsString()).isEmpty();
        assertThat(result.getSubResults().length).isEqualTo(1);
    }

    @Test
    public void shouldSupportMultipleFilters() {
        PingPongSampler sampler = new PingPongSampler() {
            @Override
            protected WebSocketClient prepareWebSocketClient(SampleResult result) {
                return mocker.createTextFollowedByPongFrameReturningClient(3);
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
        assertThat(result.getSubResults().length).isEqualTo(3);
    }

    @Test
    public void sendingFrameShouldSetResultSentSize() {
        PingPongSampler sampler = new PingPongSampler() {
            @Override
            protected WebSocketClient prepareWebSocketClient(SampleResult result) {
                return mocker.createTextReceiverClient();
            }
        };

        SampleResult result = sampler.sample(null);
        assertThat(result.isSuccessful()).isTrue();
        assertThat(result.getSentBytes()).isEqualTo(0 + 6 + 0);  // 0: no http header (because of mock); 6: frame overhead (client mask = 4 byte); 7: payload
    }

    @Test
    public void samplerOfTypePongShouldJustSendPongFrame() throws Exception {
        WebSocketClient webSocketClient = mocker.createTextReceiverClient();
        when(webSocketClient.sendPongFrame()).thenReturn(new PongFrame(new byte[0]));

        PingPongSampler sampler = new PingPongSampler() {
            @Override
            protected WebSocketClient prepareWebSocketClient(SampleResult result) {
                return webSocketClient;
            }
        };
        sampler.setType(PingPongSampler.Type.Pong);

        SampleResult result = sampler.sample(new Entry());

        assertThat(result.isSuccessful()).isTrue();
        verify(webSocketClient).sendPongFrame();
        verify(webSocketClient, never()).sendPingFrame();
        verify(webSocketClient, never()).receivePong(anyInt());
    }

}
