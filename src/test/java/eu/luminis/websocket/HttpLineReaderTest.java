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

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class HttpLineReaderTest {

    @Test
    public void testReadHeaderLine() throws IOException {

        String httpHeader = "HTTP/1.x 200 OK\r\nTransfer-Encoding: chunked\r\nDate: Sat, 28 Nov 2009 04:36:25 GMT\r\nServer: LiteSpeed\r\nConnection: close";
        HttpLineReader reader = new HttpLineReader(new ByteArrayInputStream(httpHeader.getBytes()));
        assertEquals("HTTP/1.x 200 OK", reader.readLine());
        assertEquals("Transfer-Encoding: chunked", reader.readLine());
    }

    @Test
    public void testReadHeaderLineWithMissingCR() throws IOException {

        String httpHeader = "HTTP/1.x 200 OK\nTransfer-Encoding: chunked\nDate: Sat, 28 Nov 2009 04:36:25 GMT\nServer: LiteSpeed\nConnection: close";
        HttpLineReader reader = new HttpLineReader(new ByteArrayInputStream(httpHeader.getBytes()));
        assertEquals("HTTP/1.x 200 OK", reader.readLine());
        assertEquals("Transfer-Encoding: chunked", reader.readLine());
    }

    @Test
    public void testMissingNewLine() throws IOException {

        String httpHeader = "HTTP/1.x 200 OK\r\nTransfer-Encoding: chunk";
        HttpLineReader reader = new HttpLineReader(new ByteArrayInputStream(httpHeader.getBytes()));
        assertEquals("HTTP/1.x 200 OK", reader.readLine());
        assertEquals("Transfer-Encoding: chunk", reader.readLine());
        assertNull(reader.readLine());
    }

    @Test
    public void testReadLineWhenInputEmpty() throws IOException {

        String httpHeader = "HTTP/1.x 200 OK\r\n";
        HttpLineReader reader = new HttpLineReader(new ByteArrayInputStream(httpHeader.getBytes()));
        assertEquals("HTTP/1.x 200 OK", reader.readLine());
        assertNull(reader.readLine());
    }

    @Test
    public void testReadVeryLongHeaderLine() throws IOException {

        String httpHeader = "HTTP/1.x 200 OK\r\nX-Pingback: http://nowhere.com/asergi/egidv/aefoef/weief/awefpiosf/wergiuhdb/zdjkhf/ajksdfhaslf/alksfuhalfs/alksfha/8765\r\nDate: Sat, 28 Nov 2009 04:36:25 GMT\r\nServer: LiteSpeed\r\nConnection: close";
        HttpLineReader reader = new HttpLineReader(new ByteArrayInputStream(httpHeader.getBytes()));
        assertEquals("HTTP/1.x 200 OK", reader.readLine());
        assertEquals(120, reader.readLine().length());
    }
}
