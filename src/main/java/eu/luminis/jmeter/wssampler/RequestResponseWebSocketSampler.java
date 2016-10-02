package eu.luminis.jmeter.wssampler;

import org.apache.jmeter.samplers.AbstractSampler;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class RequestResponseWebSocketSampler extends AbstractSampler {

    private static final Logger log = LoggingManager.getLoggerForClass();


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

        // Here we go!
        result.sampleStart(); // Start timing
        try {
            wsClient.connect(new URL("http", getServer(), getPort(), getPath()));
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
