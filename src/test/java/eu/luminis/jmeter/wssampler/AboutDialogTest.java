package eu.luminis.jmeter.wssampler;

import org.junit.Test;
import static org.junit.Assert.*;

public class AboutDialogTest {

    @Test
    public void testGetHighestVersion() throws Exception {
        String content = "dist-0.5.jar dist-0.6.jar";
        assertEquals("0.6", AboutDialog.getHigherVersion(content, "0.5"));
    }

    @Test
    public void testGetHighestVersionDifferentOrder() throws Exception {
        String content = "dist-0.5.jar dist-0.3.jar";
        assertEquals("0.5", AboutDialog.getHigherVersion(content, "0.3"));
    }

    @Test
    public void testGetHighestVersionWithOlder() throws Exception {
        String content = "dist-0.5.jar dist-0.3.jar";
        assertEquals("0.5", AboutDialog.getHigherVersion(content, "0.1"));
    }

    @Test
    public void testGetHighestVersionWithMicros() throws Exception {
        String content = "dist-0.5.1.jar dist-0.5.8.jar";
        assertEquals("0.5.8", AboutDialog.getHigherVersion(content, "0.5.1"));
    }

    @Test
    public void testGetHighestVersionWithSameMicros() throws Exception {
        String content = "dist-0.5.1.jar dist-0.5.8.jar";
        assertEquals(null, AboutDialog.getHigherVersion(content, "0.5.8"));
    }

    @Test
    public void testGetHighestVersionWithLargeMicros() throws Exception {
        String content = "dist-0.5.9.jar dist-0.5.10.jar";
        assertEquals("0.5.10", AboutDialog.getHigherVersion(content, "0.5.9"));
    }

    @Test
    public void testGetHighestVersionWithMicro1() throws Exception {
        String content = "dist-0.5.9.jar dist-0.5.jar";
        assertEquals("0.5.9", AboutDialog.getHigherVersion(content, "0.5.4"));
    }

    @Test
    public void testGetHighestVersionWithMicro2() throws Exception {
        String content = "dist-0.5.9.jar dist-0.6.jar";
        assertEquals("0.6", AboutDialog.getHigherVersion(content, "0.5.9"));
    }

    @Test
    public void testGetHighestVersionWithMajorVsMicro() throws Exception {
        String content = "dist-1.0.jar dist-0.6.jar";
        assertEquals("1.0", AboutDialog.getHigherVersion(content, "0.6"));
    }

    @Test
    public void testGetHighestVersionWithMajors() throws Exception {
        String content = "dist-1.0.jar dist-1.0.3.jar";
        assertEquals("1.0.3", AboutDialog.getHigherVersion(content, "1.0"));
    }

    @Test(expected = AboutDialog.CannotDetermineUpdateException.class)
    public void testGetHighestVersionUnparseable() throws Exception {
        String content = "dist.jar dist-x.jar dist-8.jar";
        AboutDialog.getHigherVersion(content, "0.5.8");
    }

    @Test
    public void testGetHighestVersionMissing() throws Exception {
        String content = "dist-0.5.9.jar dist-0.6.jar";
        assertEquals(null, AboutDialog.getHigherVersion(content, "1.0.3"));
    }
}