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
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import static javax.swing.BoxLayout.X_AXIS;

public class DataPanel extends JPanel {

    public static final String BINARY = "Binary";
    public static final String TEXT = "Text";

    private JTextArea requestDataField;
    private JComboBox typeSelector;
    private JLabel messageField;
    private final JLabel requestDataLabel;
    private final JLabel fileLabel;
    private final JTextField filePathField;
    private final JButton browseButton;
    private final JCheckBox enableFile;

    public DataPanel() {

        setBorder(BorderFactory.createTitledBorder("Data"));
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        JPanel topBar = new JPanel();
        {
            topBar.setLayout(new BoxLayout(topBar, X_AXIS));
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
        topBar.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        add(topBar);

        add(Box.createVerticalStrut(10));
        JPanel dataZone = new JPanel();
        {
            dataZone.setLayout(new BoxLayout(dataZone, X_AXIS));
            dataZone.add(Box.createHorizontalStrut(5));
            requestDataLabel = new JLabel("Request data: ");
            requestDataLabel.setBorder(BorderFactory.createEmptyBorder(3, 0, 0, 0));
            requestDataLabel.setMaximumSize(new Dimension(requestDataLabel.getMaximumSize().width, Integer.MAX_VALUE));
            requestDataLabel.setVerticalAlignment(JLabel.TOP);
            dataZone.add(requestDataLabel);
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
        dataZone.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        add(dataZone);

        add(Box.createVerticalStrut(10));
        JPanel filePathPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        {
            enableFile = new JCheckBox();
            enableFile.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    enableSelectFileOption(enableFile.isSelected());
                }
            });
            filePathPanel.add(enableFile);

            fileLabel = new JLabel("Read request data from file: ");
            filePathPanel.add(fileLabel);
            filePathField = new JTextField();
            filePathField.setColumns(30);
            filePathPanel.add(filePathField);
            browseButton = new JButton("Browse...");
            filePathPanel.add(browseButton);
            browseButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    JFileChooser filePicker = new JFileChooser();
                    int result = filePicker.showDialog(filePathField, "Open");
                    if (result == JFileChooser.APPROVE_OPTION) {
                        filePathField.setText(filePicker.getSelectedFile().getAbsolutePath());
                    }
                }
            });

            enableSelectFileOption(false);
        }
        filePathPanel.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        add(filePathPanel);
    }

    void enableSelectFileOption(boolean enable) {
        requestDataLabel.setEnabled(!enable);
        requestDataField.setEnabled(!enable);
        fileLabel.setEnabled(enable);
        filePathField.setEnabled(enable);
        browseButton.setEnabled(enable);

    }

    void clearGui() {
        requestDataField.setText("");
        messageField.setText("");
        enableFile.setSelected(false);
    }

    public String getRequestData() {
        return requestDataField.getText();
    }

    public void setRequestData(String requestData) {
        requestDataField.setText(requestData);
    }

    public DataPayloadType getType() {
        if (typeSelector.getSelectedItem().equals(BINARY))
            return DataPayloadType.Binary;
        else if (typeSelector.getSelectedItem().equals(TEXT))
            return DataPayloadType.Text;
        else
            throw new RuntimeException("Unknown type");
    }

    public void setType(DataPayloadType type) {
        switch (type) {
            case Binary:
                typeSelector.setSelectedItem(BINARY);
                break;
            case Text:
                typeSelector.setSelectedItem(TEXT);
                break;
            default:
                throw new RuntimeException("Unknown type");
        }
    }

    private void checkBinary() {
        if (typeSelector.getSelectedItem() == BINARY) {
            try {
                BinaryUtils.parseBinaryString(JMeterUtils.stripJMeterVariables(requestDataField.getText()));
                messageField.setText("");
            } catch (NumberFormatException notNumber) {
                messageField.setText("Error: request data is not in binary format; use format like '0xca 0xfe' or 'ba be' (JMeter variables like ${var} are allowed).");
            }
        } else {
            messageField.setText("");
        }
    }

}
