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
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static javax.swing.BoxLayout.X_AXIS;
import static javax.swing.BoxLayout.Y_AXIS;

public class RequestResponseWebSocketSamplerGuiPanel extends JPanel {

    public static final String BINARY = "Binary";
    public static final String TEXT = "Text";

    public static final Pattern DETECT_JMETER_VAR_REGEX = Pattern.compile("\\$\\{\\w+\\}");

    JTextField serverField;
    JTextField portField;
    JTextField requestDataField;
    JTextField pathField;
    JComboBox typeSelector;
    private JLabel messageField;
    JRadioButton reuseConnection;
    JRadioButton newConnection;
    List<JComponent> serverUrlParts = new ArrayList<>();

    public RequestResponseWebSocketSamplerGuiPanel() {
        init();
    }

    private void init() {

        JPanel boxPanel = new JPanel();
        boxPanel.setLayout(new BoxLayout(boxPanel, Y_AXIS));

        JPanel connectionPanel = new JPanel();
        connectionPanel.setLayout(new BoxLayout(connectionPanel, Y_AXIS));
        connectionPanel.setBorder(BorderFactory.createTitledBorder("Connection"));
        JPanel innerConnectionButtonPanel = new JPanel();
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
        JPanel outerConnectionButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        outerConnectionButtonPanel.add(innerConnectionButtonPanel);
        connectionPanel.add(outerConnectionButtonPanel);
        boxPanel.add(connectionPanel);
        JPanel urlPanel = new JPanel();
        urlPanel.setLayout(new BoxLayout(urlPanel, X_AXIS));
        connectionPanel.add(urlPanel);
        urlPanel.setBorder(BorderFactory.createTitledBorder("Server"));
        JLabel serverLabel = new JLabel("Server name or IP:");
        urlPanel.add(serverLabel);
        serverUrlParts.add(serverLabel);
        serverUrlParts.add(urlPanel);
        serverField = new JTextField();
        serverField.setColumns(20);
        urlPanel.add(serverField);
        JLabel portLabel = new JLabel("Port:");
        urlPanel.add(portLabel);
        serverUrlParts.add(portLabel);
        serverUrlParts.add(serverField);
        portField = new JTextField();
        portField.setColumns(5);
        portField.setMaximumSize(portField.getPreferredSize());
        serverUrlParts.add(portField);
        urlPanel.add(portField);
        JLabel pathLabel = new JLabel("Path:");
        urlPanel.add(pathLabel);
        serverUrlParts.add(pathLabel);
        pathField = new JTextField();
        pathField.setColumns(20);
        urlPanel.add(pathField);
        serverUrlParts.add(pathField);

        JPanel dataPanel = new JPanel();
        dataPanel.setBorder(BorderFactory.createTitledBorder("Data"));
        dataPanel.setLayout(new BoxLayout(dataPanel, BoxLayout.Y_AXIS));
        JPanel topBar = new JPanel();
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

    private void handleConnectionRadio(ActionEvent e) {
        boolean enabled = e.getSource() == newConnection;
        for (JComponent c: serverUrlParts)
            c.setEnabled(enabled);
    }

    void clearGui() {
        serverField.setText("");
        portField.setText("");
        requestDataField.setText("");
        pathField.setText("");
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

    private String stripJMeterVariables(String data) {
        return DETECT_JMETER_VAR_REGEX.matcher(data).replaceAll("");
    }


    public static void main(String[] args) {
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 400);
        frame.getContentPane().add(new RequestResponseWebSocketSamplerGuiPanel());
        frame.setVisible(true);
    }
}
