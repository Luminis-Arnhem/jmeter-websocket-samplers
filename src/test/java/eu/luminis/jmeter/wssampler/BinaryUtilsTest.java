package eu.luminis.jmeter.wssampler;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;

public class BinaryUtilsTest {

    private BinaryUtils test = new BinaryUtils();

    @Test
    public void testParseHexString() throws Exception {
        byte[] result = test.parseBinaryString("0x12 0x03 0x87 0xf");

        assertArrayEquals(new byte[]{0x12, 0x03, (byte) 0x87, (byte) 0xf}, result);
    }

    @Test
    public void testParseBinarySpaceSeparatedString() throws Exception {
        byte[] result = test.parseBinaryString("b3 03 ac fb");

        assertArrayEquals(new byte[]{(byte) 0xb3, 0x03, (byte) 0xac, (byte) 0xfb}, result);
    }

    @Test
    public void testParseBinaryString() throws Exception {
        byte[] result = test.parseBinaryString("cafe babe");

        assertArrayEquals(new byte[]{(byte) 0xca, (byte) 0xfe, (byte) 0xba, (byte) 0xbe}, result);
    }

    @Test
    public void testParseBinaryStringOnevenLength() throws Exception {
        byte[] result = test.parseBinaryString("cafe babe bae");

        assertArrayEquals(new byte[]{(byte) 0xca, (byte) 0xfe, (byte) 0xba, (byte) 0xbe, (byte) 0xba, (byte) 0xe }, result);
    }

    @Test(expected = NumberFormatException.class)
    public void testHexErrorString() throws Exception {
        test.parseBinaryString("0x12 0xfg");
    }

    @Test
    public void testParseEmptyString() throws Exception {
        byte[] result = test.parseBinaryString("");
        assertArrayEquals(new byte[0], result);
    }

    @Test
    public void testParseWhitespaceString() throws Exception {
        byte[] result = test.parseBinaryString(" ");
        assertArrayEquals(new byte[0], result);
    }

}