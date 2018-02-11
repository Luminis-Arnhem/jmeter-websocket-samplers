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
package eu.luminis.jmeter.visualizers;

import eu.luminis.jmeter.wssampler.BinaryUtils;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.visualizers.ResultRenderer;
import org.apache.jmeter.visualizers.SamplerResultTab;

import java.awt.Font;

public class RenderAsBinary extends SamplerResultTab implements ResultRenderer {

    private int maxDisplaySize;

    public RenderAsBinary() {
        maxDisplaySize = JMeterUtils.getPropDefault("view.results.tree.max_binary_size", -1);
        if (maxDisplaySize == -1)
            maxDisplaySize = 1024 * 1024;  // 1 MB
        else if (maxDisplaySize < 64)
            maxDisplaySize = 64;
    }

    @Override
    public void renderResult(SampleResult sampleResult) {
        Font oldFont = results.getFont();
        results.setFont(new Font(Font.MONOSPACED, Font.PLAIN, oldFont.getSize()));
        results.setContentType("text/plain");
        byte[] responseData = sampleResult.getResponseData();
        if (responseData.length > maxDisplaySize) {
            results.setText("Binary response is too large to display; showing first " + maxDisplaySize + " bytes.\n" +
                    BinaryUtils.formatBinaryInTable(responseData, maxDisplaySize, 16, true, true));
        }
        else
            results.setText(BinaryUtils.formatBinaryInTable(responseData,16, true, true));

        results.setCaretPosition(0);
        resultsScrollPane.setViewportView(results);
    }

    @Override
    public void renderImage(SampleResult sampleResult) {
        renderResult(sampleResult);
    }

    @Override
    public String toString() {
        return "Binary";
    }

}
