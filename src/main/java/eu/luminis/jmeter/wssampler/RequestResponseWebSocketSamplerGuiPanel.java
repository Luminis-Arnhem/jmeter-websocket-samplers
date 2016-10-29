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
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
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
    List<JComponent> connectionRelatedSettings = new ArrayList<>();
    JTextField connectionTimeoutField;
    JTextField readTimeoutField;

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
        urlPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(5,0,0,0),
                BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Server URL"), BorderFactory.createEmptyBorder(3,5,5,0))));
        JLabel serverLabel = new JLabel("Server name or IP:");
        urlPanel.add(serverLabel);
        connectionRelatedSettings.add(serverLabel);
        connectionRelatedSettings.add(urlPanel);
        serverField = new JTextField();
        serverField.setColumns(20);
        urlPanel.add(serverField);
        JLabel portLabel = new JLabel("Port:");
        urlPanel.add(portLabel);
        connectionRelatedSettings.add(portLabel);
        connectionRelatedSettings.add(serverField);
        portField = new JTextField();
        addIntegerRangeCheck(portField, 1, 65535);
        portField.setColumns(5);
        portField.setMaximumSize(portField.getPreferredSize());
        connectionRelatedSettings.add(portField);
        urlPanel.add(portField);
        JLabel pathLabel = new JLabel("Path:");
        urlPanel.add(pathLabel);
        connectionRelatedSettings.add(pathLabel);
        pathField = new JTextField();
        pathField.setColumns(20);
        urlPanel.add(pathField);
        connectionRelatedSettings.add(pathField);
        JPanel connectionTimeoutPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        connectionTimeoutPanel.setBorder(BorderFactory.createEmptyBorder(1, 4, 1, 0));
        {
            JLabel connectionTimeoutLabel = new JLabel("Connection timeout (ms):");
            connectionTimeoutPanel.add(connectionTimeoutLabel);
            connectionTimeoutField = new JTextField();
            connectionTimeoutField.setColumns(5);
            connectionTimeoutPanel.add(connectionTimeoutField);
            JLabel connectionTimeoutErrorLabel = new JLabel();
            connectionTimeoutErrorLabel.setForeground(Color.RED);
            addIntegerRangeCheck(connectionTimeoutField, 1, 999999, connectionTimeoutErrorLabel);
            connectionTimeoutPanel.add(connectionTimeoutErrorLabel);
            connectionRelatedSettings.add(connectionTimeoutLabel);
            connectionRelatedSettings.add(connectionTimeoutField);
        }
        connectionPanel.add(connectionTimeoutPanel);

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
                    }
                });
                dataZone.add(requestDataField);
            }
            dataPanel.add(dataZone);

            JPanel requestSettingsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            {
                requestSettingsPanel.add(new JLabel("Response (read) timeout (ms): "));
                readTimeoutField = new JTextField();
                readTimeoutField.setColumns(5);
                JLabel readTimeoutErrorField = new JLabel();
                readTimeoutErrorField.setForeground(Color.RED);
                addIntegerRangeCheck(readTimeoutField, 0, 999999, readTimeoutErrorField);
                requestSettingsPanel.add(readTimeoutField);
                requestSettingsPanel.add(readTimeoutErrorField);
            }
            dataPanel.add(requestSettingsPanel);
        }
        boxPanel.add(dataPanel);

        setLayout(new BorderLayout());
        add(boxPanel, BorderLayout.NORTH);
    }

    private void handleConnectionRadio(ActionEvent e) {
        boolean enabled = e.getSource() == newConnection;
        for (JComponent c: connectionRelatedSettings)
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

    private void addIntegerRangeCheck(final JTextField input, int min, int max) {
        addIntegerRangeCheck(input, min, max, null);
    }

    private void addIntegerRangeCheck(final JTextField input, int min, int max, JLabel errorMsgField) {
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
