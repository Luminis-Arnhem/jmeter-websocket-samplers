package eu.luminis.jmeter.wssampler;

import org.apache.jmeter.samplers.gui.AbstractSamplerGui;
import org.apache.jmeter.testelement.TestElement;

import java.awt.BorderLayout;

public class CloseWebSocketSamplerGui extends AbstractSamplerGui {

    public CloseWebSocketSamplerGui() {
        init();
    }

    private void init() {
        setLayout(new BorderLayout(0, 5));
        setBorder(makeBorder());
        add(makeTitlePanel(), BorderLayout.NORTH);
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
    public void modifyTestElement(TestElement testElement) {
        configureTestElement(testElement);
        if (testElement instanceof CloseWebSocketSampler) {
            CloseWebSocketSampler sampler = (CloseWebSocketSampler) testElement;
        }
    }
}
