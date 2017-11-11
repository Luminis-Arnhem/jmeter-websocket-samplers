/*
 * Copyright 2016, 2017 Peter Doornbosch
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

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ProtocolException;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class FrameTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void parseEmptyTextFrame() throws IOException {
        Frame frame = Frame.parseFrame(Frame.DataFrameType.NONE, new ByteArrayInputStream(new byte[] { (byte) 0x81, 0 } ));
        assertTrue(frame.isText());
        assertEquals(2, frame.getSize());
    }

    @Test
    public void parseTextFrame() throws IOException {
        Frame frame = Frame.parseFrame(Frame.DataFrameType.NONE, new ByteArrayInputStream(new byte[] { (byte) 0x81, 5, 0x48, 0x65, 0x6c, 0x6c, 0x6f } ));
        assertTrue(frame.isText());
        assertEquals("Hello", ((TextFrame) frame).getText());
        assertEquals(7, frame.getSize());
        assertTrue(((TextFrame) frame).isFinalFragment());
    }

    @Test
    public void parsePongFrame() throws IOException {
        Frame frame = Frame.parseFrame(Frame.DataFrameType.NONE, new ByteArrayInputStream(new byte[] { (byte) 0x8a, 0 } ));
        assertTrue(frame.isPong());
        assertEquals(0, ((PongFrame) frame).getData().length);
        assertEquals(2, frame.getSize());
    }

    @Test
    public void parseCloseFrameNoCloseReason() throws IOException {
        Frame frame = Frame.parseFrame(Frame.DataFrameType.NONE, new ByteArrayInputStream(new byte[] { (byte) 0x88, 2, 0x03, (byte) 0xe9 } ));
        assertTrue(frame.isClose());
        assertEquals(null, ((CloseFrame) frame).getCloseReason());
        assertEquals(1001, (int) ((CloseFrame) frame).getCloseStatus());
        assertEquals(4, frame.getSize());
    }

    @Test
    public void parseCloseFrame() throws IOException {
        Frame frame = Frame.parseFrame(Frame.DataFrameType.NONE, new ByteArrayInputStream(new byte[] { (byte) 0x88, 12, 0x03, (byte) 0xe9, 0x67, 0x6f, 0x69, 0x6e, 0x67, 0x20, 0x61, 0x77, 0x61, 0x79 } ));
        assertTrue(frame.isClose());
        assertEquals("going away", ((CloseFrame) frame).getCloseReason());
        assertEquals(1001, (int) ((CloseFrame) frame).getCloseStatus());
        assertEquals(14, frame.getSize());
    }

    @Test
    public void parseLargestJavaFramePossible() throws Exception {
        try {
            byte[] bytes = new byte[1024];  // dummy, not possible to create a byte array with length 2147483647 or larger....
            bytes[0] = (byte) 0x81;  // Text
            bytes[1] = (byte) 0x7f;  // 127: 64-bit length is used
            bytes[2] = (byte) 0;
            bytes[3] = (byte) 0;
            bytes[4] = (byte) 0;
            bytes[5] = (byte) 0;
            bytes[6] = (byte) 0x7f;
            bytes[7] = (byte) 0xff;
            bytes[8] = (byte) 0xff;
            bytes[9] = (byte) 0xff;

            Frame.parseFrame(Frame.DataFrameType.NONE, new ByteArrayInputStream(bytes));
            Assert.fail("expected exception");
        }
        catch (EndOfStreamException e) {
            // Ok: stream is much too short
        }
        catch (OutOfMemoryError oom) {
            // Ok: dependening on JVM, such a large array can or can not be created.
        }
    }

    @Test
    public void parseFrameWithExcessiveLength1() throws Exception {
        byte[] bytes = new byte[1024];  // dummy, not possible to create a byte array with length 2147483647 or larger....
        bytes[0] = (byte) 0x81;  // Text
        bytes[1] = (byte) 0x7f;  // 127: 64-bit length is used
        bytes[2] = (byte) 0;
        bytes[3] = (byte) 0;
        bytes[4] = (byte) 0;
        bytes[5] = (byte) 0;
        bytes[6] = (byte) 0x90;  // MSB set: too large for Java
        bytes[7] = (byte) 0xff;
        bytes[8] = (byte) 0xff;
        bytes[9] = (byte) 0xff;

        thrown.expect(RuntimeException.class);
        thrown.expectMessage("Frame too large; Java does not support arrays longer than 2147483647 bytes.");
        Frame.parseFrame(Frame.DataFrameType.NONE, new ByteArrayInputStream(bytes));
    }

    @Test
    public void parseFrameWithExcessiveLength2() throws Exception {
        byte[] bytes = new byte[1024];  // dummy, not possible to create a byte array with length 2147483647 or larger....
        bytes[0] = (byte) 0x81;  // Text
        bytes[1] = (byte) 0x7f;  // 127: 64-bit length is used
        bytes[2] = (byte) 0;
        bytes[3] = (byte) 0;
        bytes[4] = (byte) 0;
        bytes[5] = (byte) 1;
        bytes[6] = (byte) 0;
        bytes[7] = (byte) 0;
        bytes[8] = (byte) 0;
        bytes[9] = (byte) 0;

        thrown.expect(RuntimeException.class);
        thrown.expectMessage("Frame too large; Java does not support arrays longer than 2147483647 bytes.");
        Frame.parseFrame(Frame.DataFrameType.NONE, new ByteArrayInputStream(bytes));
    }

    @Test
    public void testReadFromStreamBlocking() throws IOException {

        byte[] inputBuffer = new byte[256];
        for (int i = 0; i < 256; i++)
            inputBuffer[i] = (byte) i;

        InputStream input = new ByteArrayInputStream(inputBuffer) {
            @Override
            public synchronized int read(byte[] b, int offset, int count) {
                // For the test: simulate a network stream and do not return all bytes at once.
                if (count >= 21)
                    return super.read(b, offset, 21);
                else
                    return super.read(b, offset, count);
            }
        };

        byte[] outputBuffer = new byte[256];
        assertEquals(inputBuffer.length, Frame.readFromStream(input, outputBuffer));
        assertArrayEquals(inputBuffer, outputBuffer);
    }

    @Test
    public void testReadFromStreamLimitedMaintainsOrder() throws IOException {

        byte[] inputBuffer = new byte[256];
        for (int i = 0; i < 256; i++)
            inputBuffer[i] = (byte) i;

        InputStream input = new ByteArrayInputStream(inputBuffer) {
            @Override
            public synchronized int read(byte[] b, int offset, int count) {
                // For the test: simulate a network stream and do not return all bytes at once.
                if (count >= 21)
                    return super.read(b, offset, 21);
                else
                    return super.read(b, offset, count);
            }
        };

        byte[] outputBuffer = new byte[222];
        int bytesRead = Frame.readFromStream(input, outputBuffer);
        assertEquals(outputBuffer.length, bytesRead);
        for (int i = 0; i < 222; i++)
            assertEquals(inputBuffer[i], outputBuffer[i]);
        for (int i = 222; i < 256; i++)
            assertEquals(inputBuffer[i], (byte) input.read());
    }

    @Test
    public void testReadFromStreamMightReturnLessThanRequested() throws IOException {

        byte[] inputBuffer = new byte[256];
        for (int i = 0; i < 256; i++)
            inputBuffer[i] = (byte) i;

        InputStream input = new ByteArrayInputStream(inputBuffer) {
            @Override
            public synchronized int read(byte[] b, int offset, int count) {
                // For the test: simulate a network stream and do not return all bytes at once.
                if (count >= 21)
                    return super.read(b, offset, 21);
                else
                    return super.read(b, offset, count);
            }
        };

        byte[] outputBuffer = new byte[512];
        int bytesRead = Frame.readFromStream(input, outputBuffer);
        assertEquals(inputBuffer.length, bytesRead);
        for (int i = 0; i < 256; i++)
            assertEquals(inputBuffer[i], outputBuffer[i]);
        assertEquals(-1, input.read());
    }

    @Test
    public void testParseFinalTextContinuationFrame() throws IOException {
        Frame frame = Frame.parseFrame(Frame.DataFrameType.TEXT, new ByteArrayInputStream(new byte[] { (byte) 0x80, 5, 0x48, 0x65, 0x6c, 0x6c, 0x6f } ));
        assertTrue(frame.isText());
        assertEquals("Hello", ((TextFrame) frame).getText());
        assertEquals(7, frame.getSize());
        assertTrue(((TextFrame) frame).isFinalFragment());
        assertTrue(((TextFrame) frame).isContinuationFrame());
    }

    @Test
    public void testParseNonFinalTextContinuationFrame() throws IOException {
        Frame frame = Frame.parseFrame(Frame.DataFrameType.TEXT, new ByteArrayInputStream(new byte[] { (byte) 0x00, 5, 0x48, 0x65, 0x6c, 0x6c, 0x6f } ));
        assertTrue(frame.isText());
        assertEquals("Hello", ((TextFrame) frame).getText());
        assertEquals(7, frame.getSize());
        assertFalse(((TextFrame) frame).isFinalFragment());
        assertTrue(((TextFrame) frame).isContinuationFrame());
    }

    @Test
    public void testParseFinalBinaryContinuationFrame() throws IOException {
        Frame frame = Frame.parseFrame(Frame.DataFrameType.BIN, new ByteArrayInputStream(new byte[] { (byte) 0x80, 5, 0x48, 0x65, 0x6c, 0x6c, 0x6f } ));
        assertTrue(frame.isBinary());
        assertArrayEquals("Hello".getBytes(), ((BinaryFrame) frame).getBinaryData());
        assertEquals(7, frame.getSize());
        assertTrue(((DataFrame) frame).isFinalFragment());
        assertTrue(((DataFrame) frame).isContinuationFrame());
    }

    @Test
    public void testParseNonFinalBinaryContinuationFrame() throws IOException {
        Frame frame = Frame.parseFrame(Frame.DataFrameType.BIN, new ByteArrayInputStream(new byte[] { (byte) 0x00, 5, 0x48, 0x65, 0x6c, 0x6c, 0x6f } ));
        assertTrue(frame.isBinary());
        assertArrayEquals("Hello".getBytes(), ((BinaryFrame) frame).getBinaryData());
        assertEquals(7, frame.getSize());
        assertFalse(((DataFrame) frame).isFinalFragment());
        assertTrue(((DataFrame) frame).isContinuationFrame());
    }

    @Test
    public void testUnexpectContinuationFrame() throws IOException {
        thrown.expect(ProtocolException.class);
        thrown.expectMessage("no continuation frame expected");
        Frame frame = Frame.parseFrame(Frame.DataFrameType.NONE, new ByteArrayInputStream(new byte[] { (byte) 0x80, 5, 0x48, 0x65, 0x6c, 0x6c, 0x6f } ));
    }
}
