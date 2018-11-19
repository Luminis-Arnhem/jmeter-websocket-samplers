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
package eu.luminis.jmeter.wssampler;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

public class BinaryUtils {

    public static byte[] parseBinaryString(String data) {
        if (data.trim().length() > 0) {
            String[] bytes = data.split(" ");
            List<Byte> result = new ArrayList<>();
            for (String element : bytes) {
                if (element.startsWith("0x"))
                    result.add(Integer.decode(element).byteValue());
                else {
                    for (int i = 0; i < element.length(); i += 2) {
                        String hexByte = element.substring(i, Math.min(element.length(), i+2));
                        result.add((byte) Integer.parseInt(hexByte, 16));
                    }
                }
            }
            return toByteArray(result);
        }
        else
            return new byte[0];
    }

    public static String formatBinary(byte[] data) {
        StringBuilder builder = new StringBuilder();
        for (byte b: data)
            builder.append(String.format("%#04x ", b));  // # defines the leading 0x, which takes 2 chars, hence width is 2 + 2 = 4
        return builder.toString().trim();
    }

    public static String formatBinaryInTable(byte[] data, int rowLength, boolean showRowAddress, boolean showAscii) {
        return formatBinaryInTable(data, data.length, rowLength, showRowAddress, showAscii);
    }

    public static String formatBinaryInTable(byte[] data, int dataSize, int rowLength, boolean showRowAddress, boolean showAscii) {
        StringBuilder builder = new StringBuilder();
        int i;
        for (i = 0; i < dataSize; i++) {
            if (showRowAddress && i % rowLength == 0)
                builder.append(String.format("%04x  ", i));
            builder.append(String.format("%02x", data[i]));
            if ((i + 1) % rowLength == 0) {
                if (showAscii) {
                    builder.append("  ");
                    for (int j = i + 1 - rowLength; j <= i; j++) {
                        if (isPrintableChar((char) data[j]))
                            builder.append((char) data[j]);
                        else
                            builder.append(".");
                        if ((j + 1) % rowLength % 8 == 0 && (j + 1) % rowLength != 0)
                            builder.append(" ");
                    }
                }
                builder.append("\n");
            } else if ((i + 1) % rowLength % 8 == 0)
                builder.append("  ");
            else if (i != dataSize - 1)
                builder.append(" ");
            else {
                if (showAscii) {
                    for (int j = i + 1; j % rowLength != 0; j++) {
                        builder.append("   ");
                        if (j % rowLength % 8 == 0)
                            builder.append(" ");
                    }
                    builder.append("  ");
                    int j;
                    for (j = i - (i % rowLength); j <= i; j++) {
                        if (isPrintableChar((char) data[j]))
                            builder.append((char) data[j]);
                        else
                            builder.append(".");
                        if ((j + 1) % rowLength % 8 == 0 && (j + 1) % rowLength != 0)
                            builder.append(" ");
                    }
                }
                builder.append("\n");
            }
        }

        return builder.toString();
    }

    public static String formatBinary(byte[] data, int maxBytes, String truncationSuffix) {
        StringBuilder builder = new StringBuilder();
        int byteCount = Math.min(data.length, maxBytes);
        for (int i = 0; i < byteCount; i++)
            builder.append(String.format("%#04x ", data[i]));  // # defines the leading 0x, which takes 2 chars, hence width is 2 + 2 = 4
        if (byteCount < data.length)
            return builder.toString().trim() + truncationSuffix;
        else
            return builder.toString().trim();
    }

    public static boolean contains(byte[] source, byte[] value) {
        if (value.length == 0)
            return false;

        for (int i = 0; i < source.length; i++) {
            if (value[0] == source[i]) {
                boolean equal = true;
                for (int j = 0; equal && j < value.length; j++) {
                    if (i+j >= source.length || value[j] != source[i+j])
                        equal = false;
                }
                if (equal)
                    return true;
            }
        }
        return false;
    }

    private static byte[] toByteArray(List<Byte> bytes) {
        byte[] result = new byte[bytes.size()];
        for (int i = 0; i < bytes.size(); i++)
            result[i] = bytes.get(i);
        return result;
    }

    public static boolean isPrintableChar( char c ) {
        Character.UnicodeBlock block = Character.UnicodeBlock.of( c );
        return (!Character.isISOControl(c)) &&
                c != KeyEvent.CHAR_UNDEFINED &&
                block != null &&
                block != Character.UnicodeBlock.SPECIALS;
    }
}
