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

import org.apache.jmeter.samplers.gui.AbstractSamplerGui;
import org.apache.jmeter.testelement.TestElement;

import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.FlowLayout;

import static eu.luminis.jmeter.wssampler.WebsocketSampler.MAX_READ_TIMEOUT;
import static eu.luminis.jmeter.wssampler.WebsocketSampler.MIN_READ_TIMEOUT;
import static javax.swing.BoxLayout.*;

public class CloseWebSocketSamplerGui extends AbstractSamplerGui {

    private JTextField closeStatusField;
    private JTextField readTimeoutField;

    public CloseWebSocketSamplerGui() {
        setLayout(new BorderLayout(0, 5));
        setBorder(makeBorder());
        add(makeTitlePanel(), BorderLayout.NORTH);

        WebSocketSamplerGuiPanel layoutPanel = new WebSocketSamplerGuiPanel(){};
        {
            layoutPanel.setLayout(new BorderLayout());

            JPanel settingsPanel = new JPanel();
            {
                settingsPanel.setLayout(new BoxLayout(settingsPanel, BoxLayout.Y_AXIS));
                settingsPanel.setBorder(BorderFactory.createTitledBorder("Data (close frame)"));
                JPanel closeStatusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
                {
                    closeStatusPanel.add(new JLabel("Close status: "));
                    closeStatusField = new JTextField();
                    closeStatusField.setColumns(10);
                    closeStatusPanel.add(closeStatusField);
                    JErrorMessageLabel closeStatusErrorField = new JErrorMessageLabel();
                    closeStatusPanel.add(closeStatusErrorField);
                    // According to WebSocket specifcation (RFC 6455), status codes in the range 1000-4999 can be used.
                    layoutPanel.addIntegerRangeCheck(closeStatusField, 1000, 4999, closeStatusErrorField);
                }
                settingsPanel.add(closeStatusPanel);

                JPanel readTimeoutPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
                {
                    readTimeoutPanel.add(new JLabel("Response (read) timeout (ms): "));
                    readTimeoutField = new JTextField();
                    readTimeoutField.setColumns(10);
                    readTimeoutPanel.add(readTimeoutField);
                    JErrorMessageLabel readTimeoutErrorField = new JErrorMessageLabel();
                    readTimeoutPanel.add(readTimeoutErrorField);
                    layoutPanel.addIntegerRangeCheck(readTimeoutField, MIN_READ_TIMEOUT, MAX_READ_TIMEOUT, readTimeoutErrorField);
                }
                settingsPanel.add(readTimeoutPanel);
            }
            layoutPanel.add(settingsPanel, BorderLayout.NORTH);
            layoutPanel.add(WebSocketSamplerGuiPanel.createAboutPanel(this));
        }
        add(layoutPanel, BorderLayout.CENTER);
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
    public void clearGui() {
        super.clearGui();
        closeStatusField.setText("");
        readTimeoutField.setText("");
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
            closeStatusField.setText(sampler.getStatusCode());
            readTimeoutField.setText(sampler.getReadTimeout());
        }
        super.configure(element);
    }

    @Override
    public void modifyTestElement(TestElement testElement) {
        configureTestElement(testElement);
        if (testElement instanceof CloseWebSocketSampler) {
            CloseWebSocketSampler sampler = (CloseWebSocketSampler) testElement;
            sampler.setStatusCode(closeStatusField.getText());
            sampler.setReadTimeout(readTimeoutField.getText());
        }
    }
}

