package eu.luminis.jmeter.wssampler;

import eu.luminis.websocket.BinaryFrame;
import eu.luminis.websocket.Frame;

public class BinaryFrameFilter extends FrameFilter {

    int matchPosition;
    byte[] matchValue;

    public enum ComparisonType {
        Equals,
        Contains,
        ContainsWithExactPosition,
        StartsWith,
        NotEquals,
        NotContains,
        NotStartsWith
    }

    @Override
    protected void prepareFilter() {
        matchPosition = getMatchPosition();
        matchValue = BinaryUtils.parseBinaryString(getMatchValue());
    }

    @Override
    protected boolean matchesFilter(Frame frame) {
        if (frame.isBinary()) {
            BinaryFrame receivedFrame = (BinaryFrame) frame;
            byte[] frameBytes = receivedFrame.getData();

            // 1st impl: only support contains with start position
            if (matchPosition + matchValue.length < frameBytes.length) {
                return equalBytes(frameBytes, matchPosition, matchValue, 0, matchValue.length);
            }
            else
                return false;
        }
        else
            return false;
    }

    static boolean equalBytes(byte[] frameBytes, int matchPosition, byte[] matchValue, int valuePosition, int length) {
        if (matchPosition + length > frameBytes.length || valuePosition + length > matchValue.length)
            return false;

        for (int i = 0; i < length; i++) {
            if (frameBytes[matchPosition + i] != matchValue[valuePosition + i])
                return false;
        }
        return true;
    }

    public int getMatchPosition() {
        return getPropertyAsInt("matchPosition");
    }

    public void setMatchPosition(int value) {
        setProperty("matchPosition", value);
    }

    public String getMatchValue() {
        return getPropertyAsString("matchValue");
    }

    public void setMatchValue(String value) {
        setProperty("matchValue", value);
    }
}
