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
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jmeter.util.JMeterUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.AdditionalMatchers;
import org.mockito.ArgumentCaptor;

import java.io.IOException;
import java.nio.file.Files;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

public class PingFrameFilterTest {

    @BeforeClass
    public static void initJMeterContext() {
        JMeterContextService.getContext().setVariables(new JMeterVariables());
    }

    private MockWebSocketClientCreator mocker = new MockWebSocketClientCreator();

    @Before
    public void setUp() throws IOException {
        JMeterUtils.loadJMeterProperties(Files.createTempFile("empty", ".props").toString());
        FrameFilter.initStaticFilterOptions();
    }

    @Test
    public void filterShouldDropPingFrame() throws IOException {
        mocker = new MockWebSocketClientCreator();
        WebSocketClient mockWsClient = mocker.createSingleFrameClient(new PingFrame(new byte[0]));
        assertThatExceptionOfType(EndOfStreamException.class).isThrownBy(() -> {
            new PingFrameFilter().receiveFrame(mockWsClient, 1000, new SampleResult());
        });
    }

    @Test
    public void filterShouldDropPongFrame() throws IOException {
        mocker = new MockWebSocketClientCreator();
        WebSocketClient mockWsClient = mocker.createSingleFrameClient(new PongFrame(new byte[0]));
        assertThatExceptionOfType(EndOfStreamException.class).isThrownBy(() -> {
            new PingFrameFilter().receiveFrame(mockWsClient, 1000, new SampleResult());
        });
    }

    @Test
    public void filterShouldNotDropTextFrames() throws IOException {
        WebSocketClient mockWsClient = new MockWebSocketClientCreator().createMultipleFrameClient(new Frame[] {
                new TextFrame("whatever"),
                new PingFrame(new byte[0]),
                new TextFrame("last frame")
        });

        Frame receivedFrame = new PingFrameFilter().receiveFrame(mockWsClient, 1000, new SampleResult());
        assertTrue(receivedFrame.isText());
        assertTrue(receivedFrame.isText());
    }

    @Test
    public void filterShouldAddSubResultForEachFilteredFrame() throws IOException {

        WebSocketClient mockWsClient = new MockWebSocketClientCreator().createMultipleFrameClient(new Frame[] {
                new TextFrame("whatever"),
                new PingFrame(new byte[0]),
                new PingFrame(new byte[0]),
                new TextFrame("last frame")
        });
        SampleResult result = new SampleResult();
        FrameFilter filter = new PingFrameFilter();
        assertTrue(filter.receiveFrame(mockWsClient, 1000, result).isText());
        assertTrue(filter.receiveFrame(mockWsClient, 1000, result).isText());

        assertEquals(2, result.getSubResults().length);
    }

    @Test
    public void filterShouldCountSentBytes() {
        SingleReadWebSocketSampler sampler = new SingleReadWebSocketSampler() {
            @Override
            protected WebSocketClient prepareWebSocketClient(SampleResult result) {
                return mocker.createMultipleFrameClient(new Frame[] { new PingFrame(new byte[0]), new TextFrame("whatever") });
            }
        };
        PingFrameFilter filter = new PingFrameFilter();
        filter.setReplyToPing(true);
        JMeterUtils.setProperty("websocket.result.size_includes_filtered_frames", "true");
        filter.initStaticFilterOptions();
        sampler.addTestElement(filter);

        SampleResult result = sampler.sample(null);

        assertTrue(result.isSuccessful());
        assertEquals(1, result.getSubResults().length);
        assertEquals(2, result.getSubResults()[0].getSentBytes());
        assertEquals(2, result.getSentBytes());
    }

    @Test
    public void filterPingOnlyShouldDropPingFrame() throws IOException {
        WebSocketClient mockWsClient = new MockWebSocketClientCreator().createMultipleFrameClient(new Frame[] {
                new TextFrame("whatever"),
                new PingFrame(new byte[0]),
                new TextFrame("last frame")
        });
        PingFrameFilter filter = new PingFrameFilter();
        filter.setFilterType(PingFrameFilter.PingFilterType.FilterPingOnly);

        SampleResult result = new SampleResult();

        assertThat(filter.receiveFrame(mockWsClient, 1000, result).isText());
        assertThat(filter.receiveFrame(mockWsClient, 1000, result).isText());
    }

