package eu.luminis.websocket;

import org.junit.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class PingFrameTest {

    @Test
    public void toStringShouldContainApplicationData() {
        Frame frame = new PingFrame("foo".getBytes());

        assertThat(frame.toString()).contains("foo");
    }

    @Test
    public void toStringShouldMentionNoApplicationData() {
        Frame frame = new PingFrame("".getBytes());

        assertThat(frame.toString()).contains("with no application data");
    }
}
