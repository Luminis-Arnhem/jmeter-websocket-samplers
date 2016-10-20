package eu.luminis.jmeter.wssampler;

import org.apache.jmeter.samplers.gui.AbstractSamplerGui;
import org.apache.jmeter.testelement.TestElement;

import java.awt.BorderLayout;

import static eu.luminis.jmeter.wssampler.RequestResponseWebSocketSamplerGuiPanel.BINARY;
import static eu.luminis.jmeter.wssampler.RequestResponseWebSocketSamplerGuiPanel.TEXT;

public class RequestResponseWebSocketSamplerGui extends AbstractSamplerGui {

    private RequestResponseWebSocketSamplerGuiPanel settingsPanel;

    public RequestResponseWebSocketSamplerGui() {
        init();
    }

    private void init() {
        setLayout(new BorderLayout(0, 5));
        setBorder(makeBorder());
        add(makeTitlePanel(), BorderLayout.NORTH);
        settingsPanel = new RequestResponseWebSocketSamplerGuiPanel();
        add(settingsPanel, BorderLayout.CENTER);
    }

    @Override
    public void clearGui() {
        super.clearGui();
        settingsPanel.clearGui();
    }

    @Override
    public String getStaticLabel() {
        return "WebSocket request-response Sampler";
    }

    @Override
    public String getLabelResource() {
        return null;
    }

    @Override
    public TestElement createTestElement() {
        RequestResponseWebSocketSampler element = new RequestResponseWebSocketSampler();
        configureTestElement(element);  // Essential because it sets some basic JMeter properties (e.g. the link between sampler and gui class)
        return element;
    }

    @Override
    public void configure(TestElement element) {
        super.configure(element);
        if (element instanceof RequestResponseWebSocketSampler) {
            RequestResponseWebSocketSampler sampler = (RequestResponseWebSocketSampler) element;
            settingsPanel.setCreateNewConnection(sampler.getCreateNewConnection());
            settingsPanel.serverField.setText(sampler.getServer());
            settingsPanel.portField.setText("" + sampler.getPort());
            settingsPanel.pathField.setText("" + sampler.getPath());
            settingsPanel.connectionTimeoutField.setText("" + sampler.getConnectTimeout());
            settingsPanel.typeSelector.setSelectedItem(sampler.getBinary()? BINARY: TEXT);
            settingsPanel.requestDataField.setText(sampler.getRequestData());
            settingsPanel.readTimeoutField.setText("" + sampler.getReadTimeout());
        }
    }

    @Override
    public void modifyTestElement(TestElement element) {
        configureTestElement(element);
        if (element instanceof RequestResponseWebSocketSampler) {
            RequestResponseWebSocketSampler sampler = (RequestResponseWebSocketSampler) element;
            sampler.setServer(settingsPanel.serverField.getText());
            sampler.setPort(getInt(settingsPanel.portField.getText(), 80));
            sampler.setPath(settingsPanel.pathField.getText());
            if (getInt(settingsPanel.connectionTimeoutField.getText(), -1) != -1)
                sampler.setConnectTimeout(getInt(settingsPanel.connectionTimeoutField.getText(), 10));
            sampler.setBinary(settingsPanel.typeSelector.getSelectedItem() == BINARY);
            sampler.setRequestData(settingsPanel.requestDataField.getText());
            sampler.setCreateNewConnection(settingsPanel.newConnection.isSelected());
            if (getInt(settingsPanel.readTimeoutField.getText(), -1) != -1)
                sampler.setReadTimeout(getInt(settingsPanel.readTimeoutField.getText(), 10));
        }
    }

    private int getInt(String value, int def) {
        try {
            return Integer.parseInt(value);
        }
        catch (NumberFormatException e) {
            return def;
        }
    }

}
