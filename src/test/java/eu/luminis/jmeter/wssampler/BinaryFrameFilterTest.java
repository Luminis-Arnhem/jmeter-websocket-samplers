package eu.luminis.jmeter.wssampler;

import eu.luminis.websocket.BinaryFrame;
import eu.luminis.websocket.EndOfStreamException;
import eu.luminis.websocket.WebSocketClient;
import org.apache.jmeter.samplers.SampleResult;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.IOException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BinaryFrameFilterTest {

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test
    public void filterShouldDropMatchingBinaryFrame() throws IOException {
        WebSocketClient mockWsClient = mock(WebSocketClient.class);
        when(mockWsClient.receiveFrame(anyInt()))
                .thenReturn(new BinaryFrame(new byte[] { 0x01, 0x02, 0x03, 0x04}))
                .thenThrow(new EndOfStreamException("end of stream"));

        exception.expect(EndOfStreamException.class);
        BinaryFrameFilter binaryFrameFilter = new BinaryFrameFilter();
        binaryFrameFilter.setMatchPosition(1);
        binaryFrameFilter.setMatchValue("0x02 0x03");
        binaryFrameFilter.receiveFrame(mockWsClient, 1000, new SampleResult());
    }

    @Test
    public void filterShouldReturnNonMatchingBinaryFrame() throws IOException {
        WebSocketClient mockWsClient = mock(WebSocketClient.class);
        when(mockWsClient.receiveFrame(anyInt()))
                .thenReturn(new BinaryFrame(new byte[] { 0x05, 0x06, 0x07, 0x08}))
                .thenThrow(new EndOfStreamException("end of stream"));

        BinaryFrameFilter binaryFrameFilter = new BinaryFrameFilter();
        binaryFrameFilter.setMatchPosition(1);
        binaryFrameFilter.setMatchValue("0x02 0x03");
        assertTrue(binaryFrameFilter.receiveFrame(mockWsClient, 1000, new SampleResult()).isBinary());
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
