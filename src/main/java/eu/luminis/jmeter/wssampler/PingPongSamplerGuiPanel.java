/*
 * Copyright 2016, 2017, 2018 Peter Doornbosch
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
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

public class PingPongSamplerGuiPanel extends JPanel {

    JTextField readTimeoutField;
    private final JRadioButton pingPongOption;
    private JLabel timeoutLabel;
    private final JRadioButton pongOption;

    public PingPongSamplerGuiPanel() {
        setLayout(new BorderLayout());

        JPanel requestSettingsPanel = new JPanel();
        {
            requestSettingsPanel.setLayout(new BoxLayout(requestSettingsPanel, BoxLayout.Y_AXIS));
            requestSettingsPanel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createTitledBorder("Behaviour"),
                    BorderFactory.createEmptyBorder(5, 5, 0, 0)));
            pingPongOption = new JRadioButton("ping/pong (send ping, expect pong)");
            requestSettingsPanel.add(pingPongOption);
            pingPongOption.setAlignmentX(JComponent.LEFT_ALIGNMENT);
            pongOption = new JRadioButton("pong (just send pong)");
            requestSettingsPanel.add(pongOption);
            pongOption.setAlignmentX(JComponent.LEFT_ALIGNMENT);
            ButtonGroup buttonGroup = new ButtonGroup();
            buttonGroup.add(pingPongOption);
            buttonGroup.add(pongOption);

            JPanel readTimeoutPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            {
                timeoutLabel = new JLabel("Pong (read) timeout (ms): ");
                readTimeoutPanel.add(timeoutLabel);
                readTimeoutField = new JTextField();
                readTimeoutField.setColumns(5);
                readTimeoutPanel.add(readTimeoutField);
                readTimeoutPanel.setAlignmentX(JComponent.LEFT_ALIGNMENT);
            }
            requestSettingsPanel.add(readTimeoutPanel);

            pingPongOption.addItemListener(e -> {
                updateEnabledState(e.getStateChange() == ItemEvent.SELECTED);
            });

            pingPongOption.setSelected(true);
            updateEnabledState(true);
        }
        add(requestSettingsPanel, BorderLayout.NORTH);
        add(WebSocketSamplerGuiPanel.createAboutPanel(this));
    }

    public void clearGui() {
        readTimeoutField.setText("");
    }

    private void updateEnabledState(boolean isPingPong) {
        readTimeoutField.setEnabled(isPingPong);
        timeoutLabel.setEnabled(isPingPong);
    }

    public PingPongSampler.Type getType() {
        if (pingPongOption.isSelected())
            return PingPongSampler.Type.PingPong;
        else
            return PingPongSampler.Type.Pong;
    }

    public void setType(PingPongSampler.Type type) {
        boolean isPingPong = type.equals(PingPongSampler.Type.PingPong);
        pingPongOption.setSelected(isPingPong);
        pongOption.setSelected(!isPingPong);
        updateEnabledState(isPingPong);
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
