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
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import static eu.luminis.jmeter.wssampler.ComparisonType.*;
import static eu.luminis.jmeter.wssampler.WebSocketSamplerGuiPanel.createAboutPanel;
import static javax.swing.BoxLayout.Y_AXIS;

public class TextFrameFilterGuiPanel extends JPanel {

    private final JButton testRegexButton;
    private JPanel matchPanel;
    JTextArea matchValue;
    private JComboBox typeSelector1;
    private JComboBox typeSelector2;
    private JComboBox typeSelector3;
    private boolean useRegex;
    private DynamicTitledBorder matchPanelPanelBorder;

    public TextFrameFilterGuiPanel() {
        setLayout(new BoxLayout(this, Y_AXIS));

        JSplitPane splitter = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        {
            JPanel contentPanel = new JPanel();
            {
                contentPanel.setLayout(new BoxLayout(contentPanel, Y_AXIS));
                contentPanel.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createTitledBorder("Frame filter condition"),
                        BorderFactory.createEmptyBorder(1, 5, 1, 1)));

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
                contentPanel.add(settingsPanel);
                settingsPanel.setAlignmentX(JComponent.LEFT_ALIGNMENT);

                matchPanel = new JPanel();
                {
                    matchPanelPanelBorder = new DynamicTitledBorder(null, "Text ", TitledBorder.LEFT, TitledBorder.DEFAULT_POSITION);
                    matchPanel.setBorder(BorderFactory.createCompoundBorder(matchPanelPanelBorder, new EmptyBorder(5, 5, 5, 5)));
                    matchPanel.setLayout(new BorderLayout());
                    matchValue = new JTextArea();
                    matchValue.setLineWrap(true);
                    matchValue.setRows(2);

                    JScrollPane scrollPane = new JScrollPane(matchValue);
                    scrollPane.setMaximumSize(new Dimension(scrollPane.getMaximumSize().width, Integer.MAX_VALUE));
                    scrollPane.setBorder(new JTextField().getBorder());
                    scrollPane.setAlignmentY(0.5f);

                    matchValue.setEnabled(false);
                    matchPanelPanelBorder.setEnabled(false);
                    matchPanel.add(scrollPane);
                }
                contentPanel.add(matchPanel);
                matchPanel.setAlignmentX(JComponent.LEFT_ALIGNMENT);

                JPanel matchPositionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, -4, 0));
                {
                    testRegexButton = new JButton("Test regular expression");
                    testRegexButton.setEnabled(false);
                    testRegexButton.addActionListener(e -> {
                        new TestRegexDialog(SwingUtilities.getWindowAncestor(TextFrameFilterGuiPanel.this), matchValue.getText(), typeSelector2.getSelectedIndex() == 2, dlg -> {
                            if (dlg.isReturnRegex()) {
                                matchValue.setText(dlg.getRegex());
                            }
                            return dlg.isReturnRegex();
                        }).setVisible(true);
                    });
                    matchPositionPanel.add(testRegexButton);
                }
                matchPositionPanel.setAlignmentX(JComponent.LEFT_ALIGNMENT);
                contentPanel.add(matchPositionPanel);
            }

            splitter.setBorder(null);
            splitter.setTopComponent(contentPanel);
            splitter.setBottomComponent(createAboutPanel(this));
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
                    matchPanel.repaint();
                    break;
                case 1:
                    typeSelector2.addItem("contains"); typeSelector2.addItem("starts with"); typeSelector2.addItem("equals"); typeSelector2.addItem("ends with");
                    if (selectedItem >= 0)
                        typeSelector2.setSelectedIndex(selectedItem);
                    matchValue.setEnabled(true);
                    matchPanelPanelBorder.setEnabled(true);
                    matchPanel.repaint();
                    break;
                case 2:
                    typeSelector2.addItem("contain"); typeSelector2.addItem("start with"); typeSelector2.addItem("equal"); typeSelector2.addItem("end with");
                    if (selectedItem >= 0)
                        typeSelector2.setSelectedIndex(selectedItem);
                    matchValue.setEnabled(true);
                    matchPanelPanelBorder.setEnabled(true);
                    matchPanel.repaint();
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
            matchPanel.repaint();

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
            matchValue.setForeground(GuiUtils.getLookAndFeelColor("TextArea.foreground"));
        }
        catch (PatternSyntaxException exception) {
            matchValue.setForeground(GuiUtils.getLookAndFeelColor("TextField.errorForeground"));
        }
    }

    void resetRegexValidation() {
        matchValue.setForeground(GuiUtils.getLookAndFeelColor("TextArea.foreground"));
    }

    ComparisonType getComparisonType() {
        int code = 100 * typeSelector1.getSelectedIndex() + 10 * typeSelector2.getSelectedIndex() + typeSelector3.getSelectedIndex();
        switch (code) {
            case -111:
            case -11:
            case -1:
            case 0:   return IsPlain;
            case 100: return Contains;
            case 101: return ContainsRegex;
            case 110: return StartsWith;
            case 120: return Equals;
            case 121: return EqualsRegex;
            case 130: return EndsWith;
            case 200: return NotContains;
            case 201: return NotContainsRegex;
            case 210: return NotStartsWith;
            case 220: return NotEquals;
            case 221: return NotEqualsRegex;
            case 230: return NotEndsWith;
            default:
                throw new RuntimeException("invalid comparison type (" + code + ")");
        }
    }

    void setComparisonType(ComparisonType type) {
        switch(type) {
            case IsPlain: typeSelector1.setSelectedIndex(0);
                break;
            case Contains: typeSelector1.setSelectedIndex(1); typeSelector2.setSelectedIndex(0); typeSelector3.setSelectedIndex(0);
                break;
            case ContainsRegex: typeSelector1.setSelectedIndex(1); typeSelector2.setSelectedIndex(0); typeSelector3.setSelectedIndex(1);
                break;
            case StartsWith: typeSelector1.setSelectedIndex(1); typeSelector2.setSelectedIndex(1); typeSelector3.setSelectedIndex(0);
                break;
            case Equals: typeSelector1.setSelectedIndex(1); typeSelector2.setSelectedIndex(2); typeSelector3.setSelectedIndex(0);
                break;
            case EqualsRegex: typeSelector1.setSelectedIndex(1); typeSelector2.setSelectedIndex(2); typeSelector3.setSelectedIndex(1);
                break;
            case EndsWith: typeSelector1.setSelectedIndex(1); typeSelector2.setSelectedIndex(3); typeSelector3.setSelectedIndex(0);
                break;
            case NotContains: typeSelector1.setSelectedIndex(2); typeSelector2.setSelectedIndex(0); typeSelector3.setSelectedIndex(0);
                break;
            case NotContainsRegex: typeSelector1.setSelectedIndex(2); typeSelector2.setSelectedIndex(0); typeSelector3.setSelectedIndex(1);
                break;
            case NotStartsWith: typeSelector1.setSelectedIndex(2); typeSelector2.setSelectedIndex(1); typeSelector3.setSelectedIndex(0);
                break;
            case NotEquals: typeSelector1.setSelectedIndex(2); typeSelector2.setSelectedIndex(2); typeSelector3.setSelectedIndex(0);
                break;
            case NotEqualsRegex: typeSelector1.setSelectedIndex(2); typeSelector2.setSelectedIndex(2); typeSelector3.setSelectedIndex(1);
                break;
            case NotEndsWith: typeSelector1.setSelectedIndex(2); typeSelector2.setSelectedIndex(3); typeSelector3.setSelectedIndex(0);
                break;
            default:
                throw new RuntimeException("invalid comparison type");
        }
    }


    public static void main(String[] args) {
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(700, 400);
        frame.setLocation(new Point(400, 400));
        frame.getContentPane().add(new TextFrameFilterGuiPanel());
        frame.setVisible(true);
    }

}
