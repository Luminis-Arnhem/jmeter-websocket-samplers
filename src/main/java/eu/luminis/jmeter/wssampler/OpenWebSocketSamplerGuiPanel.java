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

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;

import static javax.swing.BoxLayout.Y_AXIS;

public class OpenWebSocketSamplerGuiPanel extends WebSocketSamplerGuiPanel {

    private JLabel connectionTimeoutErrorLabel;
    private JLabel readTimeoutErrorLabel;

    public OpenWebSocketSamplerGuiPanel() {
        init();
    }

    private void init() {

        JPanel boxPanel = new JPanel();
        {
            boxPanel.setLayout(new BoxLayout(boxPanel, Y_AXIS));
            boxPanel.setBorder(BorderFactory.createTitledBorder("Connection"));
            boxPanel.add(Box.createVerticalStrut(3));

            JPanel urlPanel = createUrlPanel();
            boxPanel.add(urlPanel);

            JPanel connectionTimeoutPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            connectionTimeoutPanel.setBorder(BorderFactory.createEmptyBorder(1, 4, 1, 0));
            {
                JLabel connectionTimeoutLabel = new JLabel("Connection timeout (ms):");
                connectionTimeoutPanel.add(connectionTimeoutLabel);
                connectionTimeoutField = new JTextField();
                connectionTimeoutField.setColumns(10);
                connectionTimeoutPanel.add(connectionTimeoutField);
                connectionTimeoutErrorLabel = new JLabel();
                connectionTimeoutErrorLabel.setForeground(GuiUtils.getLookAndFeelColor("TextField.errorForeground"));
                addIntegerRangeCheck(connectionTimeoutField, MIN_CONNECTION_TIMEOUT, MAX_CONNECTION_TIMEOUT, connectionTimeoutErrorLabel);
                connectionTimeoutPanel.add(connectionTimeoutErrorLabel);
            }
            boxPanel.add(connectionTimeoutPanel);

            JPanel readTimeoutPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            readTimeoutPanel.setBorder(BorderFactory.createEmptyBorder(1, 4, 1, 0));
            {
                JLabel readTimeoutLabel = new JLabel("Read timeout (ms):");
                readTimeoutPanel.add(readTimeoutLabel);
                readTimeoutField = new JTextField();
                readTimeoutField.setColumns(10);
                readTimeoutPanel.add(readTimeoutField);
                readTimeoutErrorLabel = new JLabel();
                readTimeoutErrorLabel.setForeground(GuiUtils.getLookAndFeelColor("TextField.errorForeground"));
                addIntegerRangeCheck(readTimeoutField, MIN_READ_TIMEOUT, MAX_READ_TIMEOUT, readTimeoutErrorLabel);
                readTimeoutPanel.add(readTimeoutErrorLabel);
            }
            boxPanel.add(readTimeoutPanel);
        }

        this.setLayout(new BorderLayout());
        add(boxPanel, BorderLayout.NORTH);

        add(createAboutPanel(this));
    }

    void clearGui() {
        super.clearGui();
        connectionTimeoutErrorLabel.setText("");
    }


    public static void main(String[] args) {
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 400);
        frame.getContentPane().add(new OpenWebSocketSamplerGuiPanel());
        frame.setVisible(true);
    }
}
