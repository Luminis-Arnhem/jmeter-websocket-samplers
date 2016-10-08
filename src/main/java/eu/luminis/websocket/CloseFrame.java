package eu.luminis.websocket;

public class CloseFrame extends Frame {

    private Integer closeStatus;
    private String closeReason;

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
}


