package eu.luminis.jmeter.wssampler;

import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import java.awt.*;

public class DynamicTitledBorder extends TitledBorder {

    public DynamicTitledBorder(String title) {
        super(title);
    }

    public DynamicTitledBorder(Border border, String title, int left, int defaultPosition) {
        super(border, title, left, defaultPosition);
    }

    public void setEnabled(boolean enabled) {
        setTitleColor(enabled? Color.BLACK: Color.GRAY);
    }
}
