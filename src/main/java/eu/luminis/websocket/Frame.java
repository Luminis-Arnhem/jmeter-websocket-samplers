/*
 * Copyright 2016, 2017 Peter Doornbosch
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
import java.util.Random;

public abstract class Frame {

    public static final int OPCODE_CONT = 0x00;
    public static final int OPCODE_TEXT = 0x01;
    public static final int OPCODE_BINARY = 0x02;
    public static final int OPCODE_CLOSE = 0x08;
    public static final int OPCODE_PING = 0x09;
    public static final int OPCODE_PONG = 0x0a;
    public static final int FIN_BIT_ON = 0x80;
    public static final int MASK_BIT_MASKED = 0x80;

    static private Random randomGenerator = new Random();

    private int frameSize;


    static Frame parseFrame(InputStream istream) throws IOException {
        int byte1 = istream.read();
        if (byte1 == -1)
            throw new EndOfStreamException("end of stream");
        int byte2 = istream.read();
        if (byte2 == -1)
            throw new EndOfStreamException("end of stream");

        int opCode = byte1 & 0x0f;
        int firstLengthByte = byte2 & 0x7f;
        int length;
        int nrOfLenghtBytes = 0;
        if (firstLengthByte < 126)
            length = firstLengthByte;
        else if (firstLengthByte == 126) {
            byte1 = istream.read();
            if (byte1 == -1)
                throw new EndOfStreamException("end of stream");
            byte2 = istream.read();
            if (byte2 == -1)
                throw new EndOfStreamException("end of stream");
            length = ((byte1 & 0xff) << 8) | (byte2 & 0xff);
            nrOfLenghtBytes = 2;
        }
        else {
            byte[] lengthBytes = new byte[8];
            int bytesRead = readFromStream(istream, lengthBytes);
            if (bytesRead != lengthBytes.length)
                throw new EndOfStreamException("WebSocket protocol error: expected " + lengthBytes.length + " length bytes, but can only read " + bytesRead + " bytes");
            // If most signicifant word (32 bytes) of length are non-zero, it results in an unsupported length (must fit in a Java int)
            if (lengthBytes[0] != 0  || lengthBytes[1] != 0  || lengthBytes[2] != 0  || lengthBytes[3] != 0 )
                throw new RuntimeException("Frame too large; Java does not support arrays longer than 2147483647 bytes.");
            // Must check for most significant bit on least significant word set to avoid negative array size
            if ( (lengthBytes[4] & 0x80) == 128)
                throw new RuntimeException("Frame too large; Java does not support arrays longer than 2147483647 bytes.");
            length = ( (lengthBytes[4] & 0xff) << 24) | ((lengthBytes[5] & 0xff) << 16) | ((lengthBytes[6] & 0xff) << 8) | ((lengthBytes[7] & 0xff) << 0);
            nrOfLenghtBytes = 8;
        }
        byte[] payload = new byte[length];  // Note that this can still throw an OutOfMem, as the max array size is JVM dependent.
        int bytesRead = readFromStream(istream, payload);
        if (bytesRead == -1)
            throw new EndOfStreamException("end of stream");
        if (bytesRead != length)
            throw new EndOfStreamException("WebSocket protocol error: expected payload of length " + length + ", but can only read " + bytesRead + " bytes");
        switch (opCode) {
            case OPCODE_TEXT:
                return new TextFrame(payload, 2 + nrOfLenghtBytes + length);
            case OPCODE_BINARY:
                return new BinaryFrame(payload, 2 + nrOfLenghtBytes + length);
            case OPCODE_CLOSE:
                return new CloseFrame(payload, 2 + nrOfLenghtBytes + length);
            case OPCODE_PING:
                return new PingFrame(payload, 2 + nrOfLenghtBytes + length);
            case OPCODE_PONG:
                return new PongFrame(payload, 2 + nrOfLenghtBytes + length);
            default:
                throw new RuntimeException("unsupported frame type: " + opCode);
        }
    }

    protected Frame(int size) {
        frameSize = size;
    }

    public byte[] getFrameBytes() {
        // Generate mask
        byte[] mask = new byte[4];
        randomGenerator.nextBytes(mask);
        // Mask payload
        byte[] payload = getPayload();
        byte[] lengthBytes;
        if (payload.length <= 125) {
            lengthBytes = new byte[1];
            lengthBytes[0] = (byte) payload.length;
        }
        else if (payload.length < 65536) {  // 2 ^ 16 = 65536
            lengthBytes = new byte[3];
            lengthBytes[0] = (byte) 126;
            lengthBytes[1] = (byte) (payload.length >> 8);
            lengthBytes[2] = (byte) (payload.length >> 0);
        }
        else {
            // In Java, a byte[] cannot be larger than max-int (which is 2 ^ 31 - 1), which fits in 4 bytes
            // So, the most significant 4 bytes in the 8-byte length are never used.
            lengthBytes = new byte[9];
            lengthBytes[0] = (byte) 127;
            lengthBytes[1] = 0;
            lengthBytes[2] = 0;
            lengthBytes[3] = 0;
            lengthBytes[4] = 0;
            lengthBytes[5] = (byte) (payload.length >> 24);
            lengthBytes[6] = (byte) (payload.length >> 16);
            lengthBytes[7] = (byte) (payload.length >> 8);
            lengthBytes[8] = (byte) (payload.length >> 0);
        }

        byte[] masked = new byte[payload.length];
        for (int i = 0; i < payload.length; i++) {
            masked[i] = (byte) (payload[i] ^ mask[i%4]);
        }
        // Create frame bytes
        byte[] frame = new byte[1 + lengthBytes.length + 4 + payload.length];
        frame[0] = (byte) (FIN_BIT_ON | getOpCode());
        frame[1] = (byte) (MASK_BIT_MASKED | lengthBytes[0]);
        System.arraycopy(lengthBytes, 1, frame, 2, lengthBytes.length - 1);
        System.arraycopy(mask, 0, frame, 1 + lengthBytes.length, 4);
        System.arraycopy(masked, 0, frame, 1 + lengthBytes.length + 4, payload.length);
        return frame;
    }

    /**
     *  Read from stream until expected number of bytes is read, or the stream is closed. So, this method might block!
     *  (Note that this is the difference with java.io.BufferedInputStream: that reads as much as available.)
     */
    protected static int readFromStream(InputStream stream, byte[] buffer) throws IOException {
        return readFromStream(stream, buffer, 0, buffer.length);
    }

    /**
     *  Read from stream until expected number of bytes is read, or the stream is closed. So, this method might block!
     *  (Note that this is the difference with java.io.BufferedInputStream: that reads as much as available.)
     */
    protected static int readFromStream(InputStream stream, byte[] buffer, int offset, int expected) throws IOException {
        int toRead = expected;
        int totalRead = 0;
        do {
            int bytesRead = stream.read(buffer, offset, toRead);
            if (bytesRead < 0)  // -1: Stream is at end of file
                return totalRead;
            if (bytesRead == 0)   // Should not happen according to Javadoc, but just in case... avoid endless loop
                return totalRead;
            totalRead += bytesRead;
            offset += bytesRead;
            toRead = expected - totalRead;
        }
        while (totalRead < expected);
        return totalRead;
    }

    public boolean isText() {
        return false;
    }

    public boolean isBinary() {
        return false;
    }

    public boolean isClose() {
        return false;
    }

    public boolean isPing() {
        return false;
    }

    public boolean isPong() {
        return false;
    }

    protected abstract byte[] getPayload();

    protected abstract byte getOpCode();

    public abstract String getTypeAsString();

    public int getSize() {
        return frameSize;
    }
}
