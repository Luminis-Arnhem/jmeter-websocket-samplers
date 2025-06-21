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

public abstract class DataFrame extends Frame {

    protected final boolean compressed;
    protected boolean finalFragment = true;

    public DataFrame() {
        super(0);
        compressed = false;
    }

    public DataFrame(boolean fin, int size) {
        super(size);
        finalFragment = fin;
        compressed = false;
    }

    public DataFrame(boolean fin, int size, boolean compressed) {
        super(size);
        finalFragment = fin;
        this.compressed = compressed;
    }

    public abstract Object getData();

    public boolean isFinalFragment() {
        return finalFragment;
    }

    public boolean isContinuationFrame() {
        return false;
    }

    public boolean isCompressed() {
        return compressed;
    }
}
