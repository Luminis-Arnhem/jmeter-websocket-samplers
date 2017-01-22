package eu.luminis.jmeter.wssampler;

import javax.swing.JLabel;

/**
 * JLabel extension that hides the label when disabeld.
 */
public class JErrorMessageLabel extends JLabel {

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        setVisible(enabled);
    }
}
