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
    public void testContainsWithEmptyValue() {
        assertFalse(test.contains(new byte[] { 0x01 }, new byte[0]));
    }

}
