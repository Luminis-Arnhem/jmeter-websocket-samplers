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

import javax.swing.*;
import java.awt.*;

import static eu.luminis.jmeter.wssampler.SingleReadWebSocketSampler.DataType.*;
import static javax.swing.BoxLayout.Y_AXIS;

public class SingleReadWebSocketSamplerGuiPanel extends WebSocketSamplerGuiPanel {

    public static final String BINARY = "Binary";
    public static final String TEXT = "Text";
    public static final String ANY = "Text or Binary";

    JComboBox typeSelector;
    JCheckBox optionalSampler;

    public SingleReadWebSocketSamplerGuiPanel() {
        init();
        setCreateNewConnection(false);
    }

    private void init() {

        JPanel boxPanel = new JPanel();
        boxPanel.setLayout(new BoxLayout(boxPanel, Y_AXIS));

        JPanel connectionPanel = createConnectionPanel();
        boxPanel.add(connectionPanel);

        JPanel dataPanel = new JPanel();
        {
            dataPanel.setBorder(BorderFactory.createTitledBorder("Response"));
            dataPanel.setLayout(new BoxLayout(dataPanel, BoxLayout.Y_AXIS));

            JPanel topBar = new JPanel();
            {
                topBar.setLayout(new BoxLayout(topBar, BoxLayout.X_AXIS));
                String[] typeOptions = {TEXT, BINARY, ANY};
                typeSelector = new JComboBox(typeOptions);
                typeSelector.setMaximumSize(typeSelector.getMinimumSize());
                topBar.add(typeSelector);
                topBar.add(Box.createHorizontalStrut(10));
                topBar.add(Box.createHorizontalGlue());
            }
            dataPanel.add(topBar);

            JPanel requestSettingsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            {
                requestSettingsPanel.add(new JLabel("Response (read) timeout (ms): "));
                readTimeoutField = new JTextField();
                readTimeoutField.setColumns(10);
                JLabel readTimeoutErrorField = new JLabel();
                readTimeoutErrorField.setForeground(GuiUtils.getLookAndFeelColor("TextField.errorForeground"));
                addIntegerRangeCheck(readTimeoutField, MIN_READ_TIMEOUT, MAX_READ_TIMEOUT, readTimeoutErrorField);
                requestSettingsPanel.add(readTimeoutField);
                requestSettingsPanel.add(readTimeoutErrorField);
            }
            dataPanel.add(requestSettingsPanel);

            JPanel optionalSettingsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            {
                optionalSampler = new JCheckBox("Optional read (read timeout will not lead to sampler error)");
                optionalSettingsPanel.add(optionalSampler);
            }

            dataPanel.add(optionalSettingsPanel);
        }
        boxPanel.add(dataPanel);
        boxPanel.add(createAboutPanel(this));

        setLayout(new BorderLayout());
        add(boxPanel, BorderLayout.NORTH);
    }

    void clearGui() {
        serverField.setText("");
        portField.setText("");
        pathField.setText("");
        setCreateNewConnection(false);
    }

    public SingleReadWebSocketSampler.DataType getType() {
        if (typeSelector.getSelectedItem().equals(TEXT)) {
            return Text;
        }
        if (typeSelector.getSelectedItem().equals(BINARY)) {
            return Binary;
        }
        if (typeSelector.getSelectedItem().equals(ANY)) {
            return Any;
        }
        throw new IllegalStateException();
    }

    public void setType(SingleReadWebSocketSampler.DataType dataType) {
        switch (dataType) {
            case Text:
                typeSelector.setSelectedItem(TEXT);
                break;
            case Binary:
                typeSelector.setSelectedItem(BINARY);
                break;
            case Any:
                typeSelector.setSelectedItem(ANY);
                break;
        }
    }


    public static void main(String[] args) {
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 400);
        frame.getContentPane().add(new SingleReadWebSocketSamplerGuiPanel());
        frame.setVisible(true);
    }
}
