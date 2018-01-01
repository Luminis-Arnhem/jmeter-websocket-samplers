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


import eu.luminis.websocket.BinaryFrame;
import eu.luminis.websocket.EndOfStreamException;
import eu.luminis.websocket.MockWebSocketClientCreator;
import eu.luminis.websocket.WebSocketClient;
import org.apache.jmeter.samplers.SampleResult;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;


import java.io.IOException;

import static eu.luminis.jmeter.wssampler.ComparisonType.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

public class BinaryFrameFilterTest {

    MockWebSocketClientCreator mocker = new MockWebSocketClientCreator();

    SampleResult result;

    @Before
    public void setUp() {
        result = new SampleResult();
    }

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test
    public void plainFilterDiscardsBinaryFrame() throws IOException {
        WebSocketClient wsClient = mocker.createSingleFrameClient(new BinaryFrame(new byte[] { 0x01, 0x02, 0x03, 0x04}));

        BinaryFrameFilter binaryFrameFilter = new BinaryFrameFilter(IsPlain);
        exception.expect(EndOfStreamException.class);
        binaryFrameFilter.receiveFrame(wsClient, 1000, result);
    }

    @Test
    public void filterContainsShouldDropMatchingBinaryFrame() throws IOException {
        WebSocketClient wsClient = mocker.createSingleFrameClient(new BinaryFrame(new byte[] { 0x01, 0x02, 0x03, 0x04}));

        BinaryFrameFilter binaryFrameFilter = new BinaryFrameFilter(Contains);
        binaryFrameFilter.setMatchPosition("1");
        binaryFrameFilter.setMatchValue("0x02 0x03");
        exception.expect(EndOfStreamException.class);
        binaryFrameFilter.receiveFrame(wsClient, 1000, result);
    }

    @Test
    public void filterContainsShouldReturnNonMatchingBinaryFrame() throws IOException {
        WebSocketClient wsClient = mocker.createSingleFrameClient(new BinaryFrame(new byte[] { 0x05, 0x06, 0x07, 0x08}));

        BinaryFrameFilter binaryFrameFilter = new BinaryFrameFilter(Contains);
        binaryFrameFilter.setMatchPosition("1");
        binaryFrameFilter.setMatchValue("0x02 0x03");
        assertTrue(binaryFrameFilter.receiveFrame(wsClient, 1000, result).isBinary());
    }

    @Test
    public void filterContainsWithEmptyMatchValueShouldFilterNothing() throws IOException {
        WebSocketClient wsClient = mocker.createSingleFrameClient(new BinaryFrame(new byte[] { 0x01, 0x02, 0x03, 0x04}));

        BinaryFrameFilter binaryFrameFilter = new BinaryFrameFilter(Contains);
        assertTrue(binaryFrameFilter.receiveFrame(wsClient, 1000, result).isBinary());
    }

    @Test
    public void filterContainsShouldFilterFrameThatContainsMatchValue() throws IOException {
        WebSocketClient wsClient = mocker.createSingleFrameClient(new BinaryFrame(new byte[] { 0x01, 0x02, 0x03 }));

        BinaryFrameFilter binaryFrameFilter = new BinaryFrameFilter(Contains);
        binaryFrameFilter.setMatchValue("0x02 0x03");
        exception.expect(EndOfStreamException.class);
        binaryFrameFilter.receiveFrame(wsClient, 1000, result);
    }

    @Test
    public void filterContainsShouldKeepFrameThatDoesntContainMatchValue() throws IOException {
        WebSocketClient wsClient = mocker.createSingleFrameClient(new BinaryFrame(new byte[] { 0x01, 0x02, 0x03 }));

        BinaryFrameFilter binaryFrameFilter = new BinaryFrameFilter(Contains);
        binaryFrameFilter.setMatchValue("0x02 0x04");
        assertTrue(binaryFrameFilter.receiveFrame(wsClient, 1000, result).isBinary());
    }

    @Test
    public void filterNotContainsShouldFilterFrameThatNotContains() throws IOException {
        WebSocketClient wsClient = mocker.createSingleFrameClient(new BinaryFrame(new byte[] { 0x01, 0x02, 0x03, 0x04}));

        BinaryFrameFilter binaryFrameFilter = new BinaryFrameFilter(NotContains);
        binaryFrameFilter.setMatchValue("0x02 0x04");
        exception.expect(EndOfStreamException.class);
        binaryFrameFilter.receiveFrame(wsClient, 1000, result);
    }

