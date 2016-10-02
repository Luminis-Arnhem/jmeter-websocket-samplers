package eu.luminis.jmeter.wssampler;

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
    private HeaderManager headerManager;


    public RequestResponseWebSocketSampler() {
        super.setName("Request-Response WebSocket Sampler");
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
        boolean isOK = false; // Did sample succeed?
        String response = null;

        WebSocketClient wsClient = new WebSocketClient();

        result.setSampleLabel(getTitle());
        result.setSamplerData("Connect URL:\nws://" + getServer() + ":" + getPort() + getPath() + "\n\nRequest data:\n" + getRequestData() + "\n");

        Map<String, String> additionalHeaders = Collections.EMPTY_MAP;
        if (headerManager != null) {
            additionalHeaders = convertHeaders(headerManager);
            result.setRequestHeaders(additionalHeaders.entrySet().stream().map(e -> e.getKey() + ": " + e.getValue()).collect(Collectors.joining("\n")));
        }
        // Here we go!
        result.sampleStart(); // Start timing
        try {
            wsClient.connect(new URL("http", getServer(), getPort(), getPath()), additionalHeaders);
            wsClient.sendTextFrame(getRequestData());
            response = wsClient.receiveText();
            result.sampleEnd(); // End timimg

            log.info("Received text: '" + response + "'");
            result.setResponseData(response, null);
            result.setDataType(SampleResult.TEXT);

            result.setResponseCodeOK();
            result.setResponseMessage("OK");
            isOK = true;

        } catch (MalformedURLException e) {
            // Impossible
            throw new RuntimeException(e);
        } catch (IOException ioExc) {
            result.sampleEnd(); // End timimg

            log.error("Error during sampling", ioExc);
            result.setResponseCode("500");
            result.setResponseMessage(ioExc.toString());
        }

        result.setSuccessful(isOK);
        return result;
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

    public String toString() {
        return "WS Req/resp sampler: " + getServer() + ":" + getPort() + getPath() + " - '" + getRequestData() + "'";
    }

}
