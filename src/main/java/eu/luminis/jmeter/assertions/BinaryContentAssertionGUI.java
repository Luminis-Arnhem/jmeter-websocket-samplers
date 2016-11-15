package eu.luminis.jmeter.assertions;

import org.apache.jmeter.assertions.gui.AbstractAssertionGui;
import org.apache.jmeter.testelement.TestElement;

import java.awt.BorderLayout;

import static eu.luminis.jmeter.assertions.BinaryContentAssertion.ComparisonType.*;

public class BinaryContentAssertionGUI extends AbstractAssertionGui {

    private BinaryContentAssertionGuiPanel settingsPanel;

    public BinaryContentAssertionGUI() {
        init();
    }

    @Override
    public void clearGui() {
        super.clearGui();
        settingsPanel.clearGui();
    }

    @Override
    public String getStaticLabel() {
        return "Binary Response Assertion";
    }

    @Override
    public String getLabelResource() {
        return null;
    }

    private void init() {
        setLayout(new BorderLayout(0, 5));
        setBorder(makeBorder());
        add(makeTitlePanel(), BorderLayout.NORTH);
        settingsPanel = new BinaryContentAssertionGuiPanel();
        add(settingsPanel, BorderLayout.CENTER);
    }

    @Override
    public TestElement createTestElement() {
        BinaryContentAssertion element = new BinaryContentAssertion();
        modifyTestElement(element);
        return element;

    }

    @Override
    public void modifyTestElement(TestElement el) {
        configureTestElement(el);
        if (el instanceof BinaryContentAssertion) {
            BinaryContentAssertion ra = (BinaryContentAssertion) el;
            ra.setComparisonValue(settingsPanel.binaryContent.getText());
            if (settingsPanel.containsButton.isSelected() && settingsPanel.doesButton.isSelected())
                ra.setComparisonType(Contains);
            else if (settingsPanel.containsButton.isSelected() && !settingsPanel.doesButton.isSelected())
                ra.setComparisonType(NotContains);
            else if (settingsPanel.equalsButton.isSelected() && settingsPanel.doesButton.isSelected())
                ra.setComparisonType(Equals);
            else if (settingsPanel.equalsButton.isSelected() && !settingsPanel.doesButton.isSelected())
                ra.setComparisonType(NotEquals);
            else if (settingsPanel.startsWithButton.isSelected() && settingsPanel.doesButton.isSelected())
                ra.setComparisonType(StartsWith);
            else if (settingsPanel.startsWithButton.isSelected() && !settingsPanel.doesButton.isSelected())
                ra.setComparisonType(NotStartsWith);
            else
                throw new RuntimeException("Program error");
        }
    }

    @Override
    public void configure(TestElement element) {
        super.configure(element);
        BinaryContentAssertion assertion = (BinaryContentAssertion) element;
        settingsPanel.binaryContent.setText(assertion.getComparisonValue());
        settingsPanel.setDoes(assertion.getComparisonType() == Contains || assertion.getComparisonType() == Equals || assertion.getComparisonType() == StartsWith);
        if (assertion.getComparisonType() == Contains || assertion.getComparisonType() == NotContains)
            settingsPanel.setContains();
        else if (assertion.getComparisonType() == Equals || assertion.getComparisonType() == NotEquals)
            settingsPanel.setEquals();
        else if (assertion.getComparisonType() == StartsWith || assertion.getComparisonType() == NotStartsWith)
            settingsPanel.setStartsWith();
        else
            throw new RuntimeException("Program error");
    }

}
