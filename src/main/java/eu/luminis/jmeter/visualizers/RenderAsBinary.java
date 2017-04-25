/*
 * Copyright 2016, 2017 Peter Doornbosch
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
import org.apache.jmeter.visualizers.ResultRenderer;
import org.apache.jmeter.visualizers.SamplerResultTab;

public class RenderAsBinary extends SamplerResultTab implements ResultRenderer {

    public RenderAsBinary() {
    }

    @Override
    public void renderResult(SampleResult sampleResult) {
        results.setContentType("text/plain");
        results.setText(BinaryUtils.formatBinary(sampleResult.getResponseData()));
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
