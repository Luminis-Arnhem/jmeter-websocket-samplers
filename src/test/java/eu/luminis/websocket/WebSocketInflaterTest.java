package eu.luminis.websocket;

import eu.luminis.utils.WebSocketInflaterConstants;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import static junit.framework.TestCase.assertEquals;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class WebSocketInflaterTest {

    private static final Logger LOGGER = LoggingManager.getLoggerForClass();

    private static final byte[] COMPRESSED_DATA = "compressedData".getBytes();
    private static final byte[] DECOMPRESSED_DATA = "decompressedData".getBytes();

    @Mock
    Inflater inflater;

    @Mock
    ByteArrayOutputStream compressedData;

    @Mock
    ByteArrayOutputStream decompressedData;

    WebSocketInflater webSocketInflaterWithContextDecompression;

    WebSocketInflater webSocketInflaterWithoutContextDecompression;

    @Before
    public void setUp() {
        webSocketInflaterWithContextDecompression =
                new WebSocketInflater(inflater, true, compressedData, decompressedData);

        webSocketInflaterWithoutContextDecompression =
                new WebSocketInflater(inflater, false, compressedData, decompressedData);
    }

    @Test
    public void appendCompressedDataWithContextDecompressionAndFinalFragment() throws IOException {

        //GIVEN
        doNothing().when(compressedData).write(any(byte[].class));

        //WHEN
        webSocketInflaterWithContextDecompression.appendCompressedData(COMPRESSED_DATA, true, LOGGER);

        //THEN
        verify(compressedData, times(2)).write(any(byte[].class));
        verify(compressedData).write(COMPRESSED_DATA);
        verify(compressedData).write(WebSocketInflaterConstants.FRAME_TAIL);
    }

    @Test
    public void appendCompressedDataWithContextDecompressionAndNotFinalFragment() throws IOException {

        //GIVEN
        doNothing().when(compressedData).write(COMPRESSED_DATA);

        //WHEN
        webSocketInflaterWithContextDecompression.appendCompressedData(COMPRESSED_DATA, false, LOGGER);

        //THEN
        verify(compressedData).write(any(byte[].class));
        verify(compressedData).write(COMPRESSED_DATA);
    }

    @Test
    public void appendCompressedDataWithoutContextDecompressionAndFinalFragment() throws IOException {

        //GIVEN
        doNothing().when(compressedData).write(COMPRESSED_DATA);

        //WHEN
        webSocketInflaterWithoutContextDecompression.appendCompressedData(COMPRESSED_DATA, true, LOGGER);

        //THEN
        verify(compressedData).write(any(byte[].class));
        verify(compressedData).write(COMPRESSED_DATA);
    }

    @Test
    public void appendCompressedDataWithoutContextDecompressionAndNotFinalFragment() throws IOException {

        //GIVEN
        doNothing().when(compressedData).write(COMPRESSED_DATA);

        //WHEN
        webSocketInflaterWithoutContextDecompression.appendCompressedData(COMPRESSED_DATA, false, LOGGER);

        //THEN
        verify(compressedData).write(any(byte[].class));
        verify(compressedData).write(COMPRESSED_DATA);
    }

    @Test(expected = RuntimeException.class)
    public void appendCompressedDataThenThrowIOException() throws IOException {

        //GIVEN
        doThrow(IOException.class).when(compressedData).write(COMPRESSED_DATA);

        //WHEN
        webSocketInflaterWithContextDecompression.appendCompressedData(COMPRESSED_DATA, true, LOGGER);
    }

    @Test
    public void decompressNextMessageWithDecompressionContext() throws DataFormatException {

        //GIVEN
        int decompressedByteArrayLength = DECOMPRESSED_DATA.length;

        mockInflater(decompressedByteArrayLength);

        //WHEN
        byte[] actualDecompressedData = webSocketInflaterWithContextDecompression.decompressNextMessage(LOGGER);

        //THEN
        verifyInflater(
                actualDecompressedData,
                decompressedByteArrayLength,
                0,
                1);
    }

    @Test
    public void decompressNextMessageWithoutDecompressionContext() throws DataFormatException {

        //GIVEN
        int decompressedByteArrayLength = DECOMPRESSED_DATA.length;

        mockInflater(decompressedByteArrayLength);

        //WHEN
        byte[] actualDecompressedData = webSocketInflaterWithoutContextDecompression.decompressNextMessage(LOGGER);

        //THEN
        verifyInflater(
                actualDecompressedData,
                decompressedByteArrayLength,
                1,
                1);
    }

    @Test
    public void decompressNextMessageIfNumberOfDecompressedBytesIsNull() throws DataFormatException {

        //GIVEN
        int decompressedByteArrayLength = 0;

        mockInflater(decompressedByteArrayLength);

        //WHEN
        byte[] actualDecompressedData = webSocketInflaterWithoutContextDecompression.decompressNextMessage(LOGGER);

        //THEN
        verifyInflater(
                actualDecompressedData,
                decompressedByteArrayLength,
                1,
                0);
    }

    @Test(expected = RuntimeException.class)
    public void decompressNextMessageThenThrowDataFormatException() throws DataFormatException {

        //GIVEN
        when(compressedData.toByteArray()).thenReturn(COMPRESSED_DATA);

        doNothing().when(inflater).setInput(COMPRESSED_DATA);
        when(inflater.inflate(any(byte[].class))).thenThrow(DataFormatException.class);

        //WHEN
        webSocketInflaterWithContextDecompression.decompressNextMessage(LOGGER);
    }

    @Test
    public void close() throws IOException {

        //GIVEN
        doNothing().when(inflater).end();
        doNothing().when(compressedData).close();
        doNothing().when(decompressedData).close();

        //WHEN
        webSocketInflaterWithContextDecompression.close();

        //THEN
        verify(inflater).end();
        verify(compressedData).close();
        verify(decompressedData).close();
    }

    @Test(expected = RuntimeException.class)
    public void closeThenThrowIOException() throws IOException {

        //GIVEN
        doNothing().when(inflater).end();
        doThrow(IOException.class).when(compressedData).close();

        //WHEN
        webSocketInflaterWithContextDecompression.close();
    }

    private void mockInflater(int decompressedDataLength) throws DataFormatException {
        when(compressedData.toByteArray()).thenReturn(COMPRESSED_DATA);
        doNothing().when(compressedData).reset();

        doNothing().when(inflater).setInput(COMPRESSED_DATA);
        when(inflater.inflate(any(byte[].class))).thenReturn(decompressedDataLength);
        doNothing().when(inflater).reset();

        doNothing().when(decompressedData).write(any(byte[].class), eq(0), eq(decompressedDataLength));
        doNothing().when(decompressedData).reset();
        when(decompressedData.toByteArray()).thenReturn(DECOMPRESSED_DATA);
    }

    private void verifyInflater(byte[] actualDecompressedData,
                                int decompressedDataLength,
                                int numberOfInflaterResettingInvocations,
                                int numberOfDecompressedDataResettingAndWritingInvocations) throws DataFormatException {

        assertEquals(DECOMPRESSED_DATA, actualDecompressedData);

        verify(compressedData).toByteArray();
        verify(compressedData).reset();

        verify(inflater).setInput(COMPRESSED_DATA);
        verify(inflater).inflate(any(byte[].class));
        verify(inflater, times(numberOfInflaterResettingInvocations)).reset();

        verify(decompressedData, times(numberOfDecompressedDataResettingAndWritingInvocations)).reset();

        verify(decompressedData, times(numberOfDecompressedDataResettingAndWritingInvocations))
                .write(any(byte[].class), eq(0), eq(decompressedDataLength));

        verify(decompressedData).toByteArray();
    }
}