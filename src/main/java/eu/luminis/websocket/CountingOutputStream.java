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
import java.io.OutputStream;

public class CountingOutputStream extends OutputStream {

    private OutputStream wrappedStream;
    private int count = 0;

    public CountingOutputStream(OutputStream out) {
        this.wrappedStream = out;
    }

    public int getCount() {
        return count;
    }

    public void resetCount() {
        count = 0;
    }

    @Override
    public void write(int b) throws IOException {
        wrappedStream.write(b);
        count++;
    }

    @Override
    public void write(byte[] b) throws IOException {
        wrappedStream.write(b);
        count += b.length;
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        wrappedStream.write(b, off, len);
        count += len;
    }

    @Override
    public void flush() throws IOException {
        wrappedStream.flush();
    }

    @Override
    public void close() throws IOException {
        wrappedStream.close();
    }

}
