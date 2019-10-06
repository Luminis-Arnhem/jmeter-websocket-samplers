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

import org.apache.jorphan.logging.LoggingManager;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import org.apache.log.Logger;

import java.io.*;
import java.net.ProtocolException;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.Iterator;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.*;


public class FrameTest {

    private static Logger logger = LoggingManager.getLoggerForClass();;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void parseEmptyTextFrame() throws IOException {
        Frame frame = Frame.parseFrame(Frame.DataFrameType.NONE, new ByteArrayInputStream(new byte[] { (byte) 0x81, 0 } ), logger);
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
        Frame frame = Frame.parseFrame(Frame.DataFrameType.NONE, new ByteArrayInputStream(new byte[] { (byte) 0x8a, 0 } ), logger);
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
    public void testReadStreamWithZeroReadFromInput() throws IOException {
        // This test simulates the situation that the BufferedInputStream in Frame.readFromStream, sometimes reads
        // zero bytes. Given the javadoc of InputStream, this is not expected, but apparently, this sometimes happens,
        // see https://bitbucket.org/pjtr/jmeter-websocket-samplers/issues/115/unsupported-frame-type-under-heavy-load.

        InputStream simulateInputFromSocket = new InputStream() {
            byte[] data = new byte[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29 };
            int index = 0;
            int simulationStep = 0;

            @Override
            public int read(byte[] b, int off, int len) throws IOException {
                int count = Integer.min(10, Integer.min(data.length - index, len));
                if (simulationStep == 1)
                    count = 0;
                System.arraycopy(data, index, b, off, count);
                index += count;
                simulationStep++;
                return count;
            }

            @Override
            public int available() throws IOException {
                // Always return 0, to make the caller believe there are currently no bytes available (but there will be later on)
                return 0;
            }

            @Override
            public int read(byte[] b) throws IOException {
                // Don't need to implement this method for the tests.
                throw new RuntimeException("Not implemented");
            }

            @Override
            public int read() throws IOException {
                // Don't need to implement this method for the tests.
                throw new RuntimeException("Not implemented");
            }
        };

        byte[] buffer = new byte[20];
        int nrBytesRead = Frame.readFromStream(new BufferedInputStream(simulateInputFromSocket, 8), buffer, logger);

        assertEquals(20, nrBytesRead);
        for (int i = 0; i < 20; i++)
            assertEquals(i, buffer[i]);
    }

    @Test
    public void testReadStreamWithEmptyBuffer() throws IOException {
        int bytesRead = Frame.readFromStream(new ByteArrayInputStream(new byte[] { 0x01, 0x02, 0x03, 0x04 }), new byte[0], logger);

        assertEquals(0, bytesRead);
    }

    @Test
    public void testReadStreamForZeroBytes() throws IOException {
        int bytesRead = Frame.readFromStream(new ByteArrayInputStream(new byte[] { 0x0a, 0x0b, 0x0c, 0x0d }), new byte[1024], 0, 0, logger);

        assertEquals(0, bytesRead);
    }

    @Test
    public void testReadStreamInvalidOffset() throws IOException {
        int bytesRead = Frame.readFromStream(new ByteArrayInputStream(new byte[] { 0x0a, 0x0b, 0x0c, 0x0d }), new byte[1024], 1022, 4, logger);

        assertEquals(2, bytesRead);
    }

    @Test
    public void testTimeoutsDuringReadDoNotAffectResultOfParsingFrame() throws IOException {
        // Setup an input stream that contains one text frame, but throws a SocketTimeoutException after each 8 bytes read.
        InputStream simulatedNetworkStream = new SimulatedNetworkStreamWithTimeouts(
                new byte[] { (byte) 0x81, 18, 0x48, 0x65, 0x6c, 0x6c, 0x6f, 0x2c, 0x20, 0x77, 0x65, 0x62, 0x2d, 0x73, 0x6f, 0x63, 0x6b, 0x65, 0x74, 0x21, (byte) 0xca },
                new Integer[] { 8, 16 }
        );

        InputStream input = (new BufferedInputStream(simulatedNetworkStream));

        boolean gotTimeout = false;
        try {
            Frame.parseFrame(Frame.DataFrameType.NONE, input, logger);
        } catch (SocketTimeoutException e) {
            gotTimeout = true;
        }
        assertTrue(gotTimeout);
        gotTimeout = false;

        try {
            Frame.parseFrame(Frame.DataFrameType.NONE, input, logger);
        } catch (SocketTimeoutException e) {
            gotTimeout = true;
        }
        assertTrue(gotTimeout);

        Frame frame = Frame.parseFrame(Frame.DataFrameType.NONE, input, logger);
        assertTrue(frame instanceof TextFrame);
        assertEquals("Hello, web-socket!", ((TextFrame) frame).getText());
        assertEquals((byte) 0xca, (byte) input.read());
    }

    @Test
    public void testTimeoutAfterFirstByte() throws IOException {
        InputStream simulatedNetworkStream = new SimulatedNetworkStreamWithTimeouts(
                new byte[] { (byte) 0x81, 5, 0x48, 0x65, 0x6c, 0x6c, 0x6f },
                new Integer[] { 1 }
        );

        InputStream input = (new BufferedInputStream(simulatedNetworkStream));

        boolean gotTimeout = false;
        try {
            Frame.parseFrame(Frame.DataFrameType.NONE, input, logger);
        } catch (SocketTimeoutException e) {
            gotTimeout = true;
        }
        assertTrue(gotTimeout);

        Frame frame = Frame.parseFrame(Frame.DataFrameType.NONE, input, logger);
        assertTrue(frame instanceof TextFrame);
        assertEquals("Hello", ((TextFrame) frame).getText());
    }

    @Test
    public void testTimeoutAfterSecondByte() throws IOException {
        InputStream simulatedNetworkStream = new SimulatedNetworkStreamWithTimeouts(
                new byte[] { (byte) 0x81, 5, 0x48, 0x65, 0x6c, 0x6c, 0x6f },
                new Integer[] { 2 }
        );

        InputStream input = (new BufferedInputStream(simulatedNetworkStream));

        boolean gotTimeout = false;
        try {
            Frame.parseFrame(Frame.DataFrameType.NONE, input, logger);
        } catch (SocketTimeoutException e) {
            gotTimeout = true;
        }
        assertTrue(gotTimeout);

        Frame frame = Frame.parseFrame(Frame.DataFrameType.NONE, input, logger);
        assertTrue(frame instanceof TextFrame);
        assertEquals("Hello", ((TextFrame) frame).getText());
    }

    @Test
    public void testTimeoutInTwoBytesLengthField() throws IOException {
        byte[] rawData = new byte[4 + 200 + 1];
        System.arraycopy(new byte[] { (byte) 0x81, 126, 0, (byte) 200, 0x48, 0x65, 0x6c, 0x6c, 0x6f }, 0, rawData, 0, 9);
        rawData[204] = (byte) 0xca;
        InputStream simulatedNetworkStream = new SimulatedNetworkStreamWithTimeouts(rawData, new Integer[] { 3 });

        InputStream input = (new BufferedInputStream(simulatedNetworkStream));

        boolean gotTimeout = false;
        try {
            Frame.parseFrame(Frame.DataFrameType.NONE, input, logger);
        } catch (SocketTimeoutException e) {
            gotTimeout = true;
        }
        assertTrue(gotTimeout);

        Frame frame = Frame.parseFrame(Frame.DataFrameType.NONE, input, logger);
        assertTrue(frame instanceof TextFrame);
        assertTrue(((TextFrame) frame).getText().startsWith("Hello"));
        assertEquals(200, frame.getPayloadSize());
        assertEquals((byte) 0xca, (byte) input.read());
    }

    @Test
    public void testTimeoutInLastBytesOfTwoBytesLengthFieldFrame() throws IOException {
        byte[] rawData = new byte[4 + 65535 + 1];
        System.arraycopy(new byte[] { (byte) 0x81, 126, (byte) 0xff, (byte) 0xff, 0x48, 0x65, 0x6c, 0x6c, 0x6f }, 0, rawData, 0, 9);
        rawData[4 + 65535 + 1 - 1] = (byte) 0xca;
        // This will test whether the markLimit is large enough
        InputStream simulatedNetworkStream = new SimulatedNetworkStreamWithTimeouts(rawData, new Integer[] { 65538 });

        InputStream input = new BufferedInputStream(simulatedNetworkStream);

        boolean gotTimeout = false;
        try {
            Frame.parseFrame(Frame.DataFrameType.NONE, input, logger);
        } catch (SocketTimeoutException e) {
            gotTimeout = true;
        }
        assertTrue(gotTimeout);

        Frame frame = Frame.parseFrame(Frame.DataFrameType.NONE, input, logger);
        assertTrue(frame instanceof TextFrame);
        assertTrue(((TextFrame) frame).getText().startsWith("Hello"));
        assertEquals(65535, frame.getPayloadSize());
        assertEquals((byte) 0xca, (byte) input.read());
    }

    @Test
    public void testTimeoutInEightBytesLengthField() throws IOException {
        byte[] rawData = new byte[10 + 200 + 1];
        // Using a length that can be fit in less than 8 bytes is against the specification, but this implementation allows it.
        System.arraycopy(new byte[] { (byte) 0x81, 127, 0, 0, 0, 0, 0, 0, 0, (byte) 200, 0x48, 0x65, 0x6c, 0x6c, 0x6f }, 0, rawData, 0, 15);
        rawData[210] = (byte) 0xca;
        InputStream simulatedNetworkStream = new SimulatedNetworkStreamWithTimeouts(rawData, new Integer[] { 5 });

        // BufferedInputStream's mark-limit is at least buffer size, so to test whether the markLimit is set correctly, a small buffer size is needed.
        InputStream input = new BufferedInputStream(simulatedNetworkStream, 8);

        boolean gotTimeout = false;
        try {
            Frame.parseFrame(Frame.DataFrameType.NONE, input, logger);
        } catch (SocketTimeoutException e) {
            gotTimeout = true;
        }
        assertTrue(gotTimeout);

        Frame frame = Frame.parseFrame(Frame.DataFrameType.NONE, input, logger);
        assertTrue(frame instanceof TextFrame);
        assertTrue(((TextFrame) frame).getText().startsWith("Hello"));
        assertEquals(200, frame.getPayloadSize());
        assertEquals((byte) 0xca, (byte) input.read());
    }

    @Test
    public void testTimeoutInFrameWithBytesLengthField() throws IOException {
        byte[] rawData = new byte[10 + 200 + 1];
        // Using a length that can be fit in less than 8 bytes is against the specification, but this implementation allows it.
        System.arraycopy(new byte[] { (byte) 0x81, 127, 0, 0, 0, 0, 0, 0, 0, (byte) 200, 0x48, 0x65, 0x6c, 0x6c, 0x6f }, 0, rawData, 0, 15);
        rawData[210] = (byte) 0xca;
        InputStream simulatedNetworkStream = new SimulatedNetworkStreamWithTimeouts(rawData, new Integer[] { 195 });

        InputStream input = new BufferedInputStream(simulatedNetworkStream, 8);

        boolean gotTimeout = false;
        try {
            Frame.parseFrame(Frame.DataFrameType.NONE, input, logger);
        } catch (SocketTimeoutException e) {
            gotTimeout = true;
        }
        assertTrue(gotTimeout);

        Frame frame = Frame.parseFrame(Frame.DataFrameType.NONE, input, logger);
        assertTrue(frame instanceof TextFrame);
        assertTrue(((TextFrame) frame).getText().startsWith("Hello"));
        assertEquals(200, frame.getPayloadSize());
        assertEquals((byte) 0xca, (byte) input.read());
    }

    @Test
    public void otherIoExceptionDoesNotResetStreamToStartOfFrame() {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(new byte[]{ (byte) 0x81, 127, 0, 0, 0, 0, 0, 0, 0, (byte) 200, 0x48, 0x65, 0x6c, 0x6c, 0x6f });
        try {
            Frame.parseFrame(Frame.DataFrameType.NONE, inputStream, logger);
        } catch (IOException e) {
            assertFalse(e instanceof SocketTimeoutException);
        }
        int nextRead = inputStream.read();
        assertEquals(-1, nextRead);
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

    static class SimulatedNetworkStreamWithTimeouts extends InputStream {

        // This input stream simulates the situation that part of a frame is read, but that reading more bytes throws
        // a SocketTimeout exception, which can happen on slow or busy networks in combination with large frames.
        byte[] data;
        int index = 0;
        Iterator<Integer> remainingTimeoutIndexes;
        int nextTimeoutIndex;

        SimulatedNetworkStreamWithTimeouts(byte[] data, Integer[] timeoutIndexes) {
            this.data = data;
            this.remainingTimeoutIndexes = Arrays.asList(timeoutIndexes).iterator();
            nextTimeoutIndex = remainingTimeoutIndexes.next();
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            if (index == nextTimeoutIndex) {
                nextTimeoutIndex();
                throw new SocketTimeoutException();
            }
            else {
                int count = Integer.min(nextTimeoutIndex - index, len);
                if (index + count > data.length) {
                    count = data.length - index;
                }
                System.arraycopy(data, index, b, off, count);
                index += count;
                return count;
            }
        }

        @Override
        public int read() throws IOException {
            if (index == nextTimeoutIndex) {
                nextTimeoutIndex();
                throw new SocketTimeoutException();
            }
            else if (index >= data.length)
                return -1;
            else
                return data[index++];
        }

        private void nextTimeoutIndex() {
            if (remainingTimeoutIndexes.hasNext())
                nextTimeoutIndex = remainingTimeoutIndexes.next();
            else
                nextTimeoutIndex = Integer.MAX_VALUE;
        }
    }
}
