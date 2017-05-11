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
package eu.luminis.jmeter.wssampler;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class WebsocketSamplerTest {

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

    /**
     * A helper to switch a system property value and restore the previous one.
     * When used in try-with-resources, restores the values automatically.
     *
     * https://www.dynatrace.com/blog/how-stable-are-your-unit-tests-best-practices-to-raise-test-automation-quality/
     */
    static class ScopedProperty implements AutoCloseable {

        private final String key;
        private final String oldValue;

        /**
         *
         * @param key The System.setProperty key
         * @param value The System.setProperty value to switch to.
         */
        public ScopedProperty(final String key, final String value) {
            this.key = key;
            oldValue = System.setProperty(key, value);
        }

        @Override
        public void close() {
            // Can't use setProperty(key, null) -> Throws NullPointerException.
            if( oldValue == null ) {
                // Previously there was no entry.
                System.clearProperty(key);
            } else {
                System.setProperty(key, oldValue);
            }
        }
    }
}
