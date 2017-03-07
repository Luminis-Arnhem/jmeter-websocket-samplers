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
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import static eu.luminis.jmeter.wssampler.BinaryFrameFilter.ComparisonType.*;
import static javax.swing.BoxLayout.Y_AXIS;

public class TextFrameFilterGuiPanel extends JPanel {

    private final JButton testRegexButton;
    private JPanel borderPanel;
    JTextArea matchValue;
    private JComboBox typeSelector1;
    private JComboBox typeSelector2;
    private JComboBox typeSelector3;
    private boolean useRegex;
    private DynamicTitledBorder matchPanelPanelBorder;

    public TextFrameFilterGuiPanel() {
        setLayout(new BoxLayout(this, Y_AXIS));
        setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Frame filter condition"), BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        JPanel settingsPanel = new JPanel();
        {
            settingsPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
            settingsPanel.add(new JLabel("Discard any text frame"));

            typeSelector1 = new JComboBox(new String[] { "", "that", "that does NOT"});
            settingsPanel.add(typeSelector1);

            typeSelector2 = new JComboBox();
            typeSelector2.setPrototypeDisplayValue("starts with");
            settingsPanel.add(typeSelector2);

            typeSelector3 = new JComboBox();
            typeSelector3.setPrototypeDisplayValue("regular expression");
            settingsPanel.add(typeSelector3);
        }
        settingsPanel.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        add(settingsPanel);

        JSplitPane splitter = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        {
            borderPanel = new JPanel();
            {
                matchPanelPanelBorder = new DynamicTitledBorder(null, "Text ", TitledBorder.LEFT, TitledBorder.DEFAULT_POSITION);
                borderPanel.setBorder(BorderFactory.createCompoundBorder(matchPanelPanelBorder, new EmptyBorder(9 ,9, 9, 9)));
                borderPanel.setLayout(new BorderLayout());
                matchValue = new JTextArea();
                matchValue.setRows(2);
                matchValue.setEnabled(false);
                matchPanelPanelBorder.setEnabled(false);
                borderPanel.add(matchValue);
            }

            JPanel matchPositionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
            {
                testRegexButton = new JButton("Test regular expression");
                testRegexButton.setEnabled(false);
                testRegexButton.addActionListener(e -> {
                    new TestRegexDialog(SwingUtilities.getWindowAncestor(TextFrameFilterGuiPanel.this), matchValue.getText(), dlg -> {
                        if (dlg.isReturnRegex()) {
                            matchValue.setText(dlg.getRegex());
                        }
                        return dlg.isReturnRegex();
                    }).setVisible(true);
                });
                matchPositionPanel.add(testRegexButton);
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
                    typeSelector3.removeAllItems();
                    matchValue.setEnabled(false);
                    matchPanelPanelBorder.setEnabled(false);
                    borderPanel.repaint();
                    break;
                case 1:
                    typeSelector2.addItem("contains"); typeSelector2.addItem("starts with"); typeSelector2.addItem("equals"); typeSelector2.addItem("ends with");
                    if (selectedItem >= 0)
                        typeSelector2.setSelectedIndex(selectedItem);
                    matchValue.setEnabled(true);
                    matchPanelPanelBorder.setEnabled(true);
                    borderPanel.repaint();
                    break;
                case 2:
                    typeSelector2.addItem("contain"); typeSelector2.addItem("start with"); typeSelector2.addItem("equal"); typeSelector2.addItem("end with");
                    if (selectedItem >= 0)
                        typeSelector2.setSelectedIndex(selectedItem);
                    matchValue.setEnabled(true);
                    matchPanelPanelBorder.setEnabled(true);
                    borderPanel.repaint();
            }
        });
        typeSelector2.addActionListener(e -> {
            int previousSelection = typeSelector3.getSelectedIndex();
            typeSelector3.removeAllItems();
            if (typeSelector2.getSelectedIndex() == 1 || typeSelector2.getSelectedIndex() == 3) {
                typeSelector3.addItem("text");
            }
            else {
                typeSelector3.addItem("text");
                typeSelector3.addItem("regular expression");
                if (previousSelection != -1)
                    typeSelector3.setSelectedIndex(previousSelection);
            }
        });
        typeSelector3.addActionListener(e -> {
            useRegex = typeSelector3.getSelectedIndex() == 1;
            matchPanelPanelBorder.setTitle(useRegex ? "Regular expression": "Text");
            testRegexButton.setEnabled(useRegex);
            borderPanel.repaint();

            if (useRegex)
                compileRegex();
            else
                resetRegexValidation();
        });

        matchValue.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                if (useRegex)
                    compileRegex();
                else
                    resetRegexValidation();
            }
            public void removeUpdate(DocumentEvent e) {
                if (useRegex)
                    compileRegex();
                else
                    resetRegexValidation();
            }
            public void insertUpdate(DocumentEvent e) {
                if (useRegex)
                    compileRegex();
                else
                    resetRegexValidation();
            }
        });

    }

    void compileRegex() {
        try {
            Pattern.compile(matchValue.getText());
            matchValue.setForeground(Color.BLACK);
        }
        catch (PatternSyntaxException exception) {
            matchValue.setForeground(Color.RED);
        }
    }

    void resetRegexValidation() {
        matchValue.setForeground(Color.BLACK);
    }

    BinaryFrameFilter.ComparisonType getComparisonType() {
        int code = 10 * typeSelector1.getSelectedIndex() + typeSelector2.getSelectedIndex();
        switch (code) {
            case 0: return IsBinary;
            case 10 + 0: return Contains;
            case 10 + 1: return StartsWith;
            case 10 + 2: return Equals;
            case 10 + 3: return EndsWith;
            case 20 + 0: return NotContains;
            case 20 + 1: return NotStartsWith;
            case 20 + 2: return NotEquals;
            case 20 + 3: return NotEndsWith;
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
            case EndsWith: typeSelector1.setSelectedIndex(1); typeSelector2.setSelectedIndex(3);
                break;
            case NotContains: typeSelector1.setSelectedIndex(2); typeSelector2.setSelectedIndex(0);
                break;
            case NotStartsWith: typeSelector1.setSelectedIndex(2); typeSelector2.setSelectedIndex(1);
                break;
            case NotEquals: typeSelector1.setSelectedIndex(2); typeSelector2.setSelectedIndex(2);
                break;
            case NotEndsWith: typeSelector1.setSelectedIndex(2); typeSelector2.setSelectedIndex(3);
                break;
            default:
                throw new RuntimeException("invalid comparison type");
        }
    }


    public static void showDialog(Window parent, String regex, Function<TestRegexDialog, Boolean> closeCallback) {
        TestRegexDialog testRegexDlg = new TestRegexDialog(parent, regex, closeCallback);
    }

    public static void main(String[] args) {
        //showDialog(null, "a+b*c?");

        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(700, 400);
        frame.setLocation(new Point(400, 400));
        frame.getContentPane().add(new TextFrameFilterGuiPanel());
        frame.setVisible(true);
    }

}
