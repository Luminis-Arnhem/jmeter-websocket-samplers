/*
 * Copyright 2016 Peter Doornbosch
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

import java.nio.charset.StandardCharsets;

public class TextFrame extends Frame {

    private String text;

    public TextFrame(String text) {
        this.text = text;
    }

    public TextFrame(byte[] payload) {
        text = new String(payload, StandardCharsets.UTF_8);
    }

    public String getText() {
        return text;
    }

    public boolean isText() {
        return true;
    }

    @Override
    public String toString() {
        return "Text frame with text '" + text + "'";
    }

    @Override
    protected byte[] getPayload() {
        return text.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    protected byte getOpCode() {
        return OPCODE_TEXT;
    }
}
