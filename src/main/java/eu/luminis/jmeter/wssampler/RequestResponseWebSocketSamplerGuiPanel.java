package eu.luminis.jmeter.wssampler;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.FlowLayout;

import static javax.swing.BoxLayout.*;

public class RequestResponseWebSocketSamplerGuiPanel extends JPanel {

    JTextField serverField;
    JTextField portField;
    JTextField requestDataField;
    JTextField pathField;

    public RequestResponseWebSocketSamplerGuiPanel() {
        init();
    }

    private void init() {
        setLayout(new BoxLayout(this, Y_AXIS));

        JPanel urlPanel = new JPanel();
        urlPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
        add(urlPanel);
        urlPanel.setBorder(BorderFactory.createTitledBorder("Server"));
        urlPanel.add(new JLabel("Server name or IP:"));
        serverField = new JTextField();
        serverField.setColumns(20);
        urlPanel.add(serverField);
        urlPanel.add(new JLabel("Port:"));
        portField = new JTextField();
        portField.setColumns(3);
        urlPanel.add(portField);
        urlPanel.add(new JLabel("Path:"));
        pathField = new JTextField();
        pathField.setColumns(20);
        urlPanel.add(pathField);

        JPanel dataPanel = new JPanel();
        dataPanel.setBorder(BorderFactory.createTitledBorder("Data"));
        dataPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
        dataPanel.add(new JLabel("request data: "));
        requestDataField = new JTextField();
        requestDataField.setColumns(40);
        dataPanel.add(requestDataField);
        add(dataPanel);
    }

    void clearGui() {}

    public static void main(String[] args) {
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 400);
        frame.getContentPane().add(new RequestResponseWebSocketSamplerGuiPanel());
        frame.setVisible(true);
    }
}
