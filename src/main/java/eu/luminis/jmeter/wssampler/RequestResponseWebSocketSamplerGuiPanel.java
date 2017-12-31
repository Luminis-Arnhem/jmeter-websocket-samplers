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
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.util.Arrays;

import static javax.swing.BoxLayout.X_AXIS;
import static javax.swing.BoxLayout.Y_AXIS;

public class RequestResponseWebSocketSamplerGuiPanel extends WebSocketSamplerGuiPanel {

    public static final String BINARY = "Binary";
    public static final String TEXT = "Text";

    JTextArea requestDataField;
    JComboBox typeSelector;
    private JLabel messageField;

    public RequestResponseWebSocketSamplerGuiPanel() {
        init();
    }

    private void init() {

        this.setLayout(new BoxLayout(this, Y_AXIS));

        JPanel connectionPanel = createConnectionPanel();
        this.add(connectionPanel);
        connectionPanel.setAlignmentX(JComponent.LEFT_ALIGNMENT);

        JSplitPane splitter = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        {
            JPanel dataPanel = new JPanel();
            {
                dataPanel.setBorder(BorderFactory.createTitledBorder("Data"));
                dataPanel.setLayout(new BoxLayout(dataPanel, BoxLayout.Y_AXIS));

                JPanel topBar = new JPanel();
                {
                    topBar.setLayout(new BoxLayout(topBar, BoxLayout.X_AXIS));
                    String[] typeOptions = {TEXT, BINARY};
                    typeSelector = new JComboBox(typeOptions);
                    typeSelector.setMaximumSize(typeSelector.getMinimumSize());
                    typeSelector.addActionListener(e -> {
                        checkBinary();
                    });
                    topBar.add(typeSelector);
                    topBar.add(Box.createHorizontalStrut(10));
                    messageField = new JLabel();
                    messageField.setForeground(GuiUtils.getLookAndFeelColor("TextField.errorForeground"));
                    topBar.add(messageField);
                    topBar.add(Box.createHorizontalGlue());
                }
                dataPanel.add(topBar);

                JPanel dataZone = new JPanel();
                {
                    dataZone.setLayout(new BoxLayout(dataZone, X_AXIS));
                    dataZone.add(Box.createHorizontalStrut(5));
                    JLabel label = new JLabel("Request data: ");
                    label.setBorder(BorderFactory.createEmptyBorder(3, 0, 0, 0));
                    label.setMaximumSize(new Dimension(label.getMaximumSize().width, Integer.MAX_VALUE));
                    label.setVerticalAlignment(JLabel.TOP);
                    dataZone.add(label);
                    requestDataField = new JTextArea();
                    requestDataField.setColumns(40);
                    requestDataField.setLineWrap(true);
                    requestDataField.setBorder(BorderFactory.createEmptyBorder());
                    // Add a simple (huhuh!) on-change handler....
                    requestDataField.getDocument().addDocumentListener(new DocumentListener() {
                        public void changedUpdate(DocumentEvent e) {
                            checkBinary();
                        }

                        public void removeUpdate(DocumentEvent e) {
                            checkBinary();
                        }

                        public void insertUpdate(DocumentEvent e) {
                            checkBinary();
                        }
                    });
                    JScrollPane scrollPane = new JScrollPane(requestDataField);
                    scrollPane.setMaximumSize(new Dimension(scrollPane.getMaximumSize().width, Integer.MAX_VALUE));
                    scrollPane.setBorder(new JTextField().getBorder());
                    scrollPane.setAlignmentY(0.5f);
                    dataZone.add(scrollPane);
                }
                dataPanel.add(dataZone);

                JPanel requestSettingsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
                {
                    requestSettingsPanel.add(new JLabel("Response (read) timeout (ms): "));
                    readTimeoutField = new JTextField();
                    readTimeoutField.setColumns(10);
                    JLabel readTimeoutErrorField = new JLabel();
                    readTimeoutErrorField.setForeground(GuiUtils.getLookAndFeelColor("TextField.errorForeground"));
                    addIntegerRangeCheck(readTimeoutField, MIN_READ_TIMEOUT, MAX_READ_TIMEOUT, readTimeoutErrorField);
                    requestSettingsPanel.add(readTimeoutField);
                    requestSettingsPanel.add(readTimeoutErrorField);
                }
                dataPanel.add(requestSettingsPanel);
            }
            splitter.setTopComponent(dataPanel);
            splitter.setBottomComponent(createAboutPanel(this));
            splitter.setBorder(null);
        }
        this.add(splitter);
        splitter.setAlignmentX(JComponent.LEFT_ALIGNMENT);

        JPanel stuffIt = new JPanel();
        this.add(stuffIt);
        stuffIt.setAlignmentX(JComponent.LEFT_ALIGNMENT);
    }

    void clearGui() {
        super.clearGui();
        requestDataField.setText("");
        messageField.setText("");
        setCreateNewConnection(true);
    }

    private void checkBinary() {
        if (typeSelector.getSelectedItem() == BINARY) {
            try {
                BinaryUtils.parseBinaryString(stripJMeterVariables(requestDataField.getText()));
                messageField.setText("");
            }
            catch (NumberFormatException notNumber) {
                messageField.setText("Error: request data is not in binary format; use format like '0xca 0xfe' or 'ba be' (JMeter variables like ${var} are allowed).");
            }
        }
        else {
            messageField.setText("");
        }
    }


    public static void main(String[] args) {
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 400);
        frame.getContentPane().add(new RequestResponseWebSocketSamplerGuiPanel());
        frame.setVisible(true);
    }
}
