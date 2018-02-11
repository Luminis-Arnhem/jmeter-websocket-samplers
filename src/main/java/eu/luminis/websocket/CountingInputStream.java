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

public class CountingInputStream extends InputStream {

    private InputStream wrappedStream;
    private int count = 0;

    public CountingInputStream(InputStream in) {
        wrappedStream = in;
    }

    public int getCount() {
        return count;
    }

    @Override
    public int read() throws IOException {
        int read = wrappedStream.read();
        count++;
        return read;
    }

    @Override
    public int read(byte[] b) throws IOException {
        int read = wrappedStream.read(b);
        count++;
        return read;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int read = wrappedStream.read(b, off, len);
        count += len;
        return read;
    }

    @Override
    public long skip(long n) throws IOException {
        return wrappedStream.skip(n);
    }

    @Override
    public int available() throws IOException {
        return wrappedStream.available();
    }

    @Override
    public void close() throws IOException {
        wrappedStream.close();
    }

    @Override
    public synchronized void mark(int readlimit) {
        wrappedStream.mark(readlimit);
    }

    @Override
    public synchronized void reset() throws IOException {
        wrappedStream.reset();
    }

    @Override
    public boolean markSupported() {
        return wrappedStream.markSupported();
    }
}
