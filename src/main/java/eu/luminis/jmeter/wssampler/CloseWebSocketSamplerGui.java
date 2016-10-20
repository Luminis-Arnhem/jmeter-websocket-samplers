package eu.luminis.jmeter.wssampler;

import org.apache.jmeter.samplers.gui.AbstractSamplerGui;
import org.apache.jmeter.testelement.TestElement;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.FlowLayout;

public class CloseWebSocketSamplerGui extends AbstractSamplerGui {

    private JTextField readTimeoutField;

    public CloseWebSocketSamplerGui() {
        init();
    }

    private void init() {
        setLayout(new BorderLayout(0, 5));
        setBorder(makeBorder());
        add(makeTitlePanel(), BorderLayout.NORTH);

        JPanel requestSettingsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        {
            requestSettingsPanel.add(new JLabel("Response (read) timeout (ms): "));
            readTimeoutField = new JTextField();
            readTimeoutField.setColumns(5);
            requestSettingsPanel.add(readTimeoutField);
        }
        add(requestSettingsPanel);
    }

    @Override
    public String getStaticLabel() {
        return "WebSocket Close";
    }

    @Override
    public String getLabelResource() {
        return null;
    }

    @Override
    public TestElement createTestElement() {
        CloseWebSocketSampler element = new CloseWebSocketSampler();
        configureTestElement(element);  // Essential because it sets some basic JMeter properties (e.g. the link between sampler and gui class)
        return element;
    }

    @Override
    public void configure(TestElement element) {
        super.configure(element);
        if (element instanceof CloseWebSocketSampler) {
            CloseWebSocketSampler sampler = (CloseWebSocketSampler) element;
            readTimeoutField.setText("" + sampler.getReadTimeout());
        }
        super.configure(element);
    }

    @Override
    public void clearGui() {
        super.clearGui();
        readTimeoutField.setText("");
    }

    @Override
    public void modifyTestElement(TestElement testElement) {
        configureTestElement(testElement);
        if (testElement instanceof CloseWebSocketSampler) {
            CloseWebSocketSampler sampler = (CloseWebSocketSampler) testElement;
            if (getInt(readTimeoutField.getText(), -1) != -1)
                sampler.setReadTimeout(getInt(readTimeoutField.getText(), 10));
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

