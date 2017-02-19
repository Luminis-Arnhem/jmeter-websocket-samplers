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
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Point;

import static javax.swing.BoxLayout.Y_AXIS;

public class BinaryFrameFilterGuiPanel extends JPanel {

    private final JRadioButton doesButton;
    private final JRadioButton doesNotButton;
    private final JRadioButton containsButton;
    private final JRadioButton startsWithButton;
    private final JRadioButton equalsButton;
    JTextArea binaryContent;
    JTextField matchPosition;

    public BinaryFrameFilterGuiPanel() {
        setLayout(new BoxLayout(this, Y_AXIS));
        setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Frame filter condition"), BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        JPanel settingsPanel = new JPanel();
        {
            settingsPanel.setLayout(new BoxLayout(settingsPanel, BoxLayout.X_AXIS));
            settingsPanel.add(Box.createHorizontalStrut(5));
            settingsPanel.add(new JLabel("Filter will discard any binary frame that "));

            JPanel negationPanel = new JPanel();
            {
                negationPanel.setLayout(new BoxLayout(negationPanel, BoxLayout.Y_AXIS));
                negationPanel.add(Box.createVerticalGlue());
                doesButton = new JRadioButton("does");
                negationPanel.add(doesButton);
                doesNotButton = new JRadioButton("does NOT");
                negationPanel.add(doesNotButton);
                ButtonGroup negationGroup = new ButtonGroup();
                doesButton.setSelected(true);
                negationGroup.add(doesButton);
                negationGroup.add(doesNotButton);
                negationPanel.add(Box.createVerticalGlue());
                negationPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(5, 5, 0, 0), BorderFactory.createEtchedBorder()));
            }
            settingsPanel.add(negationPanel);

            JPanel matchPanel = new JPanel();
            {
                matchPanel.setLayout(new BoxLayout(matchPanel, BoxLayout.Y_AXIS));
                containsButton = new JRadioButton("contain");
                matchPanel.add(containsButton);
                startsWithButton = new JRadioButton("start with");
                matchPanel.add(startsWithButton);
                equalsButton = new JRadioButton("equal");
                matchPanel.add(equalsButton);
                containsButton.setSelected(true);
                ButtonGroup matchGroup = new ButtonGroup();
                matchGroup.add(containsButton);
                matchGroup.add(equalsButton);
                matchGroup.add(startsWithButton);
                matchPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(5, 5, 0, 5), BorderFactory.createEtchedBorder()));
            }
            settingsPanel.add(matchPanel);

            settingsPanel.add(new JLabel("the following binary data:"));
        }
        settingsPanel.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        add(settingsPanel);

        JPanel matchPositionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        {
            matchPositionPanel.add(new JLabel("starting at position "));
            matchPosition = new JTextField();
            matchPosition.setColumns(10);
            matchPositionPanel.add(matchPosition);
        }
        matchPositionPanel.setAlignmentX(JComponent.LEFT_ALIGNMENT);

        JSplitPane splitter = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        {
            JPanel borderPanel = new JPanel();
            {
                borderPanel.setBorder(BorderFactory.createTitledBorder(null, "Binary data", TitledBorder.LEFT, TitledBorder.DEFAULT_POSITION));
                borderPanel.setLayout(new BorderLayout());
                binaryContent = new JTextArea();
                binaryContent.setRows(5);
                borderPanel.add(binaryContent);
            }

            splitter.setBorder(null);
            splitter.setTopComponent(borderPanel);
            splitter.setBottomComponent(matchPositionPanel);
        }
        splitter.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        add(splitter);

        // Temporary disable anything not yet implemented
        doesNotButton.setEnabled(false);
        startsWithButton.setEnabled(false);
        equalsButton.setEnabled(false);
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 400);
        frame.setLocation(new Point(400, 400));
        frame.getContentPane().add(new BinaryFrameFilterGuiPanel());
        frame.setVisible(true);
    }
}
