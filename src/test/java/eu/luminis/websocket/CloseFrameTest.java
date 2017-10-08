package eu.luminis.websocket;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CloseFrameTest {

    @Test
    public void closeFrameWithOnlyStatusShouldHaveSize4() throws Exception {
        Frame close = Frame.parseFrame(new ByteArrayInputStream(new byte[] { (byte) 0x88, 0x02, 0x03, (byte) 0xe8}));

        assertTrue(close.isClose());
        assertEquals(1000, (int) ((CloseFrame) close).getCloseStatus());
        assertEquals(4, close.getSize());
        assertEquals(2, close.getPayloadSize());
    }

    @Test
    public void closeFrameCloseReasonShouldHaveLargerPayloadSize() throws Exception {
        Frame close = Frame.parseFrame(new ByteArrayInputStream(new byte[] { (byte) 0x88, 0x0e, 0x03, (byte) 0xe8, 0x6e, 0x6f, 0x72, 0x6d, 0x61, 0x6c, 0x20, 0x63, 0x6c, 0x6f, 0x73, 0x65 }));

        assertTrue(close.isClose());
        assertEquals(1000, (int) ((CloseFrame) close).getCloseStatus());
        assertEquals(16, close.getSize());
        assertEquals(14, close.getPayloadSize());
    }

    @Test
    public void minimumCloseFrameShouldHaveSize2() throws IOException {
        Frame close = Frame.parseFrame(new ByteArrayInputStream(new byte[] { (byte) 0x88, 0x00 }));

        assertTrue(close.isClose());
        assertEquals(2, close.getSize());
        assertEquals(0, close.getPayloadSize());
    }
}
