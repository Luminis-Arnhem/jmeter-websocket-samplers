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
package eu.luminis.jmeter.wssampler;

import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.testelement.property.BooleanProperty;

public class AdvancedOptionsElement extends ConfigTestElement {

    private boolean deleted = false;
    private boolean enabled = true;

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        this.enabled = enabled;
        if (!enabled)
            WebsocketSampler.multipleConnectionsEnabled = false;
        // When enabled, the value of the GUI element must be used, JMeter will call modifyTestEl on the GUI element which will take care of it.
    }

    @Override
    public void removed() {
        WebsocketSampler.multipleConnectionsEnabled = false;
        deleted = true;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public boolean getMultipleConnectionsEnabled() {
        return getPropertyAsBoolean("enableMultipleConnectionsPerThread", false);
    }

    public void setMultipleConnectionsEnabled(boolean multipleConnectionsEnabled) {
        if (! deleted && enabled) {
            setProperty(new BooleanProperty("enableMultipleConnectionsPerThread", multipleConnectionsEnabled));
            WebsocketSampler.multipleConnectionsEnabled = multipleConnectionsEnabled;
        }
    }
}
