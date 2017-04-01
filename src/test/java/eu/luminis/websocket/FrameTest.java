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

public class FrameTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

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

            Frame.parseFrame(new ByteArrayInputStream(bytes));
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
        Frame.parseFrame(new ByteArrayInputStream(bytes));
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
        Frame.parseFrame(new ByteArrayInputStream(bytes));
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
        Assert.assertEquals(inputBuffer.length, Frame.readFromStream(input, outputBuffer));
        Assert.assertArrayEquals(inputBuffer, outputBuffer);
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
        Assert.assertEquals(outputBuffer.length, bytesRead);
        for (int i = 0; i < 222; i++)
            Assert.assertEquals(inputBuffer[i], outputBuffer[i]);
        for (int i = 222; i < 256; i++)
            Assert.assertEquals(inputBuffer[i], (byte) input.read());
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
        Assert.assertEquals(inputBuffer.length, bytesRead);
        for (int i = 0; i < 256; i++)
            Assert.assertEquals(inputBuffer[i], outputBuffer[i]);
        Assert.assertEquals(-1, input.read());
    }

}
