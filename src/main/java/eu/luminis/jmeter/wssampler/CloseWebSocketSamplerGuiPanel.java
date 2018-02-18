/*
 * Copyright 2016, 2017 Peter Doornbosch
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

public class CloseWebSocketSamplerGuiPanel extends WebSocketSamplerGuiPanel {

    JTextField closeStatusField;
    JTextField readTimeoutField;

    public CloseWebSocketSamplerGuiPanel() {
        setLayout(new BorderLayout());

        JPanel layoutPanel = new JPanel();
        {
            layoutPanel.setLayout(new BoxLayout(layoutPanel, BoxLayout.Y_AXIS));

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
                    addIntegerRangeCheck(closeStatusField, 1000, 4999, closeStatusErrorField);
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
                    addIntegerRangeCheck(readTimeoutField, MIN_READ_TIMEOUT, MAX_READ_TIMEOUT, readTimeoutErrorField);
                }
                settingsPanel.add(readTimeoutPanel);
                settingsPanel.setAlignmentX(JComponent.LEFT_ALIGNMENT);
            }
            layoutPanel.add(settingsPanel);

            {
                connectionIdPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Connection"),
                        BorderFactory.createEmptyBorder(5, 5, 0, 5)));
                connectionIdLabel = new JLabel("Connection ID:");
                connectionIdPanel.add(connectionIdLabel);
                connectionIdField = new JTextField(10);
                connectionIdPanel.add(connectionIdField);
            }
            connectionIdPanel.setAlignmentX(JComponent.LEFT_ALIGNMENT);
            layoutPanel.add(connectionIdPanel);

            JPanel aboutPanel = WebSocketSamplerGuiPanel.createAboutPanel(this);
            aboutPanel.setAlignmentX(JComponent.LEFT_ALIGNMENT);
            layoutPanel.add(aboutPanel);
        }

        add(layoutPanel, BorderLayout.NORTH);
    }

    public void clearGui() {
        closeStatusField.setText("");
        readTimeoutField.setText("");
    }


    public static void main(String[] args) {
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 400);
        frame.getContentPane().add(new CloseWebSocketSamplerGuiPanel());
        frame.setVisible(true);
    }
}
