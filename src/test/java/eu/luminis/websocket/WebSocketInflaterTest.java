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
import org.slf4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import static junit.framework.TestCase.assertEquals;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class WebSocketInflaterTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebSocketInflaterTest.class);

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
        // Given
        WebSocketInflater webSocketInflater = new WebSocketInflater(true);

        // When
        webSocketInflater.appendCompressedData(new byte[0], true, LOGGER);
        byte[] actualDecompressedData = webSocketInflater.decompressNextMessage(LOGGER);

        // Then
        assertThat(actualDecompressedData)
                .isNotNull()
                .isEmpty();
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

    @Test
    public void compressedDataLongerThenBufferLengthShouldBeDecompressedCompletely() {
        // Given
        byte[] compressedData = hexStringToByteArray("8c554d6b1c3910bde757889c12b0076659c7f868bc63082410e290850d3ed448" +
                "35d3c26aa923a96732ff3eafd46aab67d7863dd996ebe3d57bafaad72ba53efa1c831975b6c1bf79a394fad6d9a44cd063cf" +
                "3eab34b0b63bcb4991da45eaf918e293da85a8c818ebf74a877e889c92a4237b37fa528a9ccd279583ca1dabbf79fb10f413" +
                "67f525861c7470eac7d7fbbb0f7f5e5d3daea423b7da52a435edc2518a18de597f5687e3e56774a53dabbb86406d7e65f6f2" +
                "5b923aefbe7cbedba4f713dc2552456e1fa2cd5dafb694d8283c09509ed311ec350f59859d147a658819a751d6ab072e83ab" +
                "1be49c8d772b155ab6765688256f8069608e8bff258e073c78de876c2973693c269692246564a0926a3873ec8514091948e8" +
                "c34b52917f8e36021268c3103bbb1f630992f4972930631425cf870c037b79edd02d75f4c49350a54a9ba002d654f8f32050" +
                "2301a218caa4fa49a1a48e5d4855e334e1f099ac7fc603b825c1fa3acec905aa6f4388c2ebaa99b3f93078773ab3e89e3d47" +
                "72e89cbb6026e187c19d64127a9d81ea538185c992d0dd9898a700011bd29d54b1ded88335233a15453a4a5263cbe796a0b6" +
                "4786938e762b386c59140848d6cd063f577cd68d8aa116e2ca3c1250bbe8d7271a85d32d36b0a3dc5c53bbe5483ea1565f96" +
                "642e507d35fff9fe7f4a72fb5f6ff77402853b18a31f5db683e30221bde834a9f192d96e5f598ec89a417e2d53dbd0a950d1" +
                "05f11926245db6b7208f8c10d988420318c48c4e36879c13a511d4afa6d52ae6947d93c4af0fdfd76a6b738d59202906541d"+
                "93416f74133b6811efd8c177782b56abbe5170ecc2e7e8ef0386099334581cf6660816b4c916d511242487a5bc8b6582c068" +
                "f9acfbd635e7ae5e38e1e4d0ac2d49f0cd3f13256727f0afcdfda7db6f1b2923476c7d73b57e6cbeaad7ba062dfc66059761" +
                "ac231de06b1250d317e140d18631a9c15116cfa58b62342b6b063d8545c94e3de4a8e5db19f642f47c831155f0e2e7db01d7" +
                "6f22e4121f0794e6b7920ce59dddd7638e2e90ae715fd64efc2b06f12ae80c2db761f486e2e9626ad05ac306a9d4791e725a" +
                "dbb2c7e727ff8fd57a3efaeb9beb1b39fa9b2a6a9a6e232c5da69d0ff1a77faeaf5572b67c428ff00f16f3873c3e4e9b2bc7" +
                "64b4ced49b59161f531c0a93b3130a2be2f5cef20138717f72966bbcb808e58600cf7d90cd49618c9a2f9ded6d969b8b829a" +
                "d3c533bd6dfa21061c38745e5c1ff42a9900d087780252b137a0fdfb0c1537fecaabdf00");

        Inflater inflater = spy(new Inflater(true));
        WebSocketInflater webSocketInflater = new WebSocketInflater(inflater, true, new ByteArrayOutputStream(), new ByteArrayOutputStream());

        // When
        webSocketInflater.appendCompressedData(compressedData, true, LOGGER);
        byte[] actualDecompressedData = webSocketInflater.decompressNextMessage(LOGGER);

        // Then
        assertThat(new String(actualDecompressedData))
                .isNotNull()
                .startsWith("1.  Introduction")
                .endsWith("compression context.")
                .hasSize(2144);
        verify(inflater, never()).end();
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

    public static byte[] hexStringToByteArray(String hex) {
        if (hex == null || hex.length() % 2 != 0) {
            throw new IllegalArgumentException("Hex string must be non-null and have even length.");
        }

        int length = hex.length();
        byte[] result = new byte[length / 2];

        for (int i = 0; i < length; i += 2) {
            int byteValue = Integer.parseInt(hex.substring(i, i + 2), 16);
            result[i / 2] = (byte) byteValue;
        }

        return result;
    }

}