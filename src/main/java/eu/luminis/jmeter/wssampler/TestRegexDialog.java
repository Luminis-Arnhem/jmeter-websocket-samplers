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

import oracle.swing.SpringUtilities;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

class TestRegexDialog extends JDialog {

    private int initialNrOfSamples = 3;
    private JTextField regex;
    private Pattern regexPattern;
    private List<JTextField> samples = new ArrayList<>();
    private List<JLabel> yesLabels = new ArrayList<>();
    private List<JLabel> noLabels = new ArrayList<>();

    private boolean returnRegex = false;
    private boolean mustMatch;

    public TestRegexDialog(Window parent, String initialRegexValue, boolean mustMatch, Function<TestRegexDialog, Boolean> closeCallback) {
        super(parent);
        setTitle("Test regular expression - " + (mustMatch? "full match": "contains match"));
        this.mustMatch = mustMatch;
        setModal(true);


        JPanel outerPanel = new JPanel();
        {
            outerPanel.setLayout(new BoxLayout(outerPanel, BoxLayout.Y_AXIS));
            outerPanel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createEmptyBorder(5, 5, 5, 5),
                    BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(null, ""),
                            BorderFactory.createEmptyBorder(5, 5, 5, 5))));

            JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 9));
            {
                JLabel usageLabel = new JLabel(mustMatch ? "<html>Samples must <em>match</em> regex.</html>" : "<html>Samples must <em>contain</em> value matching regex.</html>");
                headerPanel.add(usageLabel);
            }
            outerPanel.add(headerPanel, BorderLayout.NORTH);
            headerPanel.setAlignmentX(JComponent.LEFT_ALIGNMENT);

            JPanel gridPanel = new JPanel();
            {
                gridPanel.setLayout(new SpringLayout());

                gridPanel.add(new JLabel("Regular expression:"));
                regex = new JTextField(initialRegexValue);
                gridPanel.add(regex);
                gridPanel.add(Box.createHorizontalStrut(1));  // dummy column needed for SpringLayout
                gridPanel.add(Box.createHorizontalStrut(1));

                for (int i = 0; i < initialNrOfSamples; i++) {
                    gridPanel.add(new JLabel("Sample " + (i + 1) + ":"));
                    JTextField sample = new JTextField("<add sample text here>");
                    gridPanel.add(sample);
                    samples.add(sample);
                    JLabel yes = new JLabel("\u2713");
                    yes.setForeground(GuiUtils.getLookAndFeelColor("Icon.okForeground"));
                    gridPanel.add(yes);
                    yesLabels.add(yes);
                    JLabel no = new JLabel("\u2715");
                    no.setForeground(GuiUtils.getLookAndFeelColor("Icon.errorForeground"));
                    gridPanel.add(no);
                    noLabels.add(no);
                }
                SpringUtilities.makeCompactGrid(gridPanel, 1 + initialNrOfSamples, 4,
                        6, 6, 6, 6);
            }
            outerPanel.add(gridPanel);
            gridPanel.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        }
        add(outerPanel);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 1, 3));
        {
            JButton useThisButton = new JButton("Use this regex");
            useThisButton.addActionListener(e -> {
                returnRegex = true;
                dispose();
            });
            buttonPanel.add(useThisButton);
            JButton closeButton = new JButton("Close");
            closeButton.addActionListener(e -> dispose());
            buttonPanel.add(closeButton);
        }
        add(buttonPanel, BorderLayout.SOUTH);
        pack();

        regex.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                compileRegex();
            }
            public void removeUpdate(DocumentEvent e) {
                compileRegex();
            }
            public void insertUpdate(DocumentEvent e) {
                compileRegex();
            }
        });

        for (JTextField sample: samples)
            sample.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                evaluate();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                evaluate();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                evaluate();
            }
        });

        compileRegex();

        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                super.windowClosed(e);
                closeCallback.apply(TestRegexDialog.this);
            }
        });
        setLocationRelativeTo(getParent());
    }

    void compileRegex() {
        try {
            regexPattern = Pattern.compile(regex.getText());
            regex.setForeground(GuiUtils.getLookAndFeelColor("TextField.foreground"));
            evaluate();
        }
        catch (PatternSyntaxException exception) {
            setIndeterminate();
            regex.setForeground(GuiUtils.getLookAndFeelColor("TextField.errorForeground"));
        }
    }

    private void setIndeterminate() {
        for (JLabel yes: yesLabels)
        yes.setVisible(false);
        for (JLabel no: noLabels)
        no.setVisible(false);
    }

    void evaluate() {
        for (int i = 0; i < samples.size(); i++) {
            Matcher matcher = regexPattern.matcher(samples.get(i).getText());
            boolean matches = mustMatch? matcher.matches(): matcher.find();
            yesLabels.get(i).setVisible(matches);
            noLabels.get(i).setVisible(!matches);
        }
    }

    public boolean isReturnRegex() {
        return returnRegex;
    }

    public String getRegex() {
        return regex.getText();
    }
}
