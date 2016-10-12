package eu.luminis.websocket;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.ByteArrayInputStream;
import java.io.IOException;

public class WebSocketClientTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testCheckValidServerResponse() throws IOException {
        String serverResponse = "HTTP/1.1 101 Switching Protocols\r\nUpgrade: websocket\r\nConnection: Upgrade\r\nSec-WebSocket-Accept: s3pPLMBiTxaQ9kYGzzhZRbK+xOo=";
        String clientNonce = "dGhlIHNhbXBsZSBub25jZQ==";
        new WebSocketClient().checkServerResponse(new ByteArrayInputStream(serverResponse.getBytes()), clientNonce);
    }

    @Test
    public void testCheckInvalidServerSecAcceptHeader() throws IOException {
        String serverResponse = "HTTP/1.1 101 Switching Protocols\r\nUpgrade: websocket\r\nConnection: Upgrade\r\nSec-WebSocket-Accept: s3pPLMBiTxaQ9kYGzzhZRbK+xOo=";
        String clientNonce = "dGhlIHNhbXB_ZSBub25jZQ==";
        thrown.expect(HttpUpgradeException.class);
        thrown.expectMessage("Server response header 'Sec-WebSocket-Accept' has incorrect value");
        new WebSocketClient().checkServerResponse(new ByteArrayInputStream(serverResponse.getBytes()), clientNonce);
    }

    @Test
    public void testCheckMissingServerSecAcceptHeader() throws IOException {
        String serverResponse = "HTTP/1.1 101 Switching Protocols\r\nUpgrade: websocket\r\nConnection: Upgrade\r\n";
        String clientNonce = "dGhlIHNhbXBsZSBub25jZQ==";
        thrown.expect(HttpUpgradeException.class);
        thrown.expectMessage("Server response should contain 'Sec-WebSocket-Accept' header");
        new WebSocketClient().checkServerResponse(new ByteArrayInputStream(serverResponse.getBytes()), clientNonce);
    }

    @Test
    public void testCheckMissingServerUpgradeHeader() throws IOException {
        String serverResponse = "HTTP/1.1 101 Switching Protocols\r\nHost: whatever.com\r\nConnection: Upgrade\r\nSec-WebSocket-Accept: s3pPLMBiTxaQ9kYGzzhZRbK+xOo=";
        String clientNonce = "dGhlIHNhbXB_ZSBub25jZQ==";
        thrown.expect(HttpUpgradeException.class);
        thrown.expectMessage("Server response should contain 'Upgrade' header with value 'websocket'");
        new WebSocketClient().checkServerResponse(new ByteArrayInputStream(serverResponse.getBytes()), clientNonce);
    }

    @Test
    public void testCheckIncorrectServerUpgradeHeader() throws IOException {
        String serverResponse = "HTTP/1.1 101 Switching Protocols\r\nHost: whatever.com\r\nConnection: Upgrade\r\nUpgrade: bullocks\r\nSec-WebSocket-Accept: s3pPLMBiTxaQ9kYGzzhZRbK+xOo=";
        String clientNonce = "dGhlIHNhbXB_ZSBub25jZQ==";
        thrown.expect(HttpUpgradeException.class);
        thrown.expectMessage("Server response should contain 'Upgrade' header with value 'websocket'");
        new WebSocketClient().checkServerResponse(new ByteArrayInputStream(serverResponse.getBytes()), clientNonce);
    }

    @Test
    public void testCheckMissingServerConnectionHeader() throws IOException {
        String serverResponse = "HTTP/1.1 101 Switching Protocols\r\nHost: whatever.com\r\nUpgrade: websocket\r\nSec-WebSocket-Accept: s3pPLMBiTxaQ9kYGzzhZRbK+xOo=";
        String clientNonce = "dGhlIHNhbXB_ZSBub25jZQ==";
        thrown.expect(HttpUpgradeException.class);
        thrown.expectMessage("Server response should contain 'Connection' header with value 'Upgrade'");
        new WebSocketClient().checkServerResponse(new ByteArrayInputStream(serverResponse.getBytes()), clientNonce);
    }

    @Test
    public void testCheckIncorrectServerConnectionHeader() throws IOException {
        String serverResponse = "HTTP/1.1 101 Switching Protocols\r\nHost: whatever.com\r\nConnection: downgrade\r\nUpgrade: websocket\r\nSec-WebSocket-Accept: s3pPLMBiTxaQ9kYGzzhZRbK+xOo=";
        String clientNonce = "dGhlIHNhbXB_ZSBub25jZQ==";
        thrown.expect(HttpUpgradeException.class);
        thrown.expectMessage("Server response should contain 'Connection' header with value 'Upgrade'");
        new WebSocketClient().checkServerResponse(new ByteArrayInputStream(serverResponse.getBytes()), clientNonce);
    }

}
