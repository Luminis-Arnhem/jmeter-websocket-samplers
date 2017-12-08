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
        System.out.println("AdvancedOptions is removed (hope it will be the only one ;-), advanced options will be disabled");
        WebsocketSampler.multipleConnectionsEnabled = false;
        deleted = true;
    }

    public boolean getMultipleConnectionsEnabled() {
        return getPropertyAsBoolean("enableMultipleConnectionsPerThread", false);
    }

    public void setMultipleConnectionsEnabled(boolean multipleConnectionsEnabled) {
        if (! deleted && enabled) {
            setProperty(new BooleanProperty("enableMultipleConnectionsPerThread", multipleConnectionsEnabled));
            WebsocketSampler.multipleConnectionsEnabled = multipleConnectionsEnabled;
        }
        else {
            System.out.println("Cowardly refusing to set property on deleted or disabled element");
        }
    }
}
