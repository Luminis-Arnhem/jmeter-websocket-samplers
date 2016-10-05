package eu.luminis.jmeter.wssampler;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;

import static javax.swing.BoxLayout.X_AXIS;
import static javax.swing.BoxLayout.Y_AXIS;

public class RequestResponseWebSocketSamplerGuiPanel extends JPanel {

    public static final String BINARY = "Binary";
    public static final String TEXT = "TEXT";

    JTextField serverField;
    JTextField portField;
    JTextField requestDataField;
    JTextField pathField;
    JComboBox typeSelector;
    private JLabel messageField;

    public RequestResponseWebSocketSamplerGuiPanel() {
        init();
    }

    private void init() {

        JPanel boxPanel = new JPanel();
        boxPanel.setLayout(new BoxLayout(boxPanel, Y_AXIS));

        JPanel urlPanel = new JPanel();
        urlPanel.setLayout(new BoxLayout(urlPanel, X_AXIS));
        boxPanel.add(urlPanel);
        urlPanel.setBorder(BorderFactory.createTitledBorder("Server"));
        urlPanel.add(new JLabel("Server name or IP:"));
        serverField = new JTextField();
        serverField.setColumns(20);
        urlPanel.add(serverField);
        urlPanel.add(new JLabel("Port:"));
        portField = new JTextField();
        portField.setColumns(5);
        portField.setMaximumSize(portField.getPreferredSize());
        urlPanel.add(portField);
        urlPanel.add(new JLabel("Path:"));
        pathField = new JTextField();
        pathField.setColumns(20);
        urlPanel.add(pathField);

        JPanel dataPanel = new JPanel();
        dataPanel.setBorder(BorderFactory.createTitledBorder("Data"));
        dataPanel.setLayout(new BoxLayout(dataPanel, BoxLayout.Y_AXIS));
        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        String[] typeOptions = {TEXT, BINARY};
        typeSelector = new JComboBox(typeOptions);
        typeSelector.addActionListener(e -> {
            checkBinary();
        });
        topBar.add(typeSelector);
        messageField = new JLabel();
        messageField.setForeground(Color.RED);
        topBar.add(messageField);
        dataPanel.add(topBar);
        JPanel dataZone = new JPanel();
        dataZone.setLayout(new BoxLayout(dataZone, X_AXIS));
        dataZone.add(new JLabel("Request data: "));
        requestDataField = new JTextField();
        requestDataField.setColumns(40);
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
            }});
        dataZone.add(requestDataField);
        dataPanel.add(dataZone);
        boxPanel.add(dataPanel);

        setLayout(new BorderLayout());
        add(boxPanel, BorderLayout.NORTH);
    }

    void clearGui() {
        // TODO: clear all input fields
    }

    private void checkBinary() {
        if (typeSelector.getSelectedItem() == BINARY) {
            try {
                BinaryUtils.parseBinaryString(requestDataField.getText());
                messageField.setText("");
            }
            catch (NumberFormatException notNumber) {
                messageField.setText("Error: request data is not in binary format; use format like '0xca 0xfe' or 'ba be'.");
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
