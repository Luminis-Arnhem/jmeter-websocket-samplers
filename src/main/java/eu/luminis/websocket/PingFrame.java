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

public class PingFrame extends ControlFrame {

    private byte[] applicationData;

    public PingFrame(byte[] payload) {
        super(0);
        applicationData = payload;
    }

    public PingFrame(byte[] payload, int size) {
        super(size);
        applicationData = payload;
    }

    public byte[] getData() {
        return applicationData;
    }

    @Override
    public String toString() {
        return "Ping frame with " + (applicationData.length > 0? "application data '" + new String(applicationData) + "'": "no application data");
    }

    @Override
    public boolean isPing() {
        return true;
    }

    @Override
    protected byte[] getPayload() {
        return applicationData;
    }

    @Override
    protected byte getOpCode() {
        return OPCODE_PING;
    }

    @Override
    public String getTypeAsString() {
        return "ping";
    }

    @Override
    public int getPayloadSize() {
        return applicationData.length;
    }
}
