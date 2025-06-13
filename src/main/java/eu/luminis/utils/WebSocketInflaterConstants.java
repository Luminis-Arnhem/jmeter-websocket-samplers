package eu.luminis.utils;

public final class WebSocketInflaterConstants {

    public static final byte[] FRAME_TAIL = new byte[]{0, 0, -1, -1};

    private WebSocketInflaterConstants() {
        //Utility class
    }
}
