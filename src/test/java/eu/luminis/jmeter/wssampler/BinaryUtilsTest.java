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

import org.junit.Test;

import static org.junit.Assert.*;

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

    @Test
    public void testFormatSingleNibble() {
        assertEquals("0x01", test.formatBinary(new byte[] { 1 }));
    }

    @Test
    public void testFormatZeroBytes() {
        assertEquals("", test.formatBinary(new byte[0]));
    }

    @Test
    public void testFormatLargeBinaryIsLimitedToMaxBytes() {
        String formatted = test.formatBinary(new byte[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15}, 10, "");
        assertEquals(10, formatted.split(" ").length);
    }

    @Test
    public void testFormatLargeBinaryEndsWithSuffix() {
        String formatted = test.formatBinary(new byte[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15}, 10, "...");
        assertTrue(formatted.endsWith("..."));
    }

    @Test
    public void testFormatNegativeValue() {
        assertEquals("0xfc", test.formatBinary(new byte[] { -4 }));
    }

    @Test
    public void testContainsOnEqualValues() {
        assertTrue(test.contains(new byte[] { (byte) 0xca, (byte) 0xfe, (byte) 0xba, (byte) 0xbe }, new byte[] { (byte) 0xca, (byte) 0xfe, (byte) 0xba, (byte) 0xbe }));
    }

    @Test
    public void testContainsOnOverlappingValues() {
        assertTrue(test.contains(new byte[] { (byte) 0xca, (byte) 0xfe, (byte) 0xba, (byte) 0xbe }, new byte[] { (byte) 0xfe, (byte) 0xba }));
    }

    @Test
    public void testContainsOnOverlappingValuesMatchAtEnd() {
        assertTrue(test.contains(new byte[] { (byte) 0xca, (byte) 0xfe, (byte) 0xba, (byte) 0xbe }, new byte[] { (byte) 0xba, (byte) 0xbe }));
    }

    @Test
    public void testContainsOnOverlappingValuesPastEnd() {
        assertFalse(test.contains(new byte[] { (byte) 0xca, (byte) 0xfe, (byte) 0xba, (byte) 0xbe }, new byte[] { (byte) 0xba, (byte) 0xbe, (byte) 0xff }));
    }

    @Test
    public void testDoesNotContain() {
        assertFalse(test.contains(new byte[] { (byte) 0xca, (byte) 0xfe, (byte) 0xba, (byte) 0xbe }, new byte[] { (byte) 0xca, (byte) 0xfe, (byte) 0xcc }));
    }

    @Test
    public void testDoesNotContainAtEnd() {
        assertFalse(test.contains(new byte[] { (byte) 0xca, (byte) 0xfe, (byte) 0xba, (byte) 0xbe }, new byte[] { (byte) 0xbe, (byte) 0xcc }));
    }

    @Test
    public void testBinaryTableWithDefaultWidth() {
        byte[] data = new byte[36];
        for (int i = 0; i < data.length; i++)
            data[i] = (byte) i;

        String formattedBin = test.formatBinaryInTable(data, 16, true, false);
        assertEquals("0000  00 01 02 03 04 05 06 07  08 09 0a 0b 0c 0d 0e 0f\n0010  10 11 12 13 14 15 16 17  18 19 1a 1b 1c 1d 1e 1f\n0020  20 21 22 23\n", formattedBin);
    }

    @Test
    public void testBinaryTableLimitedContent() {
        byte[] data = new byte[636];
        for (int i = 0; i < data.length; i++)
            data[i] = (byte) i;

        String formattedBin = test.formatBinaryInTable(data, 36,16, true, false);
        assertEquals("0000  00 01 02 03 04 05 06 07  08 09 0a 0b 0c 0d 0e 0f\n0010  10 11 12 13 14 15 16 17  18 19 1a 1b 1c 1d 1e 1f\n0020  20 21 22 23\n", formattedBin);
    }

    @Test
    public void testBinaryTableWithAscii() {
        byte[] data = new byte[20];
        for (int i = 0; i < data.length; i++)
            data[i] = (byte) (100 + i);

        String formattedBin = test.formatBinaryInTable(data, 16, true, true);
        assertEquals("0000  64 65 66 67 68 69 6a 6b  6c 6d 6e 6f 70 71 72 73  defghijk lmnopqrs\n0010  74 75 76 77                                       tuvw\n", formattedBin);
    }

    @Test
    public void testBinaryTableWithAscii2() {
        byte[] data = new byte[36];
        for (int i = 0; i < data.length; i++)
            data[i] = (byte) (30 + i);

        String formattedBin = test.formatBinaryInTable(data, 16, true, true);
        assertEquals("0000  1e 1f 20 21 22 23 24 25  26 27 28 29 2a 2b 2c 2d  .. !\"#$% &'()*+,-\n0010  2e 2f 30 31 32 33 34 35  36 37 38 39 3a 3b 3c 3d  ./012345 6789:;<=\n0020"
                + "  3e 3f 40 41                                       >?@A\n", formattedBin);
    }

    @Test
    public void testBinaryTableWithAscii3() {
        byte[] data = new byte[44];
        for (int i = 0; i < data.length; i++)
            data[i] = (byte) (30 + i);

        String formattedBin = test.formatBinaryInTable(data, 16, true, true);
        assertEquals("0000  1e 1f 20 21 22 23 24 25  26 27 28 29 2a 2b 2c 2d  .. !\"#$% &'()*+,-\n0010  2e 2f 30 31 32 33 34 35  36 37 38 39 3a 3b 3c 3d  ./012345 6789:;<=\n"
                            + "0020  3e 3f 40 41 42 43 44 45  46 47 48 49              >?@ABCDE FGHI\n", formattedBin);
    }

    @Test
    public void testBinaryTableNoAddress() {
        byte[] data = new byte[36];
        for (int i = 0; i < data.length; i++)
            data[i] = (byte) i;

        String formattedBin = test.formatBinaryInTable(data, 16, false, false);
        assertEquals("00 01 02 03 04 05 06 07  08 09 0a 0b 0c 0d 0e 0f\n10 11 12 13 14 15 16 17  18 19 1a 1b 1c 1d 1e 1f\n20 21 22 23\n", formattedBin);
    }

    @Test
    public void testBinaryTableWidth7() {
        byte[] data = new byte[36];
        for (int i = 0; i < data.length; i++)
            data[i] = (byte) i;

        String formattedBin = test.formatBinaryInTable(data, 7, true, false);
        assertEquals("0000  00 01 02 03 04 05 06\n0007  07 08 09 0a 0b 0c 0d\n000e  0e 0f 10 11 12 13 14\n0015  15 16 17 18 19 1a 1b\n001c  1c 1d 1e 1f 20 21 22\n0023  23\n", formattedBin);
    }

    @Test
    public void testBinaryTableWidth13() {
        byte[] data = new byte[36];
        for (int i = 0; i < data.length; i++)
            data[i] = (byte) i;

        String formattedBin = test.formatBinaryInTable(data, 13, true, false);
        assertEquals("0000  00 01 02 03 04 05 06 07  08 09 0a 0b 0c\n000d  0d 0e 0f 10 11 12 13 14  15 16 17 18 19\n001a  1a 1b 1c 1d 1e 1f 20 21  22 23\n", formattedBin);
    }

    @Test
    public void testContainsWithEmptyValue() {
        assertFalse(test.contains(new byte[] { 0x01 }, new byte[0]));
    }

}
