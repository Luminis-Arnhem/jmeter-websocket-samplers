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

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AboutDialog extends JDialog {

    private static final String DOWNLOAD_API_URL = "https://api.bitbucket.org/2.0/repositories/pjtr/jmeter-websocket-samplers/downloads";
    private static final String DOWNLOAD_URL = "https://bitbucket.org/pjtr/jmeter-websocket-samplers/downloads";
    private static final String ISSUES_URL = "https://bitbucket.org/pjtr/jmeter-websocket-samplers/issues";
    private static final String DOC_URL = "https://bitbucket.org/pjtr/jmeter-websocket-samplers/src/master/README.md";

    private static AboutDialog aboutDlg;

    private SwingWorker<String, Object> worker;
    private JLabel newVersionLabel;

    public AboutDialog(Window parent) {
        super(parent, "About WebSocket Samplers");
        setResizable(false);

        final CardLayout cardLayoutMgr = new CardLayout();
        final JPanel cards = new JPanel();
        JProgressBar progress = new JProgressBar();

        JPanel panel = new JPanel();
        {
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            panel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createEmptyBorder(10, 10, 10, 10),
                    BorderFactory.createCompoundBorder(
                            BorderFactory.createTitledBorder(""),
                            BorderFactory.createEmptyBorder(10, 10, 5, 10))));


            JLabel versionMessage = new JLabel("WebSocket Samplers plugin, version " + (getVersion() != null ? getVersion() : "unknown") + ".");
            versionMessage.setAlignmentX(LEFT_ALIGNMENT);
            panel.add(versionMessage);
            JLabel docsLabel = new JLabel("<html>For documentation, see the <a href=\"" + DOC_URL + "\">" + "README" + "</a>.</html>");
            makeClickGoto(docsLabel, DOC_URL);
            panel.add(docsLabel);

            // "cards" has to be final, so cannot assign new JPanel here.
            {
                cards.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
                cards.setLayout(cardLayoutMgr);

                JPanel card1 = new JPanel();
                {
                    card1.setLayout(new BoxLayout(card1, BoxLayout.Y_AXIS));
                    JLabel updateLabel = new JLabel("Checking for updates...");
                    updateLabel.setAlignmentX(LEFT_ALIGNMENT);
                    card1.add(updateLabel);

                    card1.add(Box.createVerticalStrut(5));

                    progress.setIndeterminate(true);
                    progress.setAlignmentX(LEFT_ALIGNMENT);
                    card1.add(progress);
                }
                cards.add(card1);

                JPanel card2 = new JPanel();
                {
                    card2.setLayout(new BoxLayout(card2, BoxLayout.Y_AXIS));
                    newVersionLabel = new JLabel("There is a newer version available ().");
                    newVersionLabel.setAlignmentX(LEFT_ALIGNMENT);
                    card2.add(newVersionLabel);
                    card2.add(Box.createVerticalStrut(5));

                    card2.add(new JLabel("Download it from:"));
                    JLabel downloadLabel = new JLabel("<html> <a href=\"" + DOWNLOAD_URL + "\">" + DOWNLOAD_URL + "</a>.</html>");
                    makeClickGoto(downloadLabel, DOWNLOAD_URL);
                    card2.add(downloadLabel);
                }
                cards.add(card2);

                cards.add(new JLabel("An error occurred; could not determine whether there are any updates."));

                JPanel card4 = new JPanel();
                {
                    card4.setLayout(new BoxLayout(card4, BoxLayout.Y_AXIS));
                    card4.add(new JLabel("No update available; you have the latest version."));
                    card4.add(Box.createVerticalStrut(10));
                    card4.add(new JLabel("If you encountered an issue or ambiguity, please file an issue at"));
                    JLabel issuesUrlLabel = new JLabel("<html><a href=\"" + ISSUES_URL + "\">" + ISSUES_URL + "</a>.");
                    makeClickGoto(issuesUrlLabel, ISSUES_URL);
                    card4.add(issuesUrlLabel);
                }
                cards.add(card4);
            }
            panel.add(cards);
            cards.setAlignmentX(LEFT_ALIGNMENT);

            JPanel buttonPanel = new JPanel();
            {
                buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
                JButton okButton = new JButton("OK");
                buttonPanel.add(okButton);
                okButton.addActionListener((ActionEvent e) -> {
                    AboutDialog.this.setVisible(false);
                    AboutDialog.this.dispose();
                });
//                JButton debugButton = new JButton(">");
//                buttonPanel.add(debugButton);
//                debugButton.addActionListener((ActionEvent e) -> {
//                    cardLayoutMgr.next(cards);
//                });
            }
            panel.add(buttonPanel);
            buttonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        }
        add(panel);

        worker = new SwingWorker<String, Object>() {
            @Override
            protected void done() {
                try {
                    progress.setIndeterminate(false);
                    progress.setValue(progress.getMaximum());
                    if (! isCancelled()) {
                        newVersionLabel.setText(newVersionLabel.getText().replace("()", "(" + get() + ")"));
                        if (get() != null)
                            cardLayoutMgr.next(cards); // Second card is for when there actually is an update
                        else
                            cardLayoutMgr.last(cards); // Forth (and last) card is for when there is no update
                    }
                } catch (InterruptedException e) {
                } catch (ExecutionException e) {
                    cardLayoutMgr.next(cards);
                    cardLayoutMgr.next(cards);  // Third card is when update could not be determined
                }
            }

            @Override
            protected String doInBackground() throws Exception {
                return checkForUpdate();
            }
        };
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                // Start the background work directly _after_ the window is displayed.
                worker.execute();
            }

            @Override
            public void windowClosed(WindowEvent e) {
                // Clean up
                progress.setValue(progress.getMaximum());
                worker.cancel(true);
            }
        });
    }

    String checkForUpdate() throws CannotDetermineUpdateException {
        try {
            URL downloads = new URL(DOWNLOAD_API_URL);
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(downloads.openStream()))) {
                return getHigherVersion(reader.readLine(), getVersion());
            }
        }
        catch (Exception error) {
            throw new CannotDetermineUpdateException();
        }
    }

    String getVersion() {
        InputStream in = getClass().getResourceAsStream("version.properties");
        if (in != null) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
                return reader.readLine();
            } catch (IOException e) {
                return null;
            }
        }
        else return null;
    }

    void makeClickGoto(JLabel label, String url) {
        label.setCursor(new Cursor(Cursor.HAND_CURSOR));
        label.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    Desktop.getDesktop().browse(new URI(url));
                } catch (URISyntaxException | IOException ex) {
                    //It looks like there's a problem
                }
            }
        });
    }

    static String getHigherVersion(String content, String current) throws CannotDetermineUpdateException {
        if (current == null)
            throw new IllegalArgumentException("current version is unknown");

        Matcher m = Pattern.compile("((\\d+)\\.(\\d+)(\\.(\\d+))?)").matcher(current);
        if (! m.find()) {
            throw new IllegalArgumentException("current version cannt be parsed");
        }
        int currentVersion = (Integer.parseInt(m.group(2)) * 1000000) + Integer.parseInt(m.group(3)) * 1000;
        if (m.group(5) != null) {
            currentVersion += Integer.parseInt(m.group(5));
        }

        int higherVersion = 0;
        m = Pattern.compile("-((\\d+)\\.(\\d+)(\\.(\\d+))?)\\.jar").matcher(content);
        while (m.find()) {
            int version = (Integer.parseInt(m.group(2)) * 1000000) + Integer.parseInt(m.group(3)) * 1000;
            if (m.group(5) != null) {
                version += Integer.parseInt(m.group(5));
            }
            if (version > higherVersion)
                higherVersion = version;
        }

        if (higherVersion > 0 && higherVersion > currentVersion) {
            String result = "" + (higherVersion / 1000000) + "." + ((higherVersion % 1000000) / 1000);
            if (higherVersion % 1000 > 0)
                result += "." + (higherVersion % 1000);
            return result;
        }
        else if (higherVersion > 0)
            return null;
        else
            throw new CannotDetermineUpdateException();
    }

    public static void showDialog(Window parent) {
        if (aboutDlg == null) {
            aboutDlg = new AboutDialog(parent);
            aboutDlg.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            aboutDlg.pack();
            aboutDlg.setLocationRelativeTo(parent);
            aboutDlg.setVisible(true);
            aboutDlg.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    super.windowClosed(e);
                    aboutDlg = null;
                }
            });
        }
        else {
            JDialog dlg = aboutDlg;
            if (dlg != null)
                dlg.toFront();
        }
    }

    public static void main(String[] args) {
        showDialog(null);
    }

    static class CannotDetermineUpdateException extends Exception {}
}
