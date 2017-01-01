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
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.regex.Pattern;

import static javax.swing.BoxLayout.X_AXIS;

abstract public class WebSocketSamplerGuiPanel extends JPanel {

    public static final Pattern DETECT_JMETER_VAR_REGEX = Pattern.compile("\\$\\{\\w+\\}");

    public static final int MIN_CONNECTION_TIMEOUT = WebsocketSampler.MIN_CONNECTION_TIMEOUT;
    public static final int MAX_CONNECTION_TIMEOUT = WebsocketSampler.MAX_CONNECTION_TIMEOUT;
    public static final int MIN_READ_TIMEOUT = WebsocketSampler.MIN_READ_TIMEOUT;
    public static final int MAX_READ_TIMEOUT = WebsocketSampler.MAX_READ_TIMEOUT;

    protected JComboBox<String> protocolSelector;
    protected JTextField serverField;
    protected JTextField portField;
    protected JTextField pathField;
    protected JTextField connectionTimeoutField;
    protected JTextField readTimeoutField;
    protected JLabel portLabel;
    protected JLabel pathLabel;
    protected JLabel serverLabel;

    void clearGui() {
        protocolSelector.setSelectedItem("ws");
        serverField.setText("");
        portField.setText("");
        pathField.setText("");
    }

    protected JPanel createUrlPanel() {
        JPanel urlPanel = new JPanel();
        urlPanel.setLayout(new BoxLayout(urlPanel, X_AXIS));
        urlPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0),
                BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Server URL"), BorderFactory.createEmptyBorder(3, 5, 5, 0))));

        protocolSelector = new JComboBox<String>(new String[]{"ws", "wss"});
        urlPanel.add(protocolSelector);
        urlPanel.add(Box.createHorizontalStrut(10));
        serverLabel = new JLabel("Server name or IP:");
        urlPanel.add(serverLabel);
        serverField = new JTextField();
        serverField.setColumns(20);
        serverField.setMaximumSize(new Dimension(Integer.MAX_VALUE, serverField.getMinimumSize().height));
        urlPanel.add(serverField);
        portLabel = new JLabel("Port:");
        urlPanel.add(portLabel);
        portField = new JTextField();
        addIntegerRangeCheck(portField, 1, 65535);
        portField.setColumns(5);
        portField.setMaximumSize(portField.getPreferredSize());
        urlPanel.add(portField);
        pathLabel = new JLabel("Path:");
        urlPanel.add(pathLabel);
        pathField = new JTextField();
        pathField.setColumns(20);
        pathField.setMaximumSize(new Dimension(Integer.MAX_VALUE, pathField.getMinimumSize().height));
        urlPanel.add(pathField);

        return urlPanel;
    }

    protected void addIntegerRangeCheck(final JTextField input, int min, int max) {
        addIntegerRangeCheck(input, min, max, null);
    }

    protected void addIntegerRangeCheck(final JTextField input, int min, int max, JLabel errorMsgField) {
        input.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                checkIntegerInRange(e.getDocument(), min, max, input, errorMsgField);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                checkIntegerInRange(e.getDocument(), min, max, input, errorMsgField);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                checkIntegerInRange(e.getDocument(), min, max, input, errorMsgField);
            }
        });
    }

    private boolean checkIntegerInRange(Document doc, int min, int max, JTextField field, JLabel errorMsgField) {
        boolean ok = false;
        boolean isNumber = false;

        try {
            String literalContent = stripJMeterVariables(doc.getText(0, doc.getLength()));
            if (literalContent.trim().length() > 0) {
                int value = Integer.parseInt(literalContent);
                ok = value >= min && value <= max;
                isNumber = true;
            } else {
                // Could be just a JMeter variable (e.g. ${port}), which should not be refused!
                ok = true;
            }
        }
        catch (NumberFormatException nfe) {
        }
        catch (BadLocationException e) {
            // Impossible
        }
        if (field != null)
            if (ok) {
                field.setForeground(Color.BLACK);
                if (errorMsgField != null)
                    errorMsgField.setText("");
            }
            else {
                field.setForeground(Color.RED);
                if (isNumber && errorMsgField != null)
                    errorMsgField.setText("Value must >= " + min + " and <= " + max);
            }
        return ok;
    }

    protected String stripJMeterVariables(String data) {
        return WebSocketSamplerGuiPanel.DETECT_JMETER_VAR_REGEX.matcher(data).replaceAll("");
    }

    boolean getTLS() {
        return "wss".equals(protocolSelector.getSelectedItem());
    }

    void setTLS(boolean tls) {
        if (tls)
            protocolSelector.setSelectedItem("wss");
        else
            protocolSelector.setSelectedItem("ws");
    }

    static JPanel createAboutPanel(JComponent parent) {
        JPanel aboutPanel = new JPanel();
        aboutPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        JLabel aboutLabel = new JLabel("<html>WebSocket Samplers plugin. <a href=\"#\">Check</a> for updates.</html>");
        aboutLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                AboutDialog.showDialog(SwingUtilities.getWindowAncestor(parent));
            }
        });
        aboutLabel.setFont(parent.getFont().deriveFont(Font.PLAIN, 10));
        aboutLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        aboutPanel.add(aboutLabel);
        return aboutPanel;
    }

}