    @Test
    public void filterNotContainsAtPositionShouldFilterFrameThatDoesNotContainAtPosition() throws IOException {
        WebSocketClient wsClient = mocker.createSingleFrameClient(new BinaryFrame(new byte[] { 0x01, 0x02, 0x03, 0x04}));

        BinaryFrameFilter binaryFrameFilter = new BinaryFrameFilter(NotContains);
        binaryFrameFilter.setMatchPosition("2");
        binaryFrameFilter.setMatchValue("0x02 0x03");
        exception.expect(EndOfStreamException.class);
        binaryFrameFilter.receiveFrame(wsClient, 1000, result);
    }

    @Test
    public void filterNotContainsShouldKeepFrameThatContainsTheValue() throws IOException {
        WebSocketClient wsClient = mocker.createSingleFrameClient(new BinaryFrame(new byte[] { 0x01, 0x02, 0x03, 0x04}));

        BinaryFrameFilter binaryFrameFilter = new BinaryFrameFilter(NotContains);
        binaryFrameFilter.setMatchValue("0x03 0x04");
        assertTrue(binaryFrameFilter.receiveFrame(wsClient, 1000, result).isBinary());
    }

    @Test
    public void filterNotContainsAtPositionShouldKeepFrameThatContainsTheValueAtThatPosition() throws IOException {
        WebSocketClient wsClient = mocker.createSingleFrameClient(new BinaryFrame(new byte[] { 0x01, 0x02, 0x03, 0x04}));

        BinaryFrameFilter binaryFrameFilter = new BinaryFrameFilter(NotContains);
        binaryFrameFilter.setMatchPosition("2");
        binaryFrameFilter.setMatchValue("0x03 0x04");
        assertTrue(binaryFrameFilter.receiveFrame(wsClient, 1000, result).isBinary());
    }

    @Test
    public void filterEqualWithEqualFrameShouldFilter() throws IOException {
        WebSocketClient wsClient = mocker.createSingleFrameClient(new BinaryFrame(new byte[] { 0x01, 0x02, 0x03, 0x04}));

        BinaryFrameFilter binaryFrameFilter = new BinaryFrameFilter(Equals);
        binaryFrameFilter.setMatchValue("0x01 0x02 0x03 0x04");
        exception.expect(EndOfStreamException.class);
        binaryFrameFilter.receiveFrame(wsClient, 1000, result);
    }

    @Test
    public void filterEqualWithUnequalFrame1ShouldNotBeFiltered() throws IOException {
        WebSocketClient wsClient = mocker.createSingleFrameClient(new BinaryFrame(new byte[] { 0x01, 0x02, 0x03, 0x04}));

        BinaryFrameFilter binaryFrameFilter = new BinaryFrameFilter(Equals);
        binaryFrameFilter.setMatchValue("0x01 0x02 0x03 0x04 0x05");
        assertTrue(binaryFrameFilter.receiveFrame(wsClient, 1000, result).isBinary());
    }

    @Test
    public void filterEqualWithUnequalFrames2ShouldNotBeFiltered() throws IOException {
        WebSocketClient wsClient = mocker.createSingleFrameClient(new BinaryFrame(new byte[] { 0x01, 0x02, 0x03, 0x04}));

        BinaryFrameFilter binaryFrameFilter = new BinaryFrameFilter(Equals);
        binaryFrameFilter.setMatchValue("0x01");
        assertTrue(binaryFrameFilter.receiveFrame(wsClient, 1000, result).isBinary());
    }

    @Test
    public void filterEqualWithUnequalFrames3ShouldNoBeFiltered() throws IOException {
        WebSocketClient wsClient = mocker.createSingleFrameClient(new BinaryFrame(new byte[] { 0x01, 0x02, 0x03 }));

        BinaryFrameFilter binaryFrameFilter = new BinaryFrameFilter(Equals);
        binaryFrameFilter.setMatchValue("0x01 0x02 0x03 0x04");
        assertTrue(binaryFrameFilter.receiveFrame(wsClient, 1000, result).isBinary());
    }

    @Test
    public void filterNotEqualShouldKeepEqualFrame() throws IOException {
        WebSocketClient wsClient = mocker.createSingleFrameClient(new BinaryFrame(new byte[] { 0x01, 0x02, 0x03, 0x04}));

        BinaryFrameFilter binaryFrameFilter = new BinaryFrameFilter(NotEquals);
        binaryFrameFilter.setMatchValue("0x01 0x02 0x03 0x04");
        assertTrue(binaryFrameFilter.receiveFrame(wsClient, 1000, result).isBinary());
    }

    @Test
    public void filterNotEqualShouldFilterUnEqualFrame1() throws IOException {
        WebSocketClient wsClient = mocker.createSingleFrameClient(new BinaryFrame(new byte[] { 0x01, 0x02, 0x03, 0x04}));

        BinaryFrameFilter binaryFrameFilter = new BinaryFrameFilter(NotEquals);
        binaryFrameFilter.setMatchValue("0x01 0x02 0x03");
        exception.expect(EndOfStreamException.class);
        binaryFrameFilter.receiveFrame(wsClient, 1000, result);
    }

