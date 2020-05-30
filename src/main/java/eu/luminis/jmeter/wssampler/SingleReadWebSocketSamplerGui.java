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

import java.awt.BorderLayout;

import static eu.luminis.jmeter.wssampler.RequestResponseWebSocketSamplerGuiPanel.BINARY;
import static eu.luminis.jmeter.wssampler.RequestResponseWebSocketSamplerGuiPanel.TEXT;

public class SingleReadWebSocketSamplerGui extends AbstractSamplerGui {

    private SingleReadWebSocketSamplerGuiPanel settingsPanel;

    public SingleReadWebSocketSamplerGui() {
        init();
    }

    private void init() {
        setLayout(new BorderLayout(0, 5));
        setBorder(makeBorder());
        add(makeTitlePanel(), BorderLayout.NORTH);
        settingsPanel = new SingleReadWebSocketSamplerGuiPanel();
        add(settingsPanel, BorderLayout.CENTER);
    }

    @Override
    public void clearGui() {
        super.clearGui();
        settingsPanel.clearGui();
    }

    @Override
    public String getStaticLabel() {
        return "WebSocket Single Read Sampler";
    }

    @Override
    public String getLabelResource() {
        return null;
    }

    @Override
    public TestElement createTestElement() {
        SingleReadWebSocketSampler element = new SingleReadWebSocketSampler();
        configureTestElement(element);  // Essential because it sets some basic JMeter properties (e.g. the link between sampler and gui class)
        return element;
    }

    @Override
    public void configure(TestElement element) {
        super.configure(element);
        if (element instanceof SingleReadWebSocketSampler) {
            SingleReadWebSocketSampler sampler = (SingleReadWebSocketSampler) element;
            settingsPanel.setCreateNewConnection(sampler.getCreateNewConnection());
            settingsPanel.setTLS(sampler.getTLS());
            settingsPanel.serverField.setText(sampler.getServer());
            settingsPanel.portField.setText(sampler.getPort());
            settingsPanel.pathField.setText(sampler.getPath());
            settingsPanel.connectionTimeoutField.setText(sampler.getConnectTimeout());
            settingsPanel.setType(sampler.getDataType());
            settingsPanel.readTimeoutField.setText(sampler.getReadTimeout());
            settingsPanel.optionalSampler.setSelected(sampler.getOptional());
        }
    }

    @Override
    public void modifyTestElement(TestElement element) {
        configureTestElement(element);
        if (element instanceof SingleReadWebSocketSampler) {
            SingleReadWebSocketSampler sampler = (SingleReadWebSocketSampler) element;
            sampler.setTLS(settingsPanel.getTLS());
            sampler.setServer(settingsPanel.serverField.getText());
            sampler.setPort(settingsPanel.portField.getText());
            sampler.setPath(settingsPanel.pathField.getText());
            sampler.setConnectTimeout(settingsPanel.connectionTimeoutField.getText());
            sampler.setDataType(settingsPanel.getType());
            sampler.setCreateNewConnection(settingsPanel.newConnection.isSelected());
            sampler.setReadTimeout(settingsPanel.readTimeoutField.getText());
            sampler.setOptional(settingsPanel.optionalSampler.isSelected());
        }
    }

}
