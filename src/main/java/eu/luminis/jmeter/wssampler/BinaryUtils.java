package eu.luminis.jmeter.wssampler;

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

    public static boolean contains(byte[] source, byte[] value) {
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

}
