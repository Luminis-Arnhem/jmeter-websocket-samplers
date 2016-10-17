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

        byte[] buffer = new byte[256];
        for (int i = 0; i < 256; i++)
            buffer[i] = (byte) i;

        InputStream input = new ByteArrayInputStream(buffer) {
            @Override
            public synchronized int read(byte[] b, int off, int len) {
                // For the test: do not return all bytes, but just a few
                return super.read(b, off, 21);
            }
        };

        Assert.assertEquals(buffer.length, Frame.readFromStream(input, buffer));
    }
}
