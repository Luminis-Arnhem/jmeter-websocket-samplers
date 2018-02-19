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

import org.apache.jmeter.config.gui.AbstractConfigGui;
import org.apache.jmeter.gui.util.MenuFactory;
import org.apache.jmeter.testelement.TestElement;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;

import static javax.swing.BoxLayout.Y_AXIS;

// There is not much difference between AbstractConfigGui and AbstractControllerGui....
public class AdvancedOptionsGUI extends AbstractConfigGui {

    private static AtomicBoolean gotUiElement = new AtomicBoolean(false);

    private AdvancedOptionsGuiPanel contentPanel;

    @Override
    public void clearGui() {
        super.clearGui();
        contentPanel.clearGui();
    }

    static void resetElementCount() {
        gotUiElement.set(false);
    }

    public AdvancedOptionsGUI() {
        setLayout(new BorderLayout(0, 5));
        setBorder(makeBorder());
        add(makeTitlePanel(), BorderLayout.NORTH);

        contentPanel = new AdvancedOptionsGuiPanel();
        add(contentPanel, BorderLayout.CENTER);
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
            contentPanel.enabled_multiple_connections_per_thread.setSelected(((AdvancedOptionsElement) element).getMultipleConnectionsEnabled());
        }
    }

    @Override
    public void modifyTestElement(TestElement element) {
        configureTestElement(element);

        if (element instanceof AdvancedOptionsElement) {
            ((AdvancedOptionsElement) element).setMultipleConnectionsEnabled(contentPanel.enabled_multiple_connections_per_thread.isSelected());
            gotUiElement.set(! ((AdvancedOptionsElement) element).isDeleted());
        }
    }
}
