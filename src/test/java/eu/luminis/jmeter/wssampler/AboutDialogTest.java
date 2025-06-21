/*
 * Copyright © 2016, 2017, 2018 Peter Doornbosch
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
package eu.luminis.jmeter.wssampler;

import org.junit.Test;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;

import static org.junit.Assert.*;

public class AboutDialogTest {

    @Test
    public void testGetHighestVersion() throws Exception {
        List<String> content = listOf("v0.5", "0.6");
        assertEquals("0.6", AboutDialog.getHigherVersion(content, "0.5"));
    }

    @Test
    public void testGetHighestVersionDifferentOrder() throws Exception {
        List<String> content = listOf("0.5", "0.3");
        assertEquals("0.5", AboutDialog.getHigherVersion(content, "0.3"));
    }

    @Test
    public void testGetHighestVersionWithOlder() throws Exception {
        List<String> content = listOf("0.5", "0.3");
        assertEquals("0.5", AboutDialog.getHigherVersion(content, "0.1"));
    }

    @Test
    public void testGetHighestVersionWithMicros() throws Exception {
        List<String> content = listOf("0.5.1", "0.5.8");
        assertEquals("0.5.8", AboutDialog.getHigherVersion(content, "0.5.1"));
    }

    @Test
    public void testGetHighestVersionWithSameMicros() throws Exception {
        List<String> content = listOf("0.5.1", "0.5.8");
        assertEquals(null, AboutDialog.getHigherVersion(content, "0.5.8"));
    }

    @Test
    public void testGetHighestVersionWithLargeMicros() throws Exception {
        List<String> content = listOf("0.5.9", "0.5.10");
        assertEquals("0.5.10", AboutDialog.getHigherVersion(content, "0.5.9"));
    }

    @Test
    public void testGetHighestVersionWithMicro1() throws Exception {
        List<String> content = listOf("0.5.9", "0.5");
        assertEquals("0.5.9", AboutDialog.getHigherVersion(content, "0.5.4"));
    }

    @Test
    public void testGetHighestVersionWithMicro2() throws Exception {
        List<String> content = listOf("0.5.9", "0.6");
        assertEquals("0.6", AboutDialog.getHigherVersion(content, "0.5.9"));
    }

    @Test
    public void testGetHighestVersionWithMajorVsMicro() throws Exception {
        List<String> content = listOf("1.0", "0.6");
        assertEquals("1.0", AboutDialog.getHigherVersion(content, "0.6"));
    }

    @Test
    public void testGetHighestVersionWithMajors() throws Exception {
        List<String> content = listOf("1.0", "1.0.3");
        assertEquals("1.0.3", AboutDialog.getHigherVersion(content, "1.0"));
    }

    @Test
    public void testGetHighestVersionMissing() throws Exception {
        List<String> content = listOf("0.5.9", "0.6");
        assertEquals(null, AboutDialog.getHigherVersion(content, "1.0.3"));
    }

    @Test
    public void testGetHighestVersionWhenNoVersions() throws Exception {
        List<String> content = emptyList();
        assertEquals(null, AboutDialog.getHigherVersion(content, "1.0.3"));
    }

    @Test
    public void extractTagsFromGithubTagApi() throws Exception {
        URL contentUrl = this.getClass().getResource("github-tags.json");
        List<String> versions = AboutDialog.getReleasedVersions(contentUrl);
        assertThat(versions).contains("1.2.8", "1.1", "1.2.1", "1.2.6");
    }

    @Test
    public void extractTagsFromIncorrectGithubTagApi() throws Exception {
        URL contentUrl = this.getClass().getResource("no-tags.json");
        List<String> versions = AboutDialog.getReleasedVersions(contentUrl);
        assertThat(versions).isEmpty();
    }

    @Test(expected = AboutDialog.CannotDetermineUpdateException.class)
    public void extractTagsFromGithubTagApiWhenNotAvailable() throws Exception {
        URL contentUrl = this.getClass().getResource("doesnotexist.json");
        List<String> versions = AboutDialog.getReleasedVersions(contentUrl);
    }

    @Test
    public void testGetHighestVersion2() throws Exception {
        URL contentUrl = this.getClass().getResource("github-tags.json");
        List<String> versions = AboutDialog.getReleasedVersions(contentUrl);
        assertThat(AboutDialog.getHighestVersion(versions)).isEqualTo("1.2.10");
    }

    private List<String> listOf(String... items) {
        ArrayList<String> result = new ArrayList<>();
        for (String item : items) {
            result.add(item);
        }
        return result;
    }
}