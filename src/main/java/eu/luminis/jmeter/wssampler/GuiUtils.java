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

import javax.swing.*;
import java.awt.*;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

public class GuiUtils {

    private static final Logger log = LoggingManager.getLoggerForClass();

    /** Color used for error messages and to signal invalid input */
    private static final Color DEFAULT_ERROR_COLOR = Color.RED;
    /** Color used for "OK" icon */
    private static final Color DEFAULT_OK_ICON_COLOR = Color.GREEN.darker();
    /** Color used for "ERROR" icon */
    private static final Color DEFAULT_ERROR_ICON_COLOR = Color.RED;
    /** Color for disabled elements when UIDefaults color is undefined */
    private static Color DEFAULT_DISABLED_COLOR = Color.GRAY;
    /** Color for foreground when UIDefaults color is undefined */
    private static Color DEFAULT_FOREGROUND_COLOR = Color.BLACK;

    public static Color getLookAndFeelColor(String colorKey) {
        Color color;
        if (colorKey.equals("TextField.errorForeground")) {
            color = DEFAULT_ERROR_COLOR;
            LookAndFeel lookAndFeel = UIManager.getLookAndFeel();
            if (lookAndFeel != null && lookAndFeel.getName().contains("Darcula"))
                color = color.darker();
        }
        else if (colorKey.equals("Icon.okForeground"))
            color = DEFAULT_OK_ICON_COLOR;
        else if (colorKey.equals("Icon.errorForeground"))
            color = DEFAULT_ERROR_ICON_COLOR;
        else {
            color = UIManager.getDefaults().getColor(colorKey);
            if (color == null) {
                log.error("UIManager does not support color key '" + colorKey + "'");
                if (colorKey.contains("foreground"))
                    color = DEFAULT_FOREGROUND_COLOR;
                else if (colorKey.contains("title"))
                    color = DEFAULT_FOREGROUND_COLOR;
                else if (colorKey.contains("disabled"))
                    color = DEFAULT_DISABLED_COLOR;
                else {
                    log.error("Undefined color key '" + colorKey + "'");
                    color = Color.RED;
                }
            }
        }
        return color;
    }

}
