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

import java.io.IOException;
import java.io.InputStream;

class HttpLineReader {

    private InputStream in;

    public HttpLineReader(InputStream in) {
        this.in = in;
    }

    public String readLine() throws IOException {
        byte[] buffer = new byte[64];
        int index = 0;

        int byteRead;
        do {
            byteRead = in.read();
            if (byteRead != -1 && byteRead != '\r' && byteRead != '\n') {
                if (index == buffer.length) {
                    byte[] largerBuffer = new byte[buffer.length * 2];
                    System.arraycopy(buffer, 0, largerBuffer, 0, index);
                    buffer = largerBuffer;
                }
                buffer[index++] = (byte) byteRead;
            }
        }
        // Conform to https://www.w3.org/Protocols/rfc2616/rfc2616-sec19.html#sec19.3, just look for linefeed
        while (byteRead != -1 && byteRead != '\n');

        if (index > 0 || byteRead != -1)
            return new String(buffer, 0, index, "US-ASCII");
        else
            return null;
    }

}
