/*
 * Copyright © 2025 Peter Doornbosch
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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class JsonFormatter {

    public static String formatJson(InputStream inputStream) throws IOException {
        String json = readInputStream(inputStream);
        return formatJson(json);
    }

    private static String readInputStream(InputStream inputStream) throws IOException {
        StringBuilder jsonBuilder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                jsonBuilder.append(line.trim());
            }
        }
        return jsonBuilder.toString();
    }

    private static String formatJson(String json) {
        if (json == null || json.isEmpty()) {
            return json;
        }

        StringBuilder prettyJson = new StringBuilder();
        String indentStr = "  "; // 2 spaces
        int indentLevel = 0;
        boolean inQuotes = false;
        boolean escape = false;

        for (char c : json.toCharArray()) {
            if (escape) {
                prettyJson.append(c);
                escape = false;
                continue;
            }

            switch (c) {
                case '\\':
                    escape = true;
                    prettyJson.append(c);
                    break;

                case '"':
                    inQuotes = !inQuotes;
                    prettyJson.append(c);
                    break;

                case '{':
                case '[':
                    prettyJson.append(c);
                    if (!inQuotes) {
                        prettyJson.append('\n');
                        indentLevel++;
                        appendIndentation(prettyJson, indentLevel, indentStr);
                    }
                    break;

                case '}':
                case ']':
                    if (!inQuotes) {
                        prettyJson.append('\n');
                        indentLevel--;
                        appendIndentation(prettyJson, indentLevel, indentStr);
                        prettyJson.append(c);
                    } else {
                        prettyJson.append(c);
                    }
                    break;

                case ',':
                    prettyJson.append(c);
                    if (!inQuotes) {
                        prettyJson.append('\n');
                        appendIndentation(prettyJson, indentLevel, indentStr);
                    }
                    break;

                case ':':
                    prettyJson.append(c);
                    if (!inQuotes) {
                        prettyJson.append(' ');
                    }
                    break;

                default:
                    prettyJson.append(c);
                    break;
            }
        }

        return prettyJson.toString();
    }

    private static void appendIndentation(StringBuilder sb, int indentLevel, String indentStr) {
        for (int i = 0; i < indentLevel; i++) {
            sb.append(indentStr);
        }
    }

    public static void main(String[] args) throws IOException {
        String sampleJson = "{\"name\":\"John\",\"age\":30,\"isStudent\":false,\"courses\":[\"Math\",\"Science\"],\"address\":{\"city\":\"New York, New York\",\"zip\":\"10001\"}}";

        InputStream inputStream = new ByteArrayInputStream(sampleJson.getBytes("UTF-8"));
        String pretty = formatJson(inputStream);
        System.out.println(pretty);
    }
}
