/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jeremywentworth.main;

import java.awt.Desktop;//Ignore in Java 1.5 Version
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.net.URI;
import org.apache.log4j.Logger;

/**
 *
 * @author Jeremy Wentworth
 */
public class MouseHandler implements MouseListener, MouseMotionListener, MouseWheelListener {

    private GrainMainFrame grainMainFrame;
    private MouseRecorder mouseRecorder;
    private Point mouseLocation;
    private static Logger log = Logger.getLogger(MouseHandler.class);

    public MouseHandler(GrainMainFrame grainMainFrame) {
        this.grainMainFrame = grainMainFrame;
        this.mouseRecorder = new MouseRecorder(this);
    }

    @Override
    public void mouseClicked(MouseEvent mouseEvent) {
        mouseRecorder.addMouseEvent("mouseClicked", mouseEvent);
        if (mouseEvent.getButton() == MouseEvent.BUTTON1) {
            if (mouseEvent.isControlDown()) {
            } else if (mouseEvent.isAltDown()) {
            } else if (mouseEvent.isShiftDown()) {
            } else {
                if (grainMainFrame.getGraphicsPanel().isWebSiteClickable(mouseEvent.getPoint())) {

                    if (Desktop.isDesktopSupported()) {//Ignore in Java 1.5 Version
                        try {//Ignore in Java 1.5 Version
                            Desktop.getDesktop().browse(new URI(GrainMainFrame.WEB_SITE));//Ignore in Java 1.5 Version
                        } catch (Exception ex) {//Ignore in Java 1.5 Version
                            log.error(ex, ex);//Ignore in Java 1.5 Version
                        }//Ignore in Java 1.5 Version
                    }//Ignore in Java 1.5 Version
                }
                grainMainFrame.getGranulator().setPaused(!grainMainFrame.getGranulator().isSamplePaused());
            }
        } else if (mouseEvent.getButton() == MouseEvent.BUTTON2) {
            if (mouseEvent.isControlDown()) {
            } else if (mouseEvent.isAltDown()) {
            } else if (mouseEvent.isShiftDown()) {
            } else {
            }
        } else if (mouseEvent.getButton() == MouseEvent.BUTTON3) {
            if (mouseEvent.isControlDown()) {
            }
            if (mouseEvent.isAltDown()) {
            }
            if (mouseEvent.isShiftDown()) {
            }
            grainMainFrame.setLockLoopEndPoint(!grainMainFrame.isLockLoopEndPoint());
        }
    }

    @Override
    public void mousePressed(MouseEvent mouseEvent) {
        mouseRecorder.addMouseEvent("mousePressed", mouseEvent);
        if (mouseEvent.getButton() == MouseEvent.BUTTON1) {
            if (mouseEvent.isControlDown()) {
            }
            if (mouseEvent.isAltDown()) {
            }
            if (mouseEvent.isShiftDown()) {
            }
            grainMainFrame.setChangingLoopPoints(true);
        } else if (mouseEvent.getButton() == MouseEvent.BUTTON2) {
            if (mouseEvent.isControlDown()) {
            }
            if (mouseEvent.isAltDown()) {
            }
            if (mouseEvent.isShiftDown()) {
            }
            {
            }
        } else if (mouseEvent.getButton() == MouseEvent.BUTTON3) {
            if (mouseEvent.isControlDown()) {
            }
            if (mouseEvent.isAltDown()) {
            }
            if (mouseEvent.isShiftDown()) {
            }
        }
    }

    @Override
    public void mouseReleased(MouseEvent mouseEvent) {
        mouseRecorder.addMouseEvent("mouseReleased", mouseEvent);
        grainMainFrame.setChangingLoopPoints(false);
    }

    @Override
    public void mouseEntered(MouseEvent mouseEvent) {
    }

    @Override
    public void mouseExited(MouseEvent mouseEvent) {
    }

    @Override
    public void mouseDragged(MouseEvent mouseEvent) {
        mouseRecorder.addMouseEvent("mouseDragged", mouseEvent);
        if (grainMainFrame.isChangingLoopPoints()) {

            mouseLocation = mouseEvent.getPoint();
            grainMainFrame.setLoopPointsFraction(mouseLocation);
            moveControls(mouseEvent);
        }
    }

    private void moveControls(MouseEvent mouseEvent) {
        if (mouseEvent.isControlDown()) {

            mouseLocation = mouseEvent.getPoint();
            grainMainFrame.addGrainIntervalSegment(mouseLocation);

        }

        if (mouseEvent.isAltDown()) {

            mouseLocation = mouseEvent.getPoint();
            grainMainFrame.addRateEnvelopeSegment(mouseLocation);

        }

        if (mouseEvent.isShiftDown()) {

            mouseLocation = mouseEvent.getPoint();
            grainMainFrame.addPitchEnvelopeSegment(mouseLocation);

        }
    }

    @Override
    public void mouseMoved(MouseEvent mouseEvent) {
//        log.debug("mouseEvent = "+mouseEvent);
        mouseRecorder.addMouseEvent("mouseMoved", mouseEvent);
        moveControls(mouseEvent);

    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        float gainAdder = 0.01f;
        if (e.getWheelRotation() > 0) {
            log.debug("going down");
            gainAdder = -0.01f;
        }
        float newGain = grainMainFrame.getCurrentGain() + gainAdder;
        if (newGain < 0 || newGain > grainMainFrame.getMaxGain()) {
            return;
        }
        grainMainFrame.setCurrentGain(newGain);
    }

    public Point getMouseLocation() {
        return mouseLocation;
    }

    public boolean isPlayingMouseEvents() {
        return mouseRecorder.isPlaying();
    }

    public void setPlayingMouseEvents(boolean playingMouseEvents) {
        mouseRecorder.setPlaying(playingMouseEvents);
    }

    public boolean isRecordingMouseEvents() {
        return mouseRecorder.isRecording();
    }

    public void setRecordingMouseEvents(boolean recordingMouseEvents) {
        mouseRecorder.setRecording(recordingMouseEvents);
    }

    public void playbackMouseEvents() {
        mouseRecorder.playbackMouseEvents();
    }

    public MouseRecorder getMouseRecorder() {
        return mouseRecorder;
    }

    public void resetGranulator() {
        grainMainFrame.getGranulator().setPaused(true);
    }
}