    @Test
    public void filterPingOnlyShouldKeepPongFrame() throws IOException {
        WebSocketClient mockWsClient = new MockWebSocketClientCreator().createMultipleFrameClient(new Frame[] {
                new TextFrame("whatever"),
                new PongFrame(new byte[0]),
                new TextFrame("last frame")
        });
        PingFrameFilter filter = new PingFrameFilter();
        filter.setFilterType(PingFrameFilter.PingFilterType.FilterPingOnly);

        SampleResult result = new SampleResult();

        assertThat(filter.receiveFrame(mockWsClient, 1000, result).isText());
        assertThat(filter.receiveFrame(mockWsClient, 1000, result).isPong());
        assertThat(filter.receiveFrame(mockWsClient, 1000, result).isText());
    }

    @Test
    public void filterPongOnlyShouldDropPongFrame() throws IOException {
        WebSocketClient mockWsClient = new MockWebSocketClientCreator().createMultipleFrameClient(new Frame[] {
                new TextFrame("whatever"),
                new PongFrame(new byte[0]),
                new TextFrame("last frame")
        });
        PingFrameFilter filter = new PingFrameFilter();
        filter.setFilterType(PingFrameFilter.PingFilterType.FilterPongOnly);

        SampleResult result = new SampleResult();

        assertThat(filter.receiveFrame(mockWsClient, 1000, result).isText());
        assertThat(filter.receiveFrame(mockWsClient, 1000, result).isText());
    }

    @Test
    public void filterPongOnlyShouldKeepPingFrame() throws IOException {
        WebSocketClient mockWsClient = new MockWebSocketClientCreator().createMultipleFrameClient(new Frame[] {
                new TextFrame("whatever"),
                new PingFrame(new byte[0]),
                new TextFrame("last frame")
        });
        PingFrameFilter filter = new PingFrameFilter();
        filter.setFilterType(PingFrameFilter.PingFilterType.FilterPongOnly);

        SampleResult result = new SampleResult();

        assertThat(filter.receiveFrame(mockWsClient, 1000, result).isText());
        assertThat(filter.receiveFrame(mockWsClient, 1000, result).isPing());
        assertThat(filter.receiveFrame(mockWsClient, 1000, result).isText());
    }

    @Test
    public void filterShouldRespondToPing() throws IOException {
        WebSocketClient mockWsClient = new MockWebSocketClientCreator().createMultipleFrameClient(new Frame[] {
                new PingFrame(new byte[0]),
                new TextFrame("last frame")
        });

        PingFrameFilter filter = new PingFrameFilter();
        filter.setReplyToPing(true);

        SampleResult result = new SampleResult();
        assertThat(filter.receiveFrame(mockWsClient, 1000, result).isText());
        verify(mockWsClient).sendPongFrame(AdditionalMatchers.aryEq(new byte[0]));
    }

    @Test
    public void filterShouldNotRespondToPong() throws IOException {
        WebSocketClient mockWsClient = new MockWebSocketClientCreator().createMultipleFrameClient(new Frame[] {
                new PongFrame(new byte[0]),
                new TextFrame("last frame")
        });

        PingFrameFilter filter = new PingFrameFilter();
        filter.setReplyToPing(true);

        SampleResult result = new SampleResult();
        assertThat(filter.receiveFrame(mockWsClient, 1000, result).isText());
        verify(mockWsClient, never()).sendPongFrame();
    }

    @Test
    public void pongShouldContainSameApplicationDataAsPing() throws IOException {
        WebSocketClient mockWsClient = new MockWebSocketClientCreator().createMultipleFrameClient(new Frame[] {
                new PingFrame("pling".getBytes()),
                new TextFrame("last frame")
        });

        PingFrameFilter filter = new PingFrameFilter();
        filter.setReplyToPing(true);

        SampleResult result = new SampleResult();
        assertThat(filter.receiveFrame(mockWsClient, 1000, result).isText());
        verify(mockWsClient).sendPongFrame(AdditionalMatchers.aryEq("pling".getBytes()));
    }

}
