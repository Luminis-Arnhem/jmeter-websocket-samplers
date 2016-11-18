package eu.luminis.jmeter.wssampler;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.util.regex.Pattern;

import static javax.swing.BoxLayout.X_AXIS;
import static javax.swing.BoxLayout.Y_AXIS;

public class OpenWebSocketSamplerGuiPanel extends WebSocketSamplerGuiPanel {

    JTextField serverField;
    JTextField portField;
    JTextField pathField;
    JTextField connectionTimeoutField;
    JTextField readTimeoutField;
    private JLabel connectionTimeoutErrorLabel;
    private JLabel readTimeoutErrorLabel;

    public OpenWebSocketSamplerGuiPanel() {
        init();
    }

    private void init() {

        JPanel boxPanel = new JPanel();
        {
            boxPanel.setLayout(new BoxLayout(boxPanel, Y_AXIS));

            JPanel urlPanel = new JPanel();
            {
                urlPanel.setLayout(new BoxLayout(urlPanel, X_AXIS));
                urlPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0),
                        BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Server URL"), BorderFactory.createEmptyBorder(3, 5, 5, 0))));

                JLabel serverLabel = new JLabel("Server name or IP:");
                urlPanel.add(serverLabel);
                serverField = new JTextField();
                serverField.setColumns(20);
                urlPanel.add(serverField);
                JLabel portLabel = new JLabel("Port:");
                urlPanel.add(portLabel);
                portField = new JTextField();
                addIntegerRangeCheck(portField, 1, 65535);
                portField.setColumns(5);
                portField.setMaximumSize(portField.getPreferredSize());
                urlPanel.add(portField);
                JLabel pathLabel = new JLabel("Path:");
                urlPanel.add(pathLabel);
                pathField = new JTextField();
                pathField.setColumns(20);
                urlPanel.add(pathField);
            }
            boxPanel.add(urlPanel);

            JPanel connectionTimeoutPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            connectionTimeoutPanel.setBorder(BorderFactory.createEmptyBorder(1, 4, 1, 0));
            {
                JLabel connectionTimeoutLabel = new JLabel("Connection timeout (ms):");
                connectionTimeoutPanel.add(connectionTimeoutLabel);
                connectionTimeoutField = new JTextField();
                connectionTimeoutField.setColumns(10);
                connectionTimeoutPanel.add(connectionTimeoutField);
                connectionTimeoutErrorLabel = new JLabel();
                connectionTimeoutErrorLabel.setForeground(Color.RED);
                addIntegerRangeCheck(connectionTimeoutField, MIN_CONNECTION_TIMEOUT, MAX_CONNECTION_TIMEOUT, connectionTimeoutErrorLabel);
                connectionTimeoutPanel.add(connectionTimeoutErrorLabel);
            }
            boxPanel.add(connectionTimeoutPanel);

            JPanel readTimeoutPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            readTimeoutPanel.setBorder(BorderFactory.createEmptyBorder(1, 4, 1, 0));
            {
                JLabel readTimeoutLabel = new JLabel("Read timeout (ms):");
                readTimeoutPanel.add(readTimeoutLabel);
                readTimeoutField = new JTextField();
                readTimeoutField.setColumns(10);
                readTimeoutPanel.add(readTimeoutField);
                readTimeoutErrorLabel = new JLabel();
                readTimeoutErrorLabel.setForeground(Color.RED);
                addIntegerRangeCheck(readTimeoutField, MIN_READ_TIMEOUT, MAX_READ_TIMEOUT, readTimeoutErrorLabel);
                readTimeoutPanel.add(readTimeoutErrorLabel);
            }
            boxPanel.add(readTimeoutPanel);
        }

        this.setLayout(new BorderLayout());
        add(boxPanel, BorderLayout.NORTH);
    }

    void clearGui() {
        serverField.setText("");
        portField.setText("");
        pathField.setText("");
        connectionTimeoutErrorLabel.setText("");
    }


    public static void main(String[] args) {
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 400);
        frame.getContentPane().add(new OpenWebSocketSamplerGuiPanel());
        frame.setVisible(true);
    }
}
