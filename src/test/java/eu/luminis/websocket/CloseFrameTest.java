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
import org.apache.log.Logger;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CloseFrameTest {

    private static Logger logger = LoggingManager.getLoggerForClass();;

    @Test
    public void closeFrameWithOnlyStatusShouldHaveSize4() throws Exception {
        Frame close = Frame.parseFrame(Frame.DataFrameType.NONE, new ByteArrayInputStream(new byte[] { (byte) 0x88, 0x02, 0x03, (byte) 0xe8}));

        assertTrue(close.isClose());
        assertEquals(1000, (int) ((CloseFrame) close).getCloseStatus());
        assertEquals(4, close.getSize());
        assertEquals(2, close.getPayloadSize());
    }

    @Test
    public void closeFrameCloseReasonShouldHaveLargerPayloadSize() throws Exception {
        Frame close = Frame.parseFrame(Frame.DataFrameType.NONE, new ByteArrayInputStream(new byte[] { (byte) 0x88, 0x0e, 0x03, (byte) 0xe8, 0x6e, 0x6f, 0x72, 0x6d, 0x61, 0x6c, 0x20, 0x63, 0x6c, 0x6f, 0x73, 0x65 }));

        assertTrue(close.isClose());
        assertEquals(1000, (int) ((CloseFrame) close).getCloseStatus());
        assertEquals(16, close.getSize());
        assertEquals(14, close.getPayloadSize());
    }

    @Test
    public void minimumCloseFrameShouldHaveSize2() throws IOException {
        Frame close = Frame.parseFrame(Frame.DataFrameType.NONE, new ByteArrayInputStream(new byte[] { (byte) 0x88, 0x00 }), logger);

        assertTrue(close.isClose());
        assertEquals(2, close.getSize());
        assertEquals(0, close.getPayloadSize());
    }
}
