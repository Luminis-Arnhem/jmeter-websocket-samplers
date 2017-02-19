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

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import static javax.swing.BoxLayout.Y_AXIS;

public class PingFrameFilterGuiPanel extends JPanel {

    JCheckBox replyToPing;

    public PingFrameFilterGuiPanel() {
        setLayout(new BoxLayout(this, Y_AXIS));
        setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Frame filter conditions"), BorderFactory.createEmptyBorder(10, 10, 10, 10)));

        add(new JLabel("Filters (discards) both ping and pong frames"));
        replyToPing = new JCheckBox("Automatically respond to ping with a pong");
        add(replyToPing);
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 400);
        frame.getContentPane().add(new PingFrameFilterGuiPanel());
        frame.setVisible(true);
    }
}
