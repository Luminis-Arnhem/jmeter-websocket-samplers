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

import eu.luminis.websocket.MockWebSocketClientCreator;
import eu.luminis.websocket.WebSocketClient;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class WebsocketSamplerTest {

    MockWebSocketClientCreator mocker = new MockWebSocketClientCreator();

    @BeforeClass
    public static void initJMeterContext() {
        JMeterContextService.getContext().setVariables(new JMeterVariables());
    }

    @Test
    public void defaultDoesNotUseProxy() {
        SingleReadWebSocketSampler sampler = new SingleReadWebSocketSampler();
        sampler.initProxyConfiguration();
        assertFalse(sampler.useProxy("anywhere.com"));
    }

    @Test
    public void simpleProxyConfigurationShouldLeadToUsingProxy() {
        SingleReadWebSocketSampler sampler = new SingleReadWebSocketSampler();
        try (ScopedProperty proxyHostProp = new ScopedProperty("http.proxyHost", "proxy.local");
                ScopedProperty proxyPortProp = new ScopedProperty("http.proxyPort", "8392"))
        {
            sampler.initProxyConfiguration();
            assertTrue(sampler.useProxy("echo.websocket.org"));
        }
    }

    @Test
    public void proxyExcludedHostShouldNotUseProxy() {
        SingleReadWebSocketSampler sampler = new SingleReadWebSocketSampler();
        try (ScopedProperty proxyHostProp = new ScopedProperty("http.proxyHost", "proxy.local");
             ScopedProperty proxyPortProp = new ScopedProperty("http.proxyPort", "8392");
             ScopedProperty proxyExcludes = new ScopedProperty("http.nonProxyHosts", "www.google.com"))
        {
            sampler.initProxyConfiguration();
            assertTrue(sampler.useProxy("echo.websocket.org"));
            assertFalse(sampler.useProxy("www.google.com"));
        }
    }

    @Test
    public void proxyExcludedHostWithWildcardShouldNotUseProxy() {
        SingleReadWebSocketSampler sampler = new SingleReadWebSocketSampler();
        try (ScopedProperty proxyHostProp = new ScopedProperty("http.proxyHost", "proxy.local");
             ScopedProperty proxyPortProp = new ScopedProperty("http.proxyPort", "8392");
             ScopedProperty proxyExcludes = new ScopedProperty("http.nonProxyHosts", "*.google.com"))
        {
            sampler.initProxyConfiguration();
            assertTrue(sampler.useProxy("echo.websocket.org"));
            assertFalse(sampler.useProxy("www.google.com"));
            assertFalse(sampler.useProxy("gmail.google.com"));
            assertTrue(sampler.useProxy("google.com"));
        }
    }

    @Test
    public void readingFrameFromNewWebsocketConnectionShouldReturnCorrectHeaderAndBodySize() throws IOException {
        ByteArrayOutputStream outputBuffer = new ByteArrayOutputStream(1000);
        String httpResponse = "HTTP/1.1 101 Switching Protocols\r\nUpgrade: websocket\r\nConnection: Upgrade\r\n\r\n";
        byte[] serverResponse = new byte[httpResponse.getBytes().length + 4];
        System.arraycopy(httpResponse.getBytes(), 0, serverResponse, 0, httpResponse.getBytes().length);
        System.arraycopy(new byte[]{ (byte) 0x81, 0x02, 0x68, 0x69 }, 0, serverResponse, httpResponse.getBytes().length, 4);

        SingleReadWebSocketSampler sampler = new SingleReadWebSocketSampler() {
            @Override
            protected WebSocketClient prepareWebSocketClient(SampleResult result) {
                try {
                    return mocker.createMockWebSocketClientWithResponse(outputBuffer, serverResponse);
                } catch (MalformedURLException e) {
                    throw new RuntimeException();
                }
            }
        };

        SampleResult result = sampler.sample(null);
        assertTrue(result.isSuccessful());
        assertEquals(httpResponse.length() + 2, result.getHeadersSize());
        assertEquals(2, result.getBodySize());
    }

}
