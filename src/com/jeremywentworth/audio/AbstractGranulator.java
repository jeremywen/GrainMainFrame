/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jeremywentworth.audio;

import com.jeremywentworth.main.GrainMainFrame;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.log4j.Logger;

/**
 *
 * @author Jeremy Wentworth
 */
abstract public class AbstractGranulator implements GranulatorInterface, Delayable, Observable {

    private GrainMainFrame grainMainFrame;
    protected File fileToGranulate;
    protected File lastFileRecordedAndSaved;
    protected float startRatio = 0;
    protected float endRatio = 1;
    protected boolean delayEnabled = true;
    private ArrayList<GranulatorObserver> observers = new ArrayList<GranulatorObserver>();
    private static Logger log = Logger.getLogger(AbstractGranulator.class);

    public AbstractGranulator(GrainMainFrame grainMainFrame) {
        this.grainMainFrame = grainMainFrame;
    }

    public void resetGain() {
        setGain(grainMainFrame.getCurrentGain());
    }

    public void showFadeMessage(String message, boolean isError) {
        grainMainFrame.getGraphicsPanel().showFadeMessage(message, isError);
    }

    public File getNewFileToRecord() {
        String uniqueString = new Date().getTime() + "";
        File file = new File("samples", "GMF-" + uniqueString + ".wav");
        return file;
    }

    @Override
    public void loadLastRecordedFile() {
        if (isRecordingToFile()) {
            saveRecordedFile();
        }
        if (lastFileRecordedAndSaved == null || !lastFileRecordedAndSaved.isFile()) {
            log.debug("cannot load last recording, lastFileRecordedAndSaved = " + lastFileRecordedAndSaved);
        }
        List<File> list = grainMainFrame.getDroppedFiles();
        log.debug("lastFileRecordedAndSaved = " + lastFileRecordedAndSaved);
        list.add(0, lastFileRecordedAndSaved);
        grainMainFrame.handleDroppedFiles(GrainMainFrame.PlayWhichOne.First);
    }

    @Override
    public void notifyObservers() {
        for (GranulatorObserver granulatorObserver : observers) {
            granulatorObserver.update(this);
        }
    }

    @Override
    public void addObserver(GranulatorObserver granulatorObserver) {
        observers.add(granulatorObserver);
    }

    @Override
    public void clearObservers() {
        observers.clear();
    }

    @Override
    public void removeObserver(GranulatorObserver granulatorObserver) {
        observers.remove(granulatorObserver);
    }

    @Override
    public File getFileRecordingTo() {
        return lastFileRecordedAndSaved;
    }

    @Override
    public boolean isFileRecorded() {
        return !isRecordingToFile() && lastFileRecordedAndSaved!=null && lastFileRecordedAndSaved.isFile();
    }

    @Override
    public File getFileToGranulate() {
        return fileToGranulate;
    }

    @Override
    public void setFileToGranulate(File file) {
        this.fileToGranulate = file;
    }

    @Override
    public float getEndRatio() {
        return endRatio;
    }

    @Override
    public float getStartRatio() {
        return startRatio;
    }

    @Override
    public void setEndRatio(float endRatio) {
        setLoopPointsFraction(startRatio, endRatio);
    }

    @Override
    public void setStartRatio(float startRatio) {
        setLoopPointsFraction(startRatio, endRatio);
    }
}
