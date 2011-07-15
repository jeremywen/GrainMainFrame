/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jeremywentworth.main;

import java.awt.event.MouseEvent;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import org.apache.log4j.Logger;

/**
 *
 * @author Jeremy Wentworth
 */
public class MouseRecorder {

    private MouseHandler mainMouseListener;
    private boolean recording;
    private boolean playing;
    private ArrayList<MouseEventRecord> records;
    private boolean recordingStartTimeIsFirstEvent;
    private long recordingStartTime;
    private static Logger log = Logger.getLogger(MouseRecorder.class);

    public MouseRecorder(MouseHandler mainMouseListener) {
        this.mainMouseListener = mainMouseListener;
    }

    public void addMouseEvent(String methodName, MouseEvent mouseEvent) {
        if (recording && !playing) {
            records.add(new MouseEventRecord(methodName, mouseEvent));
        }
    }

    public boolean isPlaying() {
        return playing;
    }

    public void setPlaying(boolean playing) {
        this.playing = playing;
        log.debug("playing = " + playing);
    }

    public boolean isRecording() {
        return recording;
    }

    public void setRecording(boolean recording) {
        this.recording = recording;
        if (recording) {
            this.playing = false;
            records = new ArrayList<MouseEventRecord>();
            recordingStartTime = new Date().getTime();
        }
        log.debug("recordingMouseEvents = " + recording);
    }

    public void playbackMouseEvents() {
        Thread playbackThread = new Thread() {

            @Override
            public void run() {
                if (recording || records == null || records.size() == 0) {
                    return;
                }
                try {
                    while (playing) {
                        log.debug("playing mouse events");
                        long lastTime = recordingStartTime;
                        if (recordingStartTimeIsFirstEvent) {
                            lastTime = records.get(0).time;
                        }
                        for (MouseEventRecord mouseEventRecord : records) {
                            if (!playing) {
                                shutdown();
                                return;
                            }
                            long timeBetweenMouseEvents = mouseEventRecord.time - lastTime;
                            Thread.sleep(timeBetweenMouseEvents);
                            lastTime = mouseEventRecord.time;

                            Method method = mainMouseListener.getClass().getMethod(mouseEventRecord.methodName, mouseEventRecord.mouseEvent.getClass());
                            method.invoke(mainMouseListener, mouseEventRecord.mouseEvent);

//                            Robot robot = new Robot();
//                            robot.mouseMove(mouseEventRecord.mouseEvent.getXOnScreen(), mouseEventRecord.mouseEvent.getYOnScreen());
//                            log.debug("playing back " + mouseEventRecord);

                            if (!playing) {
                                shutdown();
                                return;
                            }
                        }
                    }
                } catch (Exception ex) {
                    log.error(ex, ex);
                }
                shutdown();
            }

            public void shutdown() {
                mainMouseListener.resetGranulator();
            }
        };
        playbackThread.start();
    }

    static class MouseEventRecord {

        private String methodName;
        private MouseEvent mouseEvent;
        private long time;

        public MouseEventRecord(String methodName, MouseEvent mouseEvent) {
            this.methodName = methodName;
            this.mouseEvent = mouseEvent;
            this.time = new Date().getTime();
        }

        @Override
        public String toString() {
            return methodName + ", " + mouseEvent;
        }
    }
}
