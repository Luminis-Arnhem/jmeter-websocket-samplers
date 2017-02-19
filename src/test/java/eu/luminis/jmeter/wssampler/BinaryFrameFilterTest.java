package eu.luminis.jmeter.wssampler;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class BinaryFrameFilterTest {

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
