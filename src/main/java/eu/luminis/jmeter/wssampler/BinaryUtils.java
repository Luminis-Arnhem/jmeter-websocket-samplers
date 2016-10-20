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
            builder.append(String.format("%#x ", b));
        return builder.toString();
    }

    private static byte[] toByteArray(List<Byte> bytes) {
        byte[] result = new byte[bytes.size()];
        for (int i = 0; i < bytes.size(); i++)
            result[i] = bytes.get(i);
        return result;
    }

}
