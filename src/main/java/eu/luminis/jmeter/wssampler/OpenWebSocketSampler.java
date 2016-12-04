/*
 * Copyright 2016 Peter Doornbosch
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

import eu.luminis.websocket.HttpUpgradeException;
import eu.luminis.websocket.WebSocketClient;
import org.apache.jmeter.protocol.http.control.Header;
import org.apache.jmeter.protocol.http.control.HeaderManager;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;


public class OpenWebSocketSampler extends WebsocketSampler {

    private static final Logger log = LoggingManager.getLoggerForClass();

    private HeaderManager headerManager;

    @Override
    public SampleResult sample(Entry entry) {
        SampleResult result = new SampleResult();
        result.setSampleLabel(getTitle());
        String validationError = validateArguments();
        if (validationError != null) {
            result.setResponseCode("Sampler error");
            result.setResponseMessage("Sampler error: " + validationError);
            return result;
        }

        boolean isOK = false; // Did sample succeed?
        URL url = null;

        try {
            url = new URL(getTLS()? "https": "http", getServer(), Integer.parseInt(getPort()), getPath());   // java.net.URL does not support "ws" protocol....
        } catch (MalformedURLException e) {
            // Impossible
        }

        result.setSamplerData("Connect URL:\n" + getConnectUrl(url) + "\n");

        Map<String, String> additionalHeaders = Collections.EMPTY_MAP;
        if (headerManager != null) {
            additionalHeaders = convertHeaders(headerManager);
            result.setRequestHeaders(additionalHeaders.entrySet().stream().map(e -> e.getKey() + ": " + e.getValue()).collect(Collectors.joining("\n")));
        }

        dispose(threadLocalCachedConnection.get());
        WebSocketClient wsClient = null;
        boolean connected = false;
        // Here we go!
        result.sampleStart(); // Start timing
        try {
            int readTimeout = Integer.parseInt(getReadTimeout());
            wsClient = new WebSocketClient(url);
            Map<String, String> responseHeaders = wsClient.connect(additionalHeaders, Integer.parseInt(getConnectTimeout()), readTimeout);
            connected = true;

            result.sampleEnd(); // End timimg

            result.setResponseCode("101");
            result.setResponseMessage("Switching Protocols");
            result.setResponseHeaders(responseHeaders.entrySet().stream().map( header -> header.getKey() + ": " + header.getValue()).collect(Collectors.joining("\n")));
            isOK = true;
        }
        catch (MalformedURLException e) {
            // Impossible
            throw new RuntimeException(e);
        }
        catch (HttpUpgradeException upgradeError) {
            result.sampleEnd(); // End timimg
            log.error("Http upgrade error", upgradeError);
            result.setResponseCode(upgradeError.getStatusCode());
            result.setResponseMessage(upgradeError.getMessage());
        }
        catch (IOException ioExc) {
            result.sampleEnd(); // End timimg
            log.error("Error during sampling", ioExc);
            result.setResponseCode("Websocket error");
            result.setResponseMessage("WebSocket error: " + ioExc);
        }
        catch (Exception error) {
            result.sampleEnd(); // End timimg
            log.error("Unhandled error during sampling", error);
            result.setResponseCode("Sampler error");
            result.setResponseMessage("Sampler error: " + error);
        }

        if (connected)
            threadLocalCachedConnection.set(wsClient);
        else
            threadLocalCachedConnection.set(null);

        result.setSuccessful(isOK);
        return result;
    }

    private String validateArguments() {
        String errorMsg = validatePortNumber(getPort());
        if (errorMsg == null)
            errorMsg = validateConnectionTimeout(getConnectTimeout());
        if (errorMsg == null)
            errorMsg = validateReadTimeout(getReadTimeout());

        return errorMsg;
    }


    public void addTestElement(TestElement el) {
        if (el instanceof HeaderManager) {
            headerManager = (HeaderManager) el;
        } else {
            super.addTestElement(el);
        }
    }

    private Map<String,String> convertHeaders(HeaderManager headerManager) {
        Map<String, String> headers = new HashMap<>();
        for (int i = 0; i < headerManager.size(); i++) {
            Header header = headerManager.get(i);
            headers.put(header.getName(), header.getValue());
        }
        return headers;
    }

    private String getTitle() {
        return this.getName();
    }

    public String getServer() {
        return getPropertyAsString("server");
    }

    public void setServer(String server) {
        setProperty("server", server);
    }

    public String getPort() {
        return getPropertyAsString("port", "" + DEFAULT_WS_PORT).trim();
    }

    public void setPort(String port) {
        setProperty("port", port);
    }

    public String getPath() {
        return getPropertyAsString("path");
    }

    public void setPath(String path) {
        setProperty("path", path);
    }

    public String getConnectTimeout() {
        return getPropertyAsString("connectTimeout", "" + WebSocketClient.DEFAULT_CONNECT_TIMEOUT).trim();
    }

    public void setConnectTimeout(String connectTimeout) {
        setProperty("connectTimeout", connectTimeout, "" + WebSocketClient.DEFAULT_CONNECT_TIMEOUT);
    }

    public String getReadTimeout() {
        return getPropertyAsString("readTimeout", "" +WebSocketClient.DEFAULT_READ_TIMEOUT).trim();
    }

    public void setReadTimeout(String readTimeout) {
        setProperty("readTimeout", readTimeout, "" + WebSocketClient.DEFAULT_READ_TIMEOUT);
    }

    @Override
    protected Logger getLogger() {
        return log;
    }
}
