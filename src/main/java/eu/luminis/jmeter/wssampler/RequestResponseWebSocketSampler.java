package eu.luminis.jmeter.wssampler;

import eu.luminis.websocket.HttpUpgradeException;
import eu.luminis.websocket.UnexpectedFrameException;
import eu.luminis.websocket.WebSocketClient;
import org.apache.jmeter.protocol.http.control.Header;
import org.apache.jmeter.protocol.http.control.HeaderManager;
import org.apache.jmeter.samplers.AbstractSampler;
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


public class RequestResponseWebSocketSampler extends AbstractSampler {

    private static final Logger log = LoggingManager.getLoggerForClass();

    private static final ThreadLocal<WebSocketClient> threadLocalCachedConnection = SharedContext.threadLocalCachedConnection;

    private HeaderManager headerManager;


    public RequestResponseWebSocketSampler() {
        super.setName("Request-Response WebSocket Sampler");
        // Set defaults that have non-default values by default
        setCreateNewConnection(true);
    }

    @Override
    public String getName() {
        return getPropertyAsString(TestElement.NAME);
    }

    @Override
    public void setName(String name) {
        if (name != null) {
            setProperty(TestElement.NAME, name);
        }
    }

    @Override
    public SampleResult sample(Entry entry) {
        SampleResult result = new SampleResult();
        result.setSampleLabel(getTitle());
        boolean isOK = false; // Did sample succeed?
        Object response = null;
        URL url = null;

        WebSocketClient wsClient;
        if (getCreateNewConnection()) {
            try {
                url = new URL("http", getServer(), getPort(), getPath());   // java.net.URL does not support "ws" protocol....
            } catch (MalformedURLException e) {
                // Impossible
            }
            wsClient = null;
            dispose(threadLocalCachedConnection.get());
        }
        else {
            wsClient = threadLocalCachedConnection.get();
            if (wsClient != null) {
                url = wsClient.getConnectUrl();
            }
            else {
                log.error("There is no connection to re-use");
                result.setResponseCode("Sampler error");
                result.setResponseMessage("Sampler configured for using existing connection, but there is no connection");
                return result;
            }
        }

        String path = url.getFile();
        if (! path.startsWith("/"))
            path = "/" + path;
        result.setSamplerData("Connect URL:\nws://" + url.getHost() + ":" + url.getPort() + path
                + (wsClient != null? "\n(using existing connection)": "")
                + "\n\nRequest data:\n" + getRequestData() + "\n");

        Map<String, String> additionalHeaders = Collections.EMPTY_MAP;
        if (headerManager != null) {
            additionalHeaders = convertHeaders(headerManager);
            result.setRequestHeaders(additionalHeaders.entrySet().stream().map(e -> e.getKey() + ": " + e.getValue()).collect(Collectors.joining("\n")));
        }

        boolean connected = false;
        // Here we go!
        result.sampleStart(); // Start timing
        try {
            if (wsClient == null) {
                wsClient = new WebSocketClient(url);
                wsClient.connect(additionalHeaders);
                connected = true;
            }
            if (getBinary())
                wsClient.sendBinaryFrame(BinaryUtils.parseBinaryString(getRequestData()));
            else
                wsClient.sendTextFrame(getRequestData());
            response = getBinary()? wsClient.receiveBinaryData(): wsClient.receiveText();
            result.sampleEnd(); // End timimg

            if (getBinary()) {
                result.setResponseData((byte[]) response);
                log.debug("Received binary data: " + formatBinary((byte[]) response));
            }
            else {
                result.setResponseData((String) response, null);
                log.debug("Received text: '" + response + "'");
            }
            result.setDataType(getBinary()? SampleResult.BINARY: SampleResult.TEXT);

            result.setResponseCodeOK();
            result.setResponseMessage("OK");
            isOK = true;

        }
        catch (UnexpectedFrameException e) {
            result.sampleEnd(); // End timimg
            log.error("Unexpected frame type received: " + e.getReceivedFrame());
            result.setResponseCode("Sampler error: unexpected frame type.");
            result.setResponseMessage("Received: " + e.getReceivedFrame());
        }
        catch (MalformedURLException e) {
            // Impossible
            throw new RuntimeException(e);
        }
        catch (NumberFormatException noNumber) {
            // Thrown by BinaryUtils.parseBinaryString
            result.sampleEnd(); // End timimg
            log.error("Request data is not binary: " + getRequestData());
            result.setResponseCode("Sampler Error");
            result.setResponseMessage("Request data is not binary: " + getRequestData());
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

        if (connected)
            threadLocalCachedConnection.set(wsClient);
        else if (getCreateNewConnection())
            threadLocalCachedConnection.set(null);

        result.setSuccessful(isOK);
        return result;
    }

    private void dispose(WebSocketClient webSocketClient) {
        if (webSocketClient != null) {
            log.debug("Closing streams for existing websocket connection");
            webSocketClient.dispose();
        }
    }

    private String formatBinary(byte[] data) {
        StringBuilder builder = new StringBuilder();
        for (byte b: data)
            builder.append(String.format("%#x ", b));
        return builder.toString();
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

    public int getPort() {
        return getPropertyAsInt("port");
    }

    public String getPath() {
        return getPropertyAsString("path");
    }

    public void setPath(String path) {
        setProperty("path", path);
    }

    public void setPort(int port) {
        setProperty("port", port);
    }

    public String getRequestData() {
        return getPropertyAsString("requestData");
    }

    public void setRequestData(String requestData) {
        setProperty("requestData", requestData);
    }

    public boolean getBinary() {
        return getPropertyAsBoolean("binaryPayload");
    }

    public void setBinary(boolean binary) {
        setProperty("binaryPayload", binary);
    }

    public String toString() {
        return "WS Req/resp sampler: " + getServer() + ":" + getPort() + getPath() + " - '" + getRequestData() + "'";
    }

    public boolean getCreateNewConnection() {
        return getPropertyAsBoolean("createNewConnection");
    }

    public void setCreateNewConnection(boolean value) {
        setProperty("createNewConnection", value);
    }
}
