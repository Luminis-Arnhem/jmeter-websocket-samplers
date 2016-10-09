package eu.luminis.websocket;

import java.nio.charset.StandardCharsets;

public class CloseFrame extends Frame {

    private Integer closeStatus;
    private String closeReason;

    public CloseFrame(int status, String requestData) {
        closeStatus = status;
        closeReason = requestData;
    }

    public CloseFrame(byte[] payload) {
        if (payload.length >= 2) {
            closeStatus = (payload[0] << 8) | (payload[1] & 0xff);
        }
        if (payload.length > 2) {
            closeReason = new String(payload, 2, payload.length - 2);
        }
    }

    public Integer getCloseStatus() {
        return closeStatus;
    }

    public String getCloseReason() {
        return closeReason;
    }

    public boolean isClose() {
        return true;
    }

    @Override
    public String toString() {
        return "Close frame with status code " + closeStatus + " and close reason '" + closeReason + "'";
    }

    @Override
    protected byte[] getPayload() {
        byte[] data = closeReason.getBytes(StandardCharsets.UTF_8);
        byte[] payload = new byte[2 + data.length];
        System.arraycopy(data, 0, payload, 2, data.length);

        payload[0] = (byte) (closeStatus >> 8);
        payload[1] = (byte) (closeStatus & 0xff);
        return payload;
    }

    @Override
    protected byte getOpCode() {
        return OPCODE_CLOSE;
    }
}