    @Test
    public void filterNotEqualShouldFilterUnEqualFrame2() throws IOException {
        WebSocketClient wsClient = mocker.createSingleFrameClient(new BinaryFrame(new byte[] { 0x01, 0x02, 0x03 }));

        BinaryFrameFilter binaryFrameFilter = new BinaryFrameFilter(NotEquals);
        binaryFrameFilter.setMatchValue("0x01 0x02 0x03 0x04");
        exception.expect(EndOfStreamException.class);
        binaryFrameFilter.receiveFrame(wsClient, 1000, result);
    }

    @Test
    public void filterNotEqualShouldFilterUnEqualFrame3() throws IOException {
        WebSocketClient wsClient = mocker.createSingleFrameClient(new BinaryFrame(new byte[] { 0x02, 0x03, 0x04 }));

        BinaryFrameFilter binaryFrameFilter = new BinaryFrameFilter(NotEquals);
        binaryFrameFilter.setMatchValue("0x01 0x02 0x03 0x04");
        exception.expect(EndOfStreamException.class);
        binaryFrameFilter.receiveFrame(wsClient, 1000, result);
    }

    @Test
    public void filterStartsWithShouldFilterFrameThatStartsWith() throws IOException {
        WebSocketClient wsClient = mocker.createSingleFrameClient(new BinaryFrame(new byte[] { 0x01, 0x02, 0x03 }));

        BinaryFrameFilter binaryFrameFilter = new BinaryFrameFilter(StartsWith);
        binaryFrameFilter.setMatchValue("0x01 0x02");
        exception.expect(EndOfStreamException.class);
        binaryFrameFilter.receiveFrame(wsClient, 1000, result);
    }

    @Test
    public void filterStartsWithShouldKeepFrameThatNotStartsWith() throws IOException {
        WebSocketClient wsClient = mocker.createSingleFrameClient(new BinaryFrame(new byte[] { 0x01, 0x02, 0x03 }));

        BinaryFrameFilter binaryFrameFilter = new BinaryFrameFilter(StartsWith);
        binaryFrameFilter.setMatchValue("0x02 0x03");
        assertTrue(binaryFrameFilter.receiveFrame(wsClient, 1000, result).isBinary());
    }

    @Test
    public void filterNotStartsWithShouldKeepFrameThatStartsWith() throws IOException {
        WebSocketClient wsClient = mocker.createSingleFrameClient(new BinaryFrame(new byte[] { 0x01, 0x02, 0x03 }));

        BinaryFrameFilter binaryFrameFilter = new BinaryFrameFilter(NotStartsWith);
        binaryFrameFilter.setMatchValue("0x01 0x02");
        assertTrue(binaryFrameFilter.receiveFrame(wsClient, 1000, result).isBinary());
    }

    @Test
    public void filterNotStartsWithShouldFilterFrameThatNotStartsWith() throws IOException {
        WebSocketClient wsClient = mocker.createSingleFrameClient(new BinaryFrame(new byte[] { 0x01, 0x02, 0x03 }));

        BinaryFrameFilter binaryFrameFilter = new BinaryFrameFilter(NotStartsWith);
        binaryFrameFilter.setMatchValue("0x01 0x02 0x03 0x04");
        exception.expect(EndOfStreamException.class);
        binaryFrameFilter.receiveFrame(wsClient, 1000, result);
    }

    @Test
    public void filterEndsWithShouldFilterFrameThatEndsWith() throws IOException {
        WebSocketClient wsClient = mocker.createSingleFrameClient(new BinaryFrame(new byte[] { 0x01, 0x02, 0x03, 0x04 }));

        BinaryFrameFilter binaryFrameFilter = new BinaryFrameFilter(EndsWith);
        binaryFrameFilter.setMatchValue("0x04");
        exception.expect(EndOfStreamException.class);
        binaryFrameFilter.receiveFrame(wsClient, 1000, result);
    }

    @Test
    public void filterEndsWithShouldKeepFrameThatNotEndsWith1() throws IOException {
        WebSocketClient wsClient = mocker.createSingleFrameClient(new BinaryFrame(new byte[] { 0x01, 0x02, 0x03, 0x04 }));

        BinaryFrameFilter binaryFrameFilter = new BinaryFrameFilter(EndsWith);
        binaryFrameFilter.setMatchValue("0x02 0x04");
        assertTrue(binaryFrameFilter.receiveFrame(wsClient, 1000, result).isBinary());
    }

