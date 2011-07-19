/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jeremywentworth.main;

import com.jeremywentworth.audio.AbstractGranulator;
import com.jeremywentworth.audio.GranulatorInterface;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.UUID;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import org.apache.log4j.Logger;

/**
 *
 * @author Jeremy Wentworth
 */
public class KeyHandler {

    private GrainMainFrame grainMainFrame;
    private GraphicsPanel graphicsPanel;
    private static Logger log = Logger.getLogger(KeyHandler.class);

    public KeyHandler(GrainMainFrame grainMainFrame) {
        this.grainMainFrame = grainMainFrame;
        this.graphicsPanel = grainMainFrame.getGraphicsPanel();
        addKeyboardMaps();
    }

    private void map(KeyStroke keyStroke, AbstractAction abstractAction) {
        UUID uuid = UUID.randomUUID();
        graphicsPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(keyStroke, uuid);
        graphicsPanel.getActionMap().put(uuid, abstractAction);
    }

    public void addKeyboardMaps() {
        map(KeyStroke.getKeyStroke(KeyEvent.VK_X, 0), new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                grainMainFrame.handleDroppedFiles(GrainMainFrame.PlayWhichOne.Random);
            }
        });

        map(KeyStroke.getKeyStroke(KeyEvent.VK_A, 0), new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                grainMainFrame.arrangeFrames();
            }
        });

        map(KeyStroke.getKeyStroke(KeyEvent.VK_C, 0), new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                grainMainFrame.spawnChild(1);
            }
        });

        map(KeyStroke.getKeyStroke(KeyEvent.VK_T, 0), new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                graphicsPanel.setTextVisible(!graphicsPanel.isTextVisible());
            }
        });

        map(KeyStroke.getKeyStroke(KeyEvent.VK_H, 0), new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                graphicsPanel.setHelpVisible(!graphicsPanel.isHelpVisible());
            }
        });

        map(KeyStroke.getKeyStroke(KeyEvent.VK_R, 0), new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                grainMainFrame.getControlHandler().getRandomness().setVisible(true);
                grainMainFrame.addRandomEnvelopeSegment(graphicsPanel.getMousePosition());
            }
        });

        map(KeyStroke.getKeyStroke(KeyEvent.VK_P, 0), new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                grainMainFrame.getControlHandler().getRandomnessPan().setVisible(true);
                grainMainFrame.addRandomPanEnvelopeSegment(graphicsPanel.getMousePosition());
            }
        });

//        map(KeyStroke.getKeyStroke(KeyEvent.VK_D, 0), new AbstractAction() {
//
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                grainMainFrame.setDelayParams(graphicsPanel.getMousePosition());
//            }
//        });

//        map(KeyStroke.getKeyStroke(KeyEvent.VK_E, 0), new AbstractAction() {
//
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                grainMainFrame.getGranulator().setDelayEnabled(!grainMainFrame.getGranulator().isDelayEnabled());
//            }
//        });

        map(KeyStroke.getKeyStroke(KeyEvent.VK_Q, 0), new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                AbstractGranulator granulator = grainMainFrame.getGranulator();
                if (granulator.isRecordingToFile()) {
                    granulator.saveRecordedFile();
                } else {
                    granulator.startRecordingToFile();
                }
            }
        });
        map(KeyStroke.getKeyStroke(KeyEvent.VK_W, 0), new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                AbstractGranulator granulator = grainMainFrame.getGranulator();
                if (granulator.isRecordingToFile()) {
                    granulator.saveRecordedFile();
                }
                granulator.loadLastRecordedFile();
            }
        });
        map(KeyStroke.getKeyStroke(KeyEvent.VK_L, 0), new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                GranulatorInterface granulator = grainMainFrame.getGranulator();
                granulator.changeLoopType();
            }
        });

        map(KeyStroke.getKeyStroke(KeyEvent.VK_M, 0), new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                //make 4 possible points
                //if at any, move to next
                //if not at any, move to first
                Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

                Point topLeft = new Point(0, 0);
                Point bottomLeft = new Point(0, screenSize.height - grainMainFrame.getHeight());
                Point bottomRight = new Point(screenSize.width - grainMainFrame.getWidth(), screenSize.height - grainMainFrame.getHeight());
                Point topRight = new Point(screenSize.width - grainMainFrame.getWidth(), 0);
                Point center = new Point((screenSize.width - grainMainFrame.getWidth()) / 2, (screenSize.height - grainMainFrame.getHeight()) / 2);

                if (grainMainFrame.getLocation().equals(topLeft)) {

                    grainMainFrame.setLocation(bottomLeft);

                } else if (grainMainFrame.getLocation().equals(bottomLeft)) {

                    grainMainFrame.setLocation(bottomRight);

                } else if (grainMainFrame.getLocation().equals(bottomRight)) {

                    grainMainFrame.setLocation(topRight);

                } else if (grainMainFrame.getLocation().equals(topRight)) {

                    grainMainFrame.setLocation(center);

                } else {

                    grainMainFrame.setLocation(topLeft);

                }
            }
        });

        map(KeyStroke.getKeyStroke(KeyEvent.VK_COMMA, 0), new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                graphicsPanel.getMainPanelMouse().setRecordingMouseEvents(false);
                if (graphicsPanel.getMainPanelMouse().isPlayingMouseEvents()) {
                    graphicsPanel.getMainPanelMouse().setPlayingMouseEvents(false);
                } else {
                    graphicsPanel.getMainPanelMouse().setPlayingMouseEvents(true);
                    graphicsPanel.getMainPanelMouse().playbackMouseEvents();
                }
            }
        });

        map(KeyStroke.getKeyStroke(KeyEvent.VK_PERIOD, 0), new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (graphicsPanel.getMainPanelMouse().isRecordingMouseEvents()) {
                    graphicsPanel.getMainPanelMouse().setRecordingMouseEvents(false);
                    graphicsPanel.getMainPanelMouse().playbackMouseEvents();
                } else {
                    graphicsPanel.getMainPanelMouse().setRecordingMouseEvents(true);
                }
            }
        });

        map(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0), new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                grainMainFrame.getGranulator().reset();
                graphicsPanel.getMainPanelMouse().setPlayingMouseEvents(false);
            }
        });

        map(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                grainMainFrame.getGranulator().setPaused(!grainMainFrame.getGranulator().isSamplePaused());
            }
        });

        map(KeyStroke.getKeyStroke(KeyEvent.VK_CONTROL, InputEvent.CTRL_DOWN_MASK), new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                grainMainFrame.addGrainIntervalSegment(graphicsPanel.getMousePosition());
            }
        });

        map(KeyStroke.getKeyStroke(KeyEvent.VK_ALT, InputEvent.ALT_DOWN_MASK), new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                grainMainFrame.addRateEnvelopeSegment(graphicsPanel.getMousePosition());
            }
        });

        map(KeyStroke.getKeyStroke(KeyEvent.VK_SHIFT, InputEvent.SHIFT_DOWN_MASK), new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                grainMainFrame.addPitchEnvelopeSegment(graphicsPanel.getMousePosition());
            }
        });

        map(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
    }
}
