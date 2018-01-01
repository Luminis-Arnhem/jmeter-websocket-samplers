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

import eu.luminis.websocket.Frame;
import eu.luminis.websocket.UnexpectedFrameException;
import eu.luminis.websocket.WebSocketClient;
import org.apache.jmeter.protocol.http.control.Header;
import org.apache.jmeter.protocol.http.control.HeaderManager;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;


public class OpenWebSocketSampler extends WebsocketSampler {

    private static final Logger log = LoggingManager.getLoggerForClass();

    @Override
    protected WebSocketClient prepareWebSocketClient(SampleResult result) {
        dispose(threadLocalCachedConnection.get());
        try {
            URL url = new URL(getTLS()? "https": "http", getServer(), Integer.parseInt(getPort()), getPath());   // java.net.URL does not support "ws" protocol....
            return new WebSocketClient(url);
        } catch (MalformedURLException e) {
            // Impossible
            throw new RuntimeException();
        }
    }

    @Override
    protected Frame doSample(WebSocketClient wsClient, SampleResult result) throws IOException, UnexpectedFrameException {
        // Intentionally left empty: this sampler does nothing but open the connection.
        return null;
    }

    @Override
    protected String validateArguments() {
        String errorMsg = validatePortNumber(getPort());
        if (errorMsg == null)
            errorMsg = validateConnectionTimeout(getConnectTimeout());
        if (errorMsg == null)
            errorMsg = validateReadTimeout(getReadTimeout());

        return errorMsg;
    }

    @Override
    public void addTestElement(TestElement element) {
        if (element instanceof HeaderManager) {
            headerManager = (HeaderManager) element;
        } else {
            super.addTestElement(element);
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

    @Override
    protected Logger getLogger() {
        return log;
    }
}
