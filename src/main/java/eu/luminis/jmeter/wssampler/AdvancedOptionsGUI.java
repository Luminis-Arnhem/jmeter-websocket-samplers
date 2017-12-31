package eu.luminis.jmeter.wssampler;

import org.apache.jmeter.config.gui.AbstractConfigGui;
import org.apache.jmeter.gui.util.MenuFactory;
import org.apache.jmeter.testelement.TestElement;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;

// There is not much difference between AbstractConfigGui and AbstractControllerGui....
public class AdvancedOptionsGUI extends AbstractConfigGui {

    private final JCheckBox enabled_multiple_connections_per_thread;
    private static AtomicBoolean gotUiElement = new AtomicBoolean(false);

    static void resetElementCount() {
        gotUiElement.set(false);
    }

    public AdvancedOptionsGUI() {
        setLayout(new BorderLayout(0, 5));
        setBorder(makeBorder());
        add(makeTitlePanel(), BorderLayout.NORTH);
        enabled_multiple_connections_per_thread = new JCheckBox("enabled multiple connections per thread");
        add(enabled_multiple_connections_per_thread);
    }

    @Override
    public Collection<String> getMenuCategories() {
        return Arrays.asList(MenuFactory.NON_TEST_ELEMENTS);  // This menu will ensure the item can only be created at top level (but unfortunately, multiple times)
    }

    @Override
    public boolean canBeAdded() {
        return !gotUiElement.get();
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
        gotUiElement.set(true);
        AdvancedOptionsElement element = new AdvancedOptionsElement();
        modifyTestElement(element);
        return element;
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
    }

    @Override
    public void configure(TestElement element) {
        super.configure(element);
        if (element instanceof AdvancedOptionsElement) {
            gotUiElement.set(true);
            WebsocketSampler.multipleConnectionsEnabled = ((AdvancedOptionsElement) element).getMultipleConnectionsEnabled();
            enabled_multiple_connections_per_thread.setSelected(((AdvancedOptionsElement) element).getMultipleConnectionsEnabled());
        }
    }

    @Override
    public void modifyTestElement(TestElement element) {
        configureTestElement(element);

        if (element instanceof AdvancedOptionsElement) {
            ((AdvancedOptionsElement) element).setMultipleConnectionsEnabled(enabled_multiple_connections_per_thread.isSelected());
            gotUiElement.set(! ((AdvancedOptionsElement) element).isDeleted());
        }
    }
}
