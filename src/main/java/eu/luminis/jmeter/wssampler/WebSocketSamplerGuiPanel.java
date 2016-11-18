package eu.luminis.jmeter.wssampler;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import java.awt.Color;
import java.util.regex.Pattern;

abstract public class WebSocketSamplerGuiPanel extends JPanel {

    public static final Pattern DETECT_JMETER_VAR_REGEX = Pattern.compile("\\$\\{\\w+\\}");

    public static final int MIN_CONNECTION_TIMEOUT = WebsocketSampler.MIN_CONNECTION_TIMEOUT;
    public static final int MAX_CONNECTION_TIMEOUT = WebsocketSampler.MAX_CONNECTION_TIMEOUT;
    public static final int MIN_READ_TIMEOUT = WebsocketSampler.MIN_READ_TIMEOUT;
    public static final int MAX_READ_TIMEOUT = WebsocketSampler.MAX_READ_TIMEOUT;


    protected void addIntegerRangeCheck(final JTextField input, int min, int max) {
        addIntegerRangeCheck(input, min, max, null);
    }

    protected void addIntegerRangeCheck(final JTextField input, int min, int max, JLabel errorMsgField) {
        input.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                checkIntegerInRange(e.getDocument(), min, max, input, errorMsgField);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                checkIntegerInRange(e.getDocument(), min, max, input, errorMsgField);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                checkIntegerInRange(e.getDocument(), min, max, input, errorMsgField);
            }
        });
    }

    private boolean checkIntegerInRange(Document doc, int min, int max, JTextField field, JLabel errorMsgField) {
        boolean ok = false;
        boolean isNumber = false;

        try {
            String literalContent = stripJMeterVariables(doc.getText(0, doc.getLength()));
            if (literalContent.trim().length() > 0) {
                int value = Integer.parseInt(literalContent);
                ok = value >= min && value <= max;
                isNumber = true;
            } else {
                // Could be just a JMeter variable (e.g. ${port}), which should not be refused!
                ok = true;
            }
        }
        catch (NumberFormatException nfe) {
        }
        catch (BadLocationException e) {
            // Impossible
        }
        if (field != null)
            if (ok) {
                field.setForeground(Color.BLACK);
                if (errorMsgField != null)
                    errorMsgField.setText("");
            }
            else {
                field.setForeground(Color.RED);
                if (isNumber && errorMsgField != null)
                    errorMsgField.setText("Value must >= " + min + " and <= " + max);
            }
        return ok;
    }

    protected String stripJMeterVariables(String data) {
        return WebSocketSamplerGuiPanel.DETECT_JMETER_VAR_REGEX.matcher(data).replaceAll("");
    }
}
