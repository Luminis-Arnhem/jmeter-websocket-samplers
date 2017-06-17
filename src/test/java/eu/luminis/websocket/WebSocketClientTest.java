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

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.*;
import java.lang.reflect.Field;
import java.net.Socket;
import java.net.URL;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class WebSocketClientTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testCheckValidServerResponse() throws IOException {
        String serverResponse = "HTTP/1.1 101 Switching Protocols\r\nUpgrade: websocket\r\nConnection: Upgrade\r\nSec-WebSocket-Accept: s3pPLMBiTxaQ9kYGzzhZRbK+xOo=";
        String clientNonce = "dGhlIHNhbXBsZSBub25jZQ==";
        new WebSocketClient(new URL("http://nowhere")).checkServerResponse(new ByteArrayInputStream(serverResponse.getBytes()), clientNonce);
        // The test is that it gets here: the server response contains all necessary headers.
    }

    @Test
    public void testCheckInvalidServerSecAcceptHeader() throws IOException {
        String serverResponse = "HTTP/1.1 101 Switching Protocols\r\nUpgrade: websocket\r\nConnection: Upgrade\r\nSec-WebSocket-Accept: s3pPLMBiTxaQ9kYGzzhZRbK+xOo=";
        String clientNonce = "dGhlIHNhbXB_ZSBub25jZQ==";
        thrown.expect(HttpUpgradeException.class);
        thrown.expectMessage("Server response header 'Sec-WebSocket-Accept' has incorrect value");
        new WebSocketClient(new URL("http://nowhere")).checkServerResponse(new ByteArrayInputStream(serverResponse.getBytes()), clientNonce);
    }

    @Test
    public void testCheckMissingServerSecAcceptHeader() throws IOException {
        String serverResponse = "HTTP/1.1 101 Switching Protocols\r\nUpgrade: websocket\r\nConnection: Upgrade\r\n";
        String clientNonce = "dGhlIHNhbXBsZSBub25jZQ==";
        thrown.expect(HttpUpgradeException.class);
        thrown.expectMessage("Server response should contain 'Sec-WebSocket-Accept' header");
        new WebSocketClient(new URL("http://nowhere")).checkServerResponse(new ByteArrayInputStream(serverResponse.getBytes()), clientNonce);
    }

    @Test
    public void testCheckMissingServerUpgradeHeader() throws IOException {
        String serverResponse = "HTTP/1.1 101 Switching Protocols\r\nHost: whatever.com\r\nConnection: Upgrade\r\nSec-WebSocket-Accept: s3pPLMBiTxaQ9kYGzzhZRbK+xOo=";
        String clientNonce = "dGhlIHNhbXB_ZSBub25jZQ==";
        thrown.expect(HttpUpgradeException.class);
        thrown.expectMessage("Server response should contain 'Upgrade' header with value 'websocket'");
        new WebSocketClient(new URL("http://nowhere")).checkServerResponse(new ByteArrayInputStream(serverResponse.getBytes()), clientNonce);
    }

    @Test
    public void testCheckIncorrectServerUpgradeHeader() throws IOException {
        String serverResponse = "HTTP/1.1 101 Switching Protocols\r\nHost: whatever.com\r\nConnection: Upgrade\r\nUpgrade: bullocks\r\nSec-WebSocket-Accept: s3pPLMBiTxaQ9kYGzzhZRbK+xOo=";
        String clientNonce = "dGhlIHNhbXB_ZSBub25jZQ==";
        thrown.expect(HttpUpgradeException.class);
        thrown.expectMessage("Server response should contain 'Upgrade' header with value 'websocket'");
        new WebSocketClient(new URL("http://nowhere")).checkServerResponse(new ByteArrayInputStream(serverResponse.getBytes()), clientNonce);
    }

    @Test
    public void testCheckMissingServerConnectionHeader() throws IOException {
        String serverResponse = "HTTP/1.1 101 Switching Protocols\r\nHost: whatever.com\r\nUpgrade: websocket\r\nSec-WebSocket-Accept: s3pPLMBiTxaQ9kYGzzhZRbK+xOo=";
        String clientNonce = "dGhlIHNhbXB_ZSBub25jZQ==";
        thrown.expect(HttpUpgradeException.class);
        thrown.expectMessage("Server response should contain 'Connection' header with value 'Upgrade'");
        new WebSocketClient(new URL("http://nowhere")).checkServerResponse(new ByteArrayInputStream(serverResponse.getBytes()), clientNonce);
    }

    @Test
    public void testCheckIncorrectServerConnectionHeader() throws IOException {
        String serverResponse = "HTTP/1.1 101 Switching Protocols\r\nHost: whatever.com\r\nConnection: downgrade\r\nUpgrade: websocket\r\nSec-WebSocket-Accept: s3pPLMBiTxaQ9kYGzzhZRbK+xOo=";
        String clientNonce = "dGhlIHNhbXB_ZSBub25jZQ==";
        thrown.expect(HttpUpgradeException.class);
        thrown.expectMessage("Server response should contain 'Connection' header with value 'Upgrade'");
        new WebSocketClient(new URL("http://nowhere")).checkServerResponse(new ByteArrayInputStream(serverResponse.getBytes()), clientNonce);
    }

    @Test(expected = IllegalStateException.class)
    public void testSendOnClosedConnection() throws IOException {
        new WebSocketClient(new URL("http://nowhere")).sendTextFrame("illegal");
    }

    @Test(expected = IllegalStateException.class)
    public void testDoubleCloseConnection() throws IOException, UnexpectedFrameException {
        WebSocketClient client = new WebSocketClient(new URL("http://nowhere"));
        client.close(1000, "illegal close", 3000);
        client.close(1000, "illegal close", 3000);
    }

    @Test(expected = IllegalStateException.class)
    public void testReceiveOnClosedConnection() throws IOException, UnexpectedFrameException {
        new WebSocketClient(new URL("http://nowhere")).receiveText(3000);
    }

    @Test(expected = IllegalStateException.class)
    public void receiveMultipleCloseMessages() throws IOException {
        WebSocketClient client = new WebSocketClient(new URL("http://nowhere"));
        setPrivateClientField(client, "state", WebSocketClient.WebSocketState.CONNECTED);
        setPrivateClientField(client, "wsSocket", new Socket());
        setPrivateClientField(client, "socketInputStream", new ByteArrayInputStream(new CloseFrame(1000, "").getFrameBytes()));
        client.receiveFrame(1);
        setPrivateClientField(client, "socketInputStream", new ByteArrayInputStream(new CloseFrame(1000, "").getFrameBytes()));
        client.receiveFrame(1);
    }

    @Test
    public void receiveCloseLeadsToClosingState() throws IOException {
        WebSocketClient client = new WebSocketClient(new URL("http://nowhere"));
        setPrivateClientField(client, "state", WebSocketClient.WebSocketState.CONNECTED);
        setPrivateClientField(client, "wsSocket", new Socket());
        setPrivateClientField(client, "socketInputStream", new ByteArrayInputStream(new CloseFrame(1000, "").getFrameBytes()));
        Frame receivedFrame = client.receiveFrame(1);
        assertTrue(receivedFrame.isClose());
        assertTrue( ((WebSocketClient.WebSocketState) getPrivateClientField(client, "state")).isClosing());
    }

    @Test
    public void receiveCloseAndSendCloseLeadsToClosedState() throws IOException {
        WebSocketClient client = new WebSocketClient(new URL("http://nowhere"));
        setPrivateClientField(client, "state", WebSocketClient.WebSocketState.CONNECTED);
        setPrivateClientField(client, "wsSocket", new Socket());
        setPrivateClientField(client, "socketInputStream", new ByteArrayInputStream(new CloseFrame(1000, "").getFrameBytes()));
        setPrivateClientField(client, "socketOutputStream", new ByteArrayOutputStream(1024));
        client.receiveFrame(1);
        client.sendClose(1000, "whatever");
        assertEquals(WebSocketClient.WebSocketState.CLOSED, getPrivateClientField(client, "state"));
    }

    @Test
    public void sendCloseLeadsToClosingState() throws IOException {
        WebSocketClient client = new WebSocketClient(new URL("http://nowhere"));
        setPrivateClientField(client, "state", WebSocketClient.WebSocketState.CONNECTED);
        setPrivateClientField(client, "wsSocket", new Socket());
        setPrivateClientField(client, "socketInputStream", new ByteArrayInputStream(new byte[0]));
        setPrivateClientField(client, "socketOutputStream", new ByteArrayOutputStream(1024));
        client.sendClose(1000, "whatever");
        assertTrue( ((WebSocketClient.WebSocketState) getPrivateClientField(client, "state")).isClosing());
    }

    @Test
    public void sendCloseAndReceiveCloseLeadsToClosedState() throws IOException {
        WebSocketClient client = new WebSocketClient(new URL("http://nowhere"));
        setPrivateClientField(client, "state", WebSocketClient.WebSocketState.CONNECTED);
        setPrivateClientField(client, "wsSocket", new Socket());
        setPrivateClientField(client, "socketInputStream", new ByteArrayInputStream(new CloseFrame(1000, "").getFrameBytes()));
        setPrivateClientField(client, "socketOutputStream", new ByteArrayOutputStream(1024));
        client.sendClose(1000, "whatever");
        client.receiveFrame(1);
        assertEquals(WebSocketClient.WebSocketState.CLOSED, getPrivateClientField(client, "state"));
    }

    @Test
    public void testProcessingHttpResponseDoesNotEatFrameBytes() throws IOException {
        String serverResponse = "HTTP/1.1 101 Switching Protocols\r\nUpgrade: websocket\r\nConnection: Upgrade\r\nSec-WebSocket-Accept: s3pPLMBiTxaQ9kYGzzhZRbK+xOo=\r\n\r\nfirstframebytes";
        String clientNonce = "dGhlIHNhbXBsZSBub25jZQ==";
        ByteArrayInputStream bytes = new ByteArrayInputStream(serverResponse.getBytes());
        new WebSocketClient(new URL("http://nowhere")).checkServerResponse(bytes, clientNonce);
        // Check that after processing the HTTP response, all bytes that are not part of the response are still in the stream.
        assertEquals("firstframebytes", new BufferedReader(new InputStreamReader(bytes)).readLine());
    }

    @Test
    public void funnyCasedUpgradeHeaderShouldBeAccepted() throws IOException {
        String serverResponse = "HTTP/1.1 101 Switching Protocols\r\nupgRade: websocket\r\nConnection: Upgrade\r\nSec-WebSocket-Accept: s3pPLMBiTxaQ9kYGzzhZRbK+xOo=";
        String clientNonce = "dGhlIHNhbXBsZSBub25jZQ==";
        Map<String, String> headers = new WebSocketClient(new URL("http://nowhere")).checkServerResponse(new ByteArrayInputStream(serverResponse.getBytes()), clientNonce);

        // Part of the test is that it gets here: when no upgrade header is found, an exception is thrown.
        assertTrue(headers.containsKey("Upgrade"));
    }

    @Test
    public void funnyCasedConnectionHeaderShouldBeAccepted() throws IOException {
        String serverResponse = "HTTP/1.1 101 Switching Protocols\r\nUpgrade: websocket\r\nConNECtion: Upgrade\r\nSec-WebSocket-Accept: s3pPLMBiTxaQ9kYGzzhZRbK+xOo=";
        String clientNonce = "dGhlIHNhbXBsZSBub25jZQ==";
        Map<String, String> headers = new WebSocketClient(new URL("http://nowhere")).checkServerResponse(new ByteArrayInputStream(serverResponse.getBytes()), clientNonce);

        // Part of the test is that it gets here: when no upgrade header is found, an exception is thrown.
        assertTrue(headers.containsKey("Upgrade"));
    }

    @Test
    public void duplicateHeaderShouldResultInMultipleValue() throws IOException {
        String serverResponse = "HTTP/1.1 101 Switching Protocols\r\nCache-Control: no-cache\r\nCache-Control: no-store\r\nUpgrade: websocket\r\nConnection: Upgrade\r\nSec-WebSocket-Accept: s3pPLMBiTxaQ9kYGzzhZRbK+xOo=";
        String clientNonce = "dGhlIHNhbXBsZSBub25jZQ==";
        Map<String, String> headers = new WebSocketClient(new URL("http://nowhere")).checkServerResponse(new ByteArrayInputStream(serverResponse.getBytes()), clientNonce);

        // Part of the test is that it gets here: when no upgrade header is found, an exception is thrown.
        assertEquals("no-cache, no-store", headers.get("Cache-Control"));
    }

    @Test
    public void headerValueWithColonShouldNotBeTruncated() throws IOException {
        String serverResponse = "HTTP/1.1 101 Switching Protocols\r\nUser-Agent: Mozilla:4.0\r\nUpgrade: websocket\r\nConnection: Upgrade\r\nSec-WebSocket-Accept: s3pPLMBiTxaQ9kYGzzhZRbK+xOo=";
        String clientNonce = "dGhlIHNhbXBsZSBub25jZQ==";
        Map<String, String> headers = new WebSocketClient(new URL("http://nowhere")).checkServerResponse(new ByteArrayInputStream(serverResponse.getBytes()), clientNonce);

        // Part of the test is that it gets here: when no upgrade header is found, an exception is thrown.
        assertEquals("Mozilla:4.0", headers.get("User-Agent"));
    }

    @Test
    public void httpHeaderWithoutSpacesShouldBeAccepted() throws IOException {
        String serverResponse = "HTTP/1.1 101 Switching Protocols\r\nUpgrade:websocket\r\nConnection: Upgrade\r\nSec-WebSocket-Accept: s3pPLMBiTxaQ9kYGzzhZRbK+xOo=";
        String clientNonce = "dGhlIHNhbXBsZSBub25jZQ==";
        Map<String, String> headers = new WebSocketClient(new URL("http://nowhere")).checkServerResponse(new ByteArrayInputStream(serverResponse.getBytes()), clientNonce);

        // Part of the test is that it gets here: when no upgrade header is found, an exception is thrown.
        assertEquals("websocket", headers.get("Upgrade"));
    }

    @Test
    public void headerValuesWithSurroundingSpacesShouldBeTrimmed() throws IOException {
        String serverResponse = "HTTP/1.1 101 Switching Protocols\r\nUpgrade:   websocket   \r\nConnection: Upgrade\r\nSec-WebSocket-Accept: s3pPLMBiTxaQ9kYGzzhZRbK+xOo=";
        String clientNonce = "dGhlIHNhbXBsZSBub25jZQ==";
        Map<String, String> headers = new WebSocketClient(new URL("http://nowhere")).checkServerResponse(new ByteArrayInputStream(serverResponse.getBytes()), clientNonce);

        // Part of the test is that it gets here: when no upgrade header is found, an exception is thrown.
        assertEquals("websocket", headers.get("Upgrade"));
    }



    private Object getPrivateClientField(WebSocketClient client, String fieldName) {
        Field field;
        try {
            field = WebSocketClient.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(client);
        } catch (NoSuchFieldException e) {
            // Impossible
            return null;
        } catch (IllegalAccessException e) {
            // Impossible
            return null;
        }
    }

    private void setPrivateClientField(WebSocketClient client, String fieldName, Object value) {
        Field field;
        try {
            field = WebSocketClient.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(client, value);
        } catch (NoSuchFieldException e) {
            // Impossible
        } catch (IllegalAccessException e) {
            // Impossible
        }
    }

    private void setPrivateClientState(WebSocketClient client, WebSocketClient.WebSocketState newState) {
        setPrivateClientField(client, "state", newState);
    }
}
