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


public class TextContinuationFrame extends TextFrame {

    public TextContinuationFrame(boolean fin, byte[] payload, int size) {
        super(fin, payload, size);
    }

    public TextContinuationFrame(boolean fin, byte[] payload, int size, boolean compressed) {
        super(fin, payload, size, compressed);
    }

    @Override
    public String getTypeAsString() {
        if (isFinalFragment())
            return "final continuation";
        else
            return "continuation";
    }

    @Override
    public boolean isContinuationFrame() {
        return true;
    }
}
