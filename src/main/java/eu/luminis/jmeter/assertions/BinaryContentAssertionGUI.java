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
