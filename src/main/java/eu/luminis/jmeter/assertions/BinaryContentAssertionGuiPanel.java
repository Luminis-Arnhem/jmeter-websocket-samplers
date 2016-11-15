package eu.luminis.jmeter.assertions;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.border.TitledBorder;
import java.awt.BorderLayout;

public class BinaryContentAssertionGuiPanel extends JPanel {

    JTextArea binaryContent;
    JRadioButton containsButton;
    JRadioButton doesButton;
    JRadioButton doesNotButton;
    JRadioButton equalsButton;
    JRadioButton startsWithButton;

    public BinaryContentAssertionGuiPanel() {
        init();
    }

    private void init() {
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        JPanel settingsPanel = new JPanel();
        {
            settingsPanel.setLayout(new BoxLayout(settingsPanel, BoxLayout.X_AXIS));
            settingsPanel.add(Box.createHorizontalStrut(5));
            settingsPanel.add(new JLabel("Assert response "));

            JPanel negationPanel = new JPanel();
            {
                negationPanel.setLayout(new BoxLayout(negationPanel, BoxLayout.Y_AXIS));
                negationPanel.add(Box.createVerticalGlue());
                doesButton = new JRadioButton("does");
                negationPanel.add(doesButton);
                doesNotButton = new JRadioButton("does NOT");
                negationPanel.add(doesNotButton);
                ButtonGroup negationGroup = new ButtonGroup();
                doesButton.setSelected(true);
                negationGroup.add(doesButton);
                negationGroup.add(doesNotButton);
                negationPanel.add(Box.createVerticalGlue());
                negationPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(5, 5, 0, 0), BorderFactory.createEtchedBorder()));
            }
            settingsPanel.add(negationPanel);

            JPanel matchPanel = new JPanel();
            {
                matchPanel.setLayout(new BoxLayout(matchPanel, BoxLayout.Y_AXIS));
                containsButton = new JRadioButton("contain");
                matchPanel.add(containsButton);
                startsWithButton = new JRadioButton("start with");
                matchPanel.add(startsWithButton);
                equalsButton = new JRadioButton("equal");
                matchPanel.add(equalsButton);
                containsButton.setSelected(true);
                ButtonGroup matchGroup = new ButtonGroup();
                matchGroup.add(containsButton);
                matchGroup.add(equalsButton);
                matchGroup.add(startsWithButton);
                matchPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(5, 5, 0, 5), BorderFactory.createEtchedBorder()));
            }
            settingsPanel.add(matchPanel);

            settingsPanel.add(new JLabel("the following binary data:"));
        }
        settingsPanel.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        add(settingsPanel);

        JSplitPane splitter = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        {
            JPanel borderPanel = new JPanel();
            {
                borderPanel.setBorder(BorderFactory.createTitledBorder(null, "Binary data", TitledBorder.LEFT, TitledBorder.DEFAULT_POSITION));
                borderPanel.setLayout(new BorderLayout());
                binaryContent = new JTextArea();
                binaryContent.setRows(5);
                borderPanel.add(binaryContent);
            }

            splitter.setBorder(null);
            splitter.setTopComponent(borderPanel);
            splitter.setBottomComponent(new JPanel());
        }

        splitter.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        add(splitter);
    }

    void clearGui() {
        binaryContent.setText("");
        // Default is: "does contain"
        setDoes(true);
        setContains();
    }
    public void setDoes(boolean value) {
        doesButton.setSelected(value);
        doesNotButton.setSelected(!value);
    }

    public void setContains() {
        containsButton.setSelected(true);
        equalsButton.setSelected(false);
        startsWithButton.setSelected(false);
    }

    public void setEquals() {
        containsButton.setSelected(false);
        equalsButton.setSelected(true);
        startsWithButton.setSelected(false);
    }

    public void setStartsWith() {
        containsButton.setSelected(false);
        equalsButton.setSelected(false);
        startsWithButton.setSelected(true);
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 400);
        frame.getContentPane().add(new BinaryContentAssertionGuiPanel());
        frame.setVisible(true);
    }

}
