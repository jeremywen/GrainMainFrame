/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jeremywentworth.vst;

import com.jeremywentworth.main.GrainMainFrame;
import com.jeremywentworth.main.GraphicsPanel;
import java.awt.Dimension;
import java.awt.GridLayout;
import javax.swing.JPanel;
import jvst.wrapper.VSTPluginAdapter;
import jvst.wrapper.VSTPluginGUIAdapter;
import jvst.wrapper.gui.VSTPluginGUIRunner;

/**
 *
 * @author Jeremy Wentworth
 */
public class GmfVstPluginGuiAdapter extends VSTPluginGUIAdapter {

    private GmfVstPluginAdapter gmfAdapter;
    private GrainMainFrame grainMainFrame;

    public GmfVstPluginGuiAdapter(VSTPluginGUIRunner guiRunner, VSTPluginAdapter pluginAdapter) {
        super(guiRunner, pluginAdapter);

        Dimension dimension = new Dimension(500, 500);
        gmfAdapter = (GmfVstPluginAdapter) pluginAdapter;
        grainMainFrame = gmfAdapter.getGrainMainFrame();
        GraphicsPanel graphicsPanel = grainMainFrame.getGraphicsPanel();
        graphicsPanel.setSize(dimension);
        graphicsPanel.setPreferredSize(dimension);

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(1, 1));
        panel.add(graphicsPanel);
        panel.validate();

        this.setTitle(GrainMainFrame.PRODUCT_NAME);
        this.getContentPane().add(panel);
        this.getContentPane().validate();
        this.setSize(dimension);
        this.setPreferredSize(dimension);
        this.setResizable(false);

        if (RUNNING_MAC_X) {
            this.setVisible(true);
        }
    }

    @Override
    public void close() {
        float[][] lastOut = gmfAdapter.getLastOut();
        System.out.println("Closing " + this.getClass().getSimpleName());
        System.out.println("lastOut.length = " + lastOut.length);
        for (float[] fs : lastOut) {
            for (float f : fs) {
                System.out.print(f + " ");
            }
            System.out.println("");
        }
        grainMainFrame.handleExit();
        super.close();
    }
}
