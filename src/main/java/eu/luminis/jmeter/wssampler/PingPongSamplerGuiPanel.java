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

public class PingPongSamplerGuiPanel extends JPanel {

    JTextField readTimeoutField;

    public PingPongSamplerGuiPanel() {
        setLayout(new BorderLayout());

        JPanel requestSettingsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        {
            requestSettingsPanel.setBorder(BorderFactory.createTitledBorder("Data (pong frame)"));
            requestSettingsPanel.add(new JLabel("Pong (read) timeout (ms): "));
            readTimeoutField = new JTextField();
            readTimeoutField.setColumns(5);
            requestSettingsPanel.add(readTimeoutField);
        }
        add(requestSettingsPanel, BorderLayout.NORTH);
        add(WebSocketSamplerGuiPanel.createAboutPanel(this));
    }

    public void clearGui() {
        readTimeoutField.setText("");
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 400);
        frame.setLocation(new Point(400, 400));
        frame.getContentPane().add(new PingPongSamplerGuiPanel());
        frame.setVisible(true);
    }
}
