/*
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

import eu.luminis.utils.WebSocketInflaterConstants;
import org.apache.log.Logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

/**
 * Class with logic to decompress websocket messages.
 */
public class WebSocketInflater {

    private final Inflater inflater;
    private final boolean contextDecompressionEnabled;
    private final ByteArrayOutputStream compressedMessageData;
    private final ByteArrayOutputStream decompressedMessageData;

    public WebSocketInflater(boolean contextDecompressionEnabled) {
        // Condition whether message compressed with context or not.
        this.contextDecompressionEnabled = contextDecompressionEnabled;

        inflater = new Inflater(true);

        this.compressedMessageData = new ByteArrayOutputStream();
        this.decompressedMessageData = new ByteArrayOutputStream();
    }

    // For testing purposes only
    protected WebSocketInflater(Inflater inflater,
                             boolean contextDecompressionEnabled,
                             ByteArrayOutputStream compressedMessageData,
                             ByteArrayOutputStream decompressedMessageData) {

        this.inflater = inflater;

        // Condition whether message compressed with context or not.
        this.contextDecompressionEnabled = contextDecompressionEnabled;

        this.compressedMessageData = compressedMessageData;
        this.decompressedMessageData = decompressedMessageData;
    }

    /**
     * Append compressed data into dynamic output stream.
     *
     * @param compressedData - frame's compressed data.
     * @param fin            - condition whether frame is final or not.
     * @param logger         - logger to log information.
     */
    public void appendCompressedData(byte[] compressedData, boolean fin, Logger logger) {
        try {
            compressedMessageData.write(compressedData);

            if (fin && contextDecompressionEnabled) {
                //In case with compression within LZ77 Sliding window(without header "server_no_context_takeover")
                //it is needed to append 4 octets(FRAME_TAIL) to the tail end of the payload of the message
                //to correctly decompress the message.
                compressedMessageData.write(WebSocketInflaterConstants.FRAME_TAIL);
                logger.debug("compressed payload size: " + compressedMessageData.size());
            }

        } catch (IOException e) {
            logger.info("Failed to write(collect) compressed data to output stream", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Decompress completed compressed frame's message data which was collected in output stream.
     * In case with header "server_no_context_takeover" it is needed to reset inflater
     * to correctly decompress each message without context.
     *
     * @param logger - logger to log information.
     * @return decompressed message.
     */
    public byte[] decompressNextMessage(Logger logger) {
        decompressedMessageData.reset();

        inflater.setInput(compressedMessageData.toByteArray());

        byte[] buffer = new byte[1024];

        try {
            do {
                int count = inflater.inflate(buffer);
                decompressedMessageData.write(buffer, 0, count);
            }
            while (inflater.getRemaining() > 0);

        }
        catch (DataFormatException e) {
            logger.info("Compressed data format is not valid", e);
            inflater.reset();
            throw new RuntimeException(e);
        }

        if (!contextDecompressionEnabled) {
            //In case without using LZ77 Sliding window compression(with header "server_no_context_takeover")
            //it is needed to reset inflater to clean up previous message context.
            inflater.reset();
        }

        compressedMessageData.reset(); //Allow to overwrite previous compressed message data by new data

        return decompressedMessageData.toByteArray();
    }

    /**
     * Close WebSocketInflater resources.
     */
    public void close() {
        inflater.end();
        try {
            compressedMessageData.close();
            decompressedMessageData.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
