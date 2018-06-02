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

public class PingFrameFilterGuiPanel extends JPanel {

    private final JRadioButton optionBoth;
    private final JRadioButton onlyPing;
    private final JRadioButton onlyPong;
    JCheckBox replyToPing;

    public PingFrameFilterGuiPanel() {
        setLayout(new BoxLayout(this, Y_AXIS));

        JPanel contentPanel = new JPanel();
        {
            contentPanel.setLayout(new BoxLayout(contentPanel, Y_AXIS));
            contentPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Frame filter behaviour"), BorderFactory.createEmptyBorder(10, 10, 10, 10)));

            JPanel labelPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            {
                labelPanel.add(new JLabel("Filter (discard):"));
                labelPanel.setMaximumSize(new Dimension(labelPanel.getMaximumSize().width, labelPanel.getMinimumSize().height));
            }
            contentPanel.add(labelPanel);
            labelPanel.setAlignmentX(JComponent.LEFT_ALIGNMENT);

            JPanel optionPanel = new JPanel();
            {
                optionPanel.setLayout(new BoxLayout(optionPanel, Y_AXIS));
                optionBoth = new JRadioButton("both ping and pong frames");
                optionPanel.add(optionBoth);
                optionBoth.setSelected(true);
                onlyPing = new JRadioButton("ping frames only");
                optionPanel.add(onlyPing);
                onlyPong = new JRadioButton("pong frames only");
                optionPanel.add(onlyPong);
                ButtonGroup optionGroup = new ButtonGroup();
                optionGroup.add(optionBoth);
                optionGroup.add(onlyPing);
                optionGroup.add(onlyPong);
            }
            contentPanel.add(optionPanel);
            optionPanel.setAlignmentX(JComponent.LEFT_ALIGNMENT);

            contentPanel.add(Box.createVerticalStrut(20));
            replyToPing = new JCheckBox("Automatically respond to ping with a pong");
            contentPanel.add(replyToPing);
            replyToPing.setAlignmentX(JComponent.LEFT_ALIGNMENT);

            add(contentPanel);
        }
        JPanel aboutPanel = WebSocketSamplerGuiPanel.createAboutPanel(this);
        add(aboutPanel);
        aboutPanel.setAlignmentX(JComponent.LEFT_ALIGNMENT);
    }

    public PingFrameFilter.PingFilterType getFilterType() {
        if (onlyPing.isSelected())
            return PingFrameFilter.PingFilterType.FilterPingOnly;
        else if (onlyPong.isSelected())
            return PingFrameFilter.PingFilterType.FilterPongOnly;
        else if (optionBoth.isSelected())
            return PingFrameFilter.PingFilterType.FilterAll;
        else
            throw new RuntimeException("Invalid ping filter state");
    }

    public void setFilterType(PingFrameFilter.PingFilterType filterType) {
        if (filterType.equals(PingFrameFilter.PingFilterType.FilterAll)) {
            optionBoth.setSelected(true);
        }
        else if (filterType.equals(PingFrameFilter.PingFilterType.FilterPingOnly)) {
            onlyPing.setSelected(true);
        }
        else if (filterType.equals(PingFrameFilter.PingFilterType.FilterPongOnly)) {
            onlyPong.setSelected(true);
        }
        else {
            throw new RuntimeException("Invalid ping filter state");
        }
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 400);
        frame.getContentPane().add(new PingFrameFilterGuiPanel());
        frame.setVisible(true);
    }
}