    @Test
    public void filterEndsWithShouldKeepFrameThatNotEndsWith2() throws IOException {
        WebSocketClient wsClient = mocker.createSingleFrameClient(new BinaryFrame(new byte[] { 0x01, 0x02, 0x03, 0x04 }));

        BinaryFrameFilter binaryFrameFilter = new BinaryFrameFilter(EndsWith);
        binaryFrameFilter.setMatchValue("0x00 0x01 0x02 0x03 0x04");
        assertTrue(binaryFrameFilter.receiveFrame(wsClient, 1000, result).isBinary());
    }

    @Test
    public void filterNotEndsWithShouldKeepFrameThatEndsWith() throws IOException {
        WebSocketClient wsClient = mocker.createSingleFrameClient(new BinaryFrame(new byte[] { 0x01, 0x02, 0x03, 0x04 }));

        BinaryFrameFilter binaryFrameFilter = new BinaryFrameFilter(NotEndsWith);
        binaryFrameFilter.setMatchValue("0x01 0x02 0x03 0x04");
        assertTrue(binaryFrameFilter.receiveFrame(wsClient, 1000, result).isBinary());
    }

    @Test
    public void filterNotEndsWithShouldFilterFrameThatNotEndsWith() throws IOException {
        WebSocketClient wsClient = mocker.createSingleFrameClient(new BinaryFrame(new byte[] { 0x01, 0x02, 0x03, 0x04 }));

        BinaryFrameFilter binaryFrameFilter = new BinaryFrameFilter(NotEndsWith);
        binaryFrameFilter.setMatchValue("0x01 0x09 0x03 0x04");
        exception.expect(EndOfStreamException.class);
        binaryFrameFilter.receiveFrame(wsClient, 1000, result);
    }

    @Test
    public void testMatchValueAcceptsBinary() {
        BinaryFrameFilter binaryFrameFilter = new BinaryFrameFilter(NotEndsWith);
        binaryFrameFilter.setMatchValue("01 02 03 04");
        binaryFrameFilter.prepareFilter();
        assertArrayEquals(new byte[] { 1, 2, 3, 4}, binaryFrameFilter.matchValue);
    }

    @Test
    public void testMatchValueDoesNotAcceptNonBinary() {
        BinaryFrameFilter binaryFrameFilter = new BinaryFrameFilter(NotEndsWith);
        binaryFrameFilter.setMatchValue("01 02 03 04 bo oe");
        binaryFrameFilter.prepareFilter();
        assertArrayEquals(new byte[0], binaryFrameFilter.matchValue);
    }

    @Test
    public void equalByteArraysShouldEqual() {
        assertTrue(BinaryFrameFilter.equalBytes(new byte[] { 0x5f, (byte) 0xa3, 0x00, (byte) 0x86 }, 0, new byte[] { 0x5f, (byte) 0xa3, 0x00, (byte) 0x86 }, 0, 4));
    }

    @Test
    public void equalByteArraySlicesShouldEqual() {
        assertTrue(BinaryFrameFilter.equalBytes(new byte[] { 0x5f, (byte) 0xa3, 0x59, (byte) 0x86 }, 0, new byte[] { 0x5f, (byte) 0xa3, 0x00, 0x7f }, 0, 2));
    }

    @Test
    public void differentLengthByteArraysShouldNotEqual1() {
        assertFalse(BinaryFrameFilter.equalBytes(new byte[] { 0x5f, (byte) 0xa3 }, 0, new byte[] { 0x5f, (byte) 0xa3, 0x00, (byte) 0x86 }, 0, 3));
    }

    @Test
    public void differentLengthByteArraysShouldNotEqual2() {
        assertFalse(BinaryFrameFilter.equalBytes(new byte[] { 0x5f, (byte) 0xa3, 0x00, (byte) 0x86 }, 0, new byte[] { 0x5f, (byte) 0xa3 }, 0, 3));
    }

    @Test
    public void equalByteArrayShiftedShouldEqual() {
        assertTrue(BinaryFrameFilter.equalBytes(new byte[] { 0x63, 0x5f, (byte) 0xa3, 0x00, (byte) 0x86 }, 1, new byte[] { 0x5f, (byte) 0xa3, 0x00, (byte) 0x86 }, 0, 4));
    }

    @Test
    public void equalByteArrayShiftedSlicesShouldEqual() {
        assertTrue(BinaryFrameFilter.equalBytes(new byte[] { 0x63, 0x5f, (byte) 0xa3, 0x00, 0x46 }, 1, new byte[] { 0x5f, (byte) 0xa3, 0x00, (byte) 0x86 }, 0, 3));
    }

}
