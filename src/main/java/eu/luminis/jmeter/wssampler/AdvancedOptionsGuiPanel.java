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

import static javax.swing.BoxLayout.Y_AXIS;

public class AdvancedOptionsGuiPanel extends JPanel {

    JCheckBox enabled_multiple_connections_per_thread;

    public AdvancedOptionsGuiPanel() {
        setLayout(new BorderLayout());

        JPanel contentPanel = new JPanel();
        {
            contentPanel.setLayout(new BoxLayout(contentPanel, Y_AXIS));
            contentPanel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createTitledBorder("Advanced options"),
                    BorderFactory.createEmptyBorder(10,10,10,10)));
            enabled_multiple_connections_per_thread = new JCheckBox("Enable multiple connections per thread");
            contentPanel.add(enabled_multiple_connections_per_thread);
            enabled_multiple_connections_per_thread.setAlignmentX(JComponent.LEFT_ALIGNMENT);

            contentPanel.add(Box.createVerticalStrut(10));

            JPanel explanationPanel = new JPanel(new BorderLayout()) {
                @Override
                public Dimension getPreferredSize() {
                    Dimension prefSize = super.getPreferredSize();
                    prefSize.width = enabled_multiple_connections_per_thread.getMinimumSize().width;
                    return prefSize;
                }
            };
            {
                JLabel explanationLabel = new JLabel("<html>" +
                        "When set, each websocket sampler gets a \"connection id\" field, that can be used to identify (or name) " +
                        "the connection that is used (or created) by that sampler. The scope of the connection id's is the current (JMeter) thread; " +
                        "websocket connections cannot be shared amongst JMeter threads. " +
                        "The connection id can have any value and can include JMeter properties. " +
                        "When not set, the default connection for the JMeter thread is used." +
                        "</html>");
                explanationPanel.add(explanationLabel, BorderLayout.CENTER);
            }
            contentPanel.add(explanationPanel);
            explanationPanel.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        }
        add(contentPanel, BorderLayout.NORTH);

        JPanel aboutPanel = WebSocketSamplerGuiPanel.createAboutPanel(this);
        add(aboutPanel);
    }

    public void clearGui() {
        enabled_multiple_connections_per_thread.setSelected(false);
    }


    public static void main(String[] args) {
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocation(new Point(400, 400));
        frame.setSize(600, 400);
        frame.getContentPane().add(new AdvancedOptionsGuiPanel());
        frame.setVisible(true);
    }

}
