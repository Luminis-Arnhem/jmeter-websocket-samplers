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

import eu.luminis.websocket.*;
import org.apache.jmeter.samplers.SampleResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;


public class RequestResponseWebSocketSampler extends WebsocketGeneralSampler {

    private static final Logger log = LoggerFactory.getLogger(RequestResponseWebSocketSampler.class);


    public RequestResponseWebSocketSampler() {
        super.setName("Request-Response WebSocket Sampler");
        // Set defaults that have non-default values by default
        setCreateNewConnection(true);
    }

    @Override
    protected WebSocketClient prepareWebSocketClient(SampleResult result) {
        String connectionId = WebsocketSampler.multipleConnectionsEnabled? getConnectionId().trim(): "";
        if (getCreateNewConnection()) {
            dispose(threadLocalCachedConnection.get().get(connectionId));
            try {
                URL url = new URL(getTLS()? "https": "http", getServer(), Integer.parseInt(getPort()), getPath());   // java.net.URL does not support "ws" protocol....
                return new WebSocketClient(url);
            } catch (MalformedURLException e) {
                // Impossible
                throw new RuntimeException();
            }
        }
        else {
            WebSocketClient wsClient = threadLocalCachedConnection.get().get(connectionId);
            if (wsClient != null) {
                return wsClient;
            }
            else {
                log.error("Sampler '"+ getName() + "': there is no connection to re-use");
                result.setResponseCode("Sampler error");
                result.setResponseMessage("Sampler configured for using existing connection, but there is no connection");
                return null;
            }
        }
    }

    @Override
    protected Frame doSample(WebSocketClient wsClient, SampleResult result) throws IOException, UnexpectedFrameException, SamplingAbortedException {
        sendFrame(wsClient, result, getType(), getRequestData(), getLoadDataFromFile()? new File(getDataFile()): null);
        return readFrame(wsClient, result, getType());
    }

    @Override
    protected void postProcessResponse(Frame response, SampleResult result) {
        processDefaultReadResponse((DataFrame) response, getType(), result);
    }

    @Override
    protected Logger getLogger() {
        return log;
    }

    protected String validateArguments() {
        String errorMsg = null;
        if (getCreateNewConnection()) {
            errorMsg = validatePortNumber(getPort());
            if (errorMsg == null)
                errorMsg = validateConnectionTimeout(getConnectTimeout());
        }
        if (errorMsg == null)
            errorMsg = validateReadTimeout(getReadTimeout());

        return errorMsg;
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

    public String getRequestData() {
        return getPropertyAsString("requestData");
    }

    public void setRequestData(String requestData) {
        setProperty("requestData", requestData);
    }

    public String toString() {
        return "WS Req/resp sampler '" + getName() + "'";
    }

    public boolean getCreateNewConnection() {
        return getPropertyAsBoolean("createNewConnection");
    }

    public void setCreateNewConnection(boolean value) {
        setProperty("createNewConnection", value);
    }

    public boolean getLoadDataFromFile() {
        return getPropertyAsBoolean("loadDataFromFile");
    }

    public void setLoadDataFromFile(boolean value) {
        setProperty("loadDataFromFile", value);
    }

    public String getDataFile() {
        return getPropertyAsString("dataFile");
    }

    public void setDataFile(String dataFile) {
        setProperty("dataFile", dataFile);
    }
}
