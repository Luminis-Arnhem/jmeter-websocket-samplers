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

import org.apache.jmeter.config.gui.AbstractConfigGui;
import org.apache.jmeter.testelement.TestElement;

import java.awt.*;

public class TextFrameFilterGui extends AbstractConfigGui {

    private TextFrameFilterGuiPanel settingsPanel;

    public TextFrameFilterGui() {
        setLayout(new BorderLayout(0, 5));
        setBorder(makeBorder());
        add(makeTitlePanel(), BorderLayout.NORTH);
        settingsPanel = new TextFrameFilterGuiPanel();
        add(settingsPanel, BorderLayout.CENTER);
    }

    @Override
    public String getStaticLabel() {
        return "WebSocket Text Frame Filter";
    }

    @Override
    public String getLabelResource() {
        return null;
    }

    @Override
    public TestElement createTestElement() {
        FrameFilter frameFilterElement = new TextFrameFilter();
        configureTestElement(frameFilterElement);  // Essential because it sets some basic JMeter properties (e.g. the link between sampler and gui class)
        return frameFilterElement;
    }

    @Override
    public void configure(TestElement element) {
        super.configure(element);
        if (element instanceof TextFrameFilter) {
            TextFrameFilter filter = (TextFrameFilter) element;
            settingsPanel.setComparisonType(filter.getComparisonType());
            settingsPanel.matchValue.setText(filter.getMatchValue());
        }

    }

    @Override
    public void modifyTestElement(TestElement element) {
        configureTestElement(element);
        if (element instanceof TextFrameFilter) {
            TextFrameFilter filter = (TextFrameFilter) element;
            filter.setComparisonType(settingsPanel.getComparisonType());
            filter.setMatchValue(settingsPanel.matchValue.getText());
        }
    }

}
