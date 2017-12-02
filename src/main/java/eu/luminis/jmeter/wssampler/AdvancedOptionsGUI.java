package eu.luminis.jmeter.wssampler;

import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.config.gui.AbstractConfigGui;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.BooleanProperty;

import javax.swing.*;
import java.awt.*;

public class AdvancedOptionsGUI extends AbstractConfigGui {

    private final JCheckBox enabled_multiple_connections_per_thread;

    public AdvancedOptionsGUI() {
        setLayout(new BorderLayout(0, 5));
        setBorder(makeBorder());
        add(makeTitlePanel(), BorderLayout.NORTH);
        enabled_multiple_connections_per_thread = new JCheckBox("enabled multiple connections per thread");
        add(enabled_multiple_connections_per_thread);
    }

    @Override
    public String getStaticLabel() {
        return "WebSocket Advanced Options";
    }

    @Override
    public String getLabelResource() {
        return null;
    }

    @Override
    public TestElement createTestElement() {
        ConfigTestElement element = new ConfigTestElement();
        modifyTestElement(element);
        return element;
    }

    @Override
    public void configure(TestElement element) {
        super.configure(element);
        if (element instanceof ConfigTestElement) {
            enabled_multiple_connections_per_thread.setSelected(element.getPropertyAsBoolean("enableMultipleConnectionsPerThread", false));
            WebsocketSampler.multipleConnectionsEnabled = element.getPropertyAsBoolean("enableMultipleConnectionsPerThread", false);
        }
    }

    @Override
    public void modifyTestElement(TestElement element) {
        configureTestElement(element);
        if (element instanceof ConfigTestElement) {
            element.setProperty(new BooleanProperty("enableMultipleConnectionsPerThread", enabled_multiple_connections_per_thread.isSelected()));
            WebsocketSampler.multipleConnectionsEnabled = enabled_multiple_connections_per_thread.isSelected();
        }
    }
}
