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

import static javax.swing.BoxLayout.Y_AXIS;

public class SingleWriteWebSocketSamplerGuiPanel extends WebSocketSamplerGuiPanel {

    private DataPanel dataPanel;


    public SingleWriteWebSocketSamplerGuiPanel() {
        init();
    }

    private void init() {

        this.setLayout(new BoxLayout(this, Y_AXIS));

        JPanel connectionPanel = createConnectionPanel();
        this.add(connectionPanel);
        connectionPanel.setAlignmentX(JComponent.LEFT_ALIGNMENT);

        JSplitPane splitter = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        {
            dataPanel = new DataPanel();

            splitter.setTopComponent(dataPanel);
            splitter.setBottomComponent(createAboutPanel(this));
            splitter.setBorder(null);
        }
        this.add(splitter);
        splitter.setAlignmentX(JComponent.LEFT_ALIGNMENT);

        JPanel stuffIt = new JPanel();
        this.add(stuffIt);
        stuffIt.setAlignmentX(JComponent.LEFT_ALIGNMENT);
    }

    void clearGui() {
        super.clearGui();
        dataPanel.clearGui();
        setCreateNewConnection(true);
    }

    public String getRequestData() {
        return dataPanel.getRequestData();
    }

    public void setRequestData(String requestData) {
        dataPanel.setRequestData(requestData);
    }

    public DataPayloadType getType() {
        return dataPanel.getType();
    }

    public void setType(DataPayloadType type) {
        dataPanel.setType(type);
    }

    public boolean getReadDataFromFile() {
        return dataPanel.getReadDataFromFile();
    }

    public void setReadDataFromFile(boolean enable) {
        dataPanel.setReadDataFromFile(enable);
    }

    public String getDataFile() {
        return dataPanel.getDataFile();
    }

    public void setDataFile(String file) {
        dataPanel.setDataFile(file);
    }


    public static void main(String[] args) {
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 400);
        frame.getContentPane().add(new SingleWriteWebSocketSamplerGuiPanel());
        frame.setVisible(true);
    }
}