/*
 * Copyright 2016 Peter Doornbosch
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
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
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
import java.util.ArrayList;
import java.util.List;

import static javax.swing.BoxLayout.X_AXIS;
import static javax.swing.BoxLayout.Y_AXIS;

public class RequestResponseWebSocketSamplerGuiPanel extends WebSocketSamplerGuiPanel {

    public static final String BINARY = "Binary";
    public static final String TEXT = "Text";

    JTextArea requestDataField;
    JComboBox typeSelector;
    private JLabel messageField;
    JRadioButton reuseConnection;
    JRadioButton newConnection;
    List<JComponent> connectionRelatedSettings = new ArrayList<>();
    JTextField connectionTimeoutField;
    JTextField readTimeoutField;

    public RequestResponseWebSocketSamplerGuiPanel() {
        init();
    }

    private void init() {

        this.setLayout(new BoxLayout(this, Y_AXIS));

        JPanel connectionPanel = new JPanel();
        {
            connectionPanel.setLayout(new BoxLayout(connectionPanel, Y_AXIS));
            connectionPanel.setBorder(BorderFactory.createTitledBorder("Connection"));

            JPanel outerConnectionButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            {
                JPanel innerConnectionButtonPanel = new JPanel();
                {
                    innerConnectionButtonPanel.setLayout(new BoxLayout(innerConnectionButtonPanel, Y_AXIS));
                    reuseConnection = new JRadioButton("use existing connection");
                    reuseConnection.addActionListener(e -> handleConnectionRadio(e));
                    innerConnectionButtonPanel.add(reuseConnection);
                    newConnection = new JRadioButton("setup new connection");
                    newConnection.setSelected(true);
                    newConnection.addActionListener(e -> handleConnectionRadio(e));
                    innerConnectionButtonPanel.add(newConnection);

                    ButtonGroup connectionButtons = new ButtonGroup();
                    connectionButtons.add(newConnection);
                    connectionButtons.add(reuseConnection);
                }
                outerConnectionButtonPanel.add(innerConnectionButtonPanel);
            }
            connectionPanel.add(outerConnectionButtonPanel);

            JPanel urlPanel = createUrlPanel();
            {
                connectionRelatedSettings.add(protocolSelector);
                connectionRelatedSettings.add(serverLabel);
                connectionRelatedSettings.add(serverField);
                connectionRelatedSettings.add(pathLabel);
                connectionRelatedSettings.add(pathField);
                connectionRelatedSettings.add(portLabel);
                connectionRelatedSettings.add(portField);
            }
            connectionPanel.add(urlPanel);

            JPanel connectionTimeoutPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            {
                connectionTimeoutPanel.setBorder(BorderFactory.createEmptyBorder(1, 4, 1, 0));
                JLabel connectionTimeoutLabel = new JLabel("Connection timeout (ms):");
                connectionTimeoutPanel.add(connectionTimeoutLabel);
                connectionTimeoutField = new JTextField();
                connectionTimeoutField.setColumns(10);
                connectionTimeoutPanel.add(connectionTimeoutField);
                JLabel connectionTimeoutErrorLabel = new JLabel();
                connectionTimeoutErrorLabel.setForeground(Color.RED);
                addIntegerRangeCheck(connectionTimeoutField, MIN_CONNECTION_TIMEOUT, MAX_CONNECTION_TIMEOUT, connectionTimeoutErrorLabel);
                connectionTimeoutPanel.add(connectionTimeoutErrorLabel);
                connectionRelatedSettings.add(connectionTimeoutLabel);
                connectionRelatedSettings.add(connectionTimeoutField);
            }
            connectionPanel.add(connectionTimeoutPanel);
        }
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
                    messageField.setBackground(Color.YELLOW);
                    messageField.setForeground(Color.RED);
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
                    readTimeoutErrorField.setForeground(Color.RED);
                    addIntegerRangeCheck(readTimeoutField, MIN_READ_TIMEOUT, MAX_READ_TIMEOUT, readTimeoutErrorField);
                    requestSettingsPanel.add(readTimeoutField);
                    requestSettingsPanel.add(readTimeoutErrorField);
                }
                dataPanel.add(requestSettingsPanel);
            }
            splitter.setTopComponent(dataPanel);
            splitter.setBottomComponent(new JPanel());
            splitter.setBorder(null);
        }
        this.add(splitter);
        splitter.setAlignmentX(JComponent.LEFT_ALIGNMENT);

        this.add(new JPanel());

    }

    private void handleConnectionRadio(ActionEvent e) {
        boolean enabled = e.getSource() == newConnection;
        for (JComponent c: connectionRelatedSettings)
            c.setEnabled(enabled);
    }

    void clearGui() {
        super.clearGui();
        requestDataField.setText("");
        messageField.setText("");
        setCreateNewConnection(true);
    }

    void setCreateNewConnection(boolean yesOrNo) {
        newConnection.setSelected(yesOrNo);
        reuseConnection.setSelected(! yesOrNo);
        handleConnectionRadio(new ActionEvent(yesOrNo? newConnection: reuseConnection, 0, "dummy"));
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
