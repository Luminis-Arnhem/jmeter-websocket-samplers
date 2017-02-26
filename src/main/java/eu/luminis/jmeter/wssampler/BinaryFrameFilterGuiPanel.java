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
import javax.swing.border.TitledBorder;
import java.awt.*;

import static eu.luminis.jmeter.wssampler.BinaryFrameFilter.ComparisonType.*;
import static javax.swing.BoxLayout.Y_AXIS;

public class BinaryFrameFilterGuiPanel extends JPanel {

    private JPanel borderPanel;
    private JLabel matchPositionLabel;
    JTextArea binaryContent;
    JTextField matchPosition;
    private JComboBox typeSelector1;
    private JComboBox typeSelector2;
    JLabel binaryDataLabel;

    public BinaryFrameFilterGuiPanel() {
        setLayout(new BoxLayout(this, Y_AXIS));
        setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Frame filter condition"), BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        JPanel settingsPanel = new JPanel();
        {
            settingsPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
            //settingsPanel.add(Box.createHorizontalStrut(5));
            settingsPanel.add(new JLabel("Discard any binary frame"));

            typeSelector1 = new JComboBox(new String[] { "", "that", "that does NOT"});
            settingsPanel.add(typeSelector1);

            typeSelector2 = new JComboBox();
            typeSelector2.setPrototypeDisplayValue("starts with");
            settingsPanel.add(typeSelector2);

            binaryDataLabel = new JLabel("the following binary data:");
            binaryDataLabel.setEnabled(false);
            settingsPanel.add(binaryDataLabel);

        }
        settingsPanel.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        add(settingsPanel);

        JSplitPane splitter = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        {
            borderPanel = new JPanel();
            {
                borderPanel.setBorder(new DynamicTitledBorder(null, "Binary data", TitledBorder.LEFT, TitledBorder.DEFAULT_POSITION));
                borderPanel.setLayout(new BorderLayout());
                binaryContent = new JTextArea();
                binaryContent.setRows(5);
                binaryContent.setEnabled(false);
                ((DynamicTitledBorder) borderPanel.getBorder()).setEnabled(false);
                borderPanel.add(binaryContent);
            }

            JPanel matchPositionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
            {
                matchPositionLabel = new JLabel("starting at position ");
                matchPositionPanel.add(matchPositionLabel);
                matchPosition = new JTextField();
                matchPosition.setColumns(10);
                matchPositionPanel.add(matchPosition);
            }
            matchPositionPanel.setAlignmentX(JComponent.LEFT_ALIGNMENT);

            splitter.setBorder(null);
            splitter.setTopComponent(borderPanel);
            splitter.setBottomComponent(matchPositionPanel);
        }
        splitter.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        add(splitter);

        typeSelector1.addActionListener(e -> {
            int selectedItem = typeSelector2.getSelectedIndex();
            typeSelector2.removeAllItems();
            switch (typeSelector1.getSelectedIndex()) {
                case 0:
                    typeSelector2.addItem("");
                    binaryDataLabel.setEnabled(false);
                    binaryContent.setEnabled(false);
                    ((DynamicTitledBorder) borderPanel.getBorder()).setEnabled(false);
                    matchPosition.setEnabled(false);
                    matchPositionLabel.setEnabled(false);
                    borderPanel.repaint();
                    break;
                case 1:
                    typeSelector2.addItem("contains"); typeSelector2.addItem("starts with"); typeSelector2.addItem("equals");
                    if (selectedItem >= 0)
                        typeSelector2.setSelectedIndex(selectedItem);
                    binaryDataLabel.setEnabled(true);
                    binaryContent.setEnabled(true);
                    ((DynamicTitledBorder) borderPanel.getBorder()).setEnabled(true);
                    matchPosition.setEnabled(typeSelector2.getSelectedIndex() == 0);
                    matchPositionLabel.setEnabled(typeSelector2.getSelectedIndex() == 0);
                    borderPanel.repaint();
                    break;
                case 2:
                    typeSelector2.addItem("contain"); typeSelector2.addItem("start with"); typeSelector2.addItem("equal");
                    if (selectedItem >= 0)
                        typeSelector2.setSelectedIndex(selectedItem);
                    binaryDataLabel.setEnabled(true);
                    binaryContent.setEnabled(true);
                    ((DynamicTitledBorder) borderPanel.getBorder()).setEnabled(true);
                    matchPosition.setEnabled(typeSelector2.getSelectedIndex() == 0);
                    matchPositionLabel.setEnabled(typeSelector2.getSelectedIndex() == 0);
                    borderPanel.repaint();
            }
        });
        typeSelector2.addActionListener(e -> {
            boolean enableMatchPosition = typeSelector2.getSelectedIndex() == 0;
            matchPosition.setEnabled(enableMatchPosition);
            matchPositionLabel.setEnabled(enableMatchPosition);
        });
    }

    BinaryFrameFilter.ComparisonType getComparisonType() {
        int code = 10 * typeSelector1.getSelectedIndex() + typeSelector2.getSelectedIndex();
        switch (code) {
            case 0: return IsBinary;
            case 10 + 0: return Contains;
            case 10 + 1: return StartsWith;
            case 10 + 2: return Equals;
            case 20 + 0: return NotContains;
            case 20 + 1: return NotStartsWith;
            case 20 + 2: return NotEquals;
            default:
                throw new RuntimeException("invalid comparison type");
        }
    }

    void setComparisonType(BinaryFrameFilter.ComparisonType type) {
        switch(type) {
            case IsBinary: typeSelector1.setSelectedIndex(0);
                break;
            case Contains: typeSelector1.setSelectedIndex(1); typeSelector2.setSelectedIndex(0);
                break;
            case StartsWith: typeSelector1.setSelectedIndex(1); typeSelector2.setSelectedIndex(1);
                break;
            case Equals: typeSelector1.setSelectedIndex(1); typeSelector2.setSelectedIndex(2);
                break;
            case NotContains: typeSelector1.setSelectedIndex(2); typeSelector2.setSelectedIndex(0);
                break;
            case NotStartsWith: typeSelector1.setSelectedIndex(2); typeSelector2.setSelectedIndex(1);
                break;
            case NotEquals: typeSelector1.setSelectedIndex(2); typeSelector2.setSelectedIndex(2);
                break;
            default:
                throw new RuntimeException("invalid comparison type");
        }
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
