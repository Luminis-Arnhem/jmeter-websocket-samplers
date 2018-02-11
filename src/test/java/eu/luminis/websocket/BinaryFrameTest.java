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

import org.junit.Test;

import static org.junit.Assert.*;

public class BinaryFrameTest {

    @Test
    public void toStringShouldPrintReadbleBytes() throws Exception {
        String string = new BinaryFrame(new byte[] { 0x54, 0x3f, 0x00, (byte) 0xcd }).toString();
        assertEquals("Binary frame, payload (length 4): 0x54 0x3f 0x00 0xcd", string);

    }

    @Test
    public void testToStringOnEmptyPayload() throws Exception {
        String string = new BinaryFrame(new byte[0]).toString();
        assertEquals("Binary frame, empty payload", string);
    }

    @Test
    public void toStringOnLargePayloadShouldNotPrintAllBytes() throws Exception {
        BinaryFrame frame = new BinaryFrame(new byte[] { 0x54, 0x3f, 0x00, (byte) 0xcd, (byte) 0xf0, 0x3D });
        frame.nrBytesPrintedInToString = 4;
        assertEquals("Binary frame, payload (length 6): 0x54 0x3f 0x00 0xcd ...", frame.toString());

    }

    @Test
    public void toStringOnPayloadShouldPrintJustBytes() throws Exception {
        BinaryFrame frame = new BinaryFrame(new byte[] { 0x54, 0x3f, 0x00, (byte) 0xCD });
        frame.nrBytesPrintedInToString = 4;
        assertEquals("Binary frame, payload (length 4): 0x54 0x3f 0x00 0xcd", frame.toString());

    }
}