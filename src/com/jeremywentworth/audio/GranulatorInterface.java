/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jeremywentworth.audio;

import java.io.File;

/**
 * Use this so i can switch out granulating libraries
 * @author Jeremy Wentworth
 */
public interface GranulatorInterface {

    public boolean isFileRecorded();

    public boolean isRecordingToFile();

    public void startRecordingToFile();

    public void saveRecordedFile();

    public File getFileRecordingTo();

    public void loadLastRecordedFile();

    public void setPaused(boolean paused);

    public void startPlaying();

    public void stopAudio();

    public void setFileToGranulate(File file);

    public File getFileToGranulate();

    public double getSamplePosition();

    public boolean isSamplePaused();

    public double getSampleLength();

    public void reset();

    public void changeLoopType();

    public void setLoopType(SampleLoopType sampleLoopType);

    public SampleLoopType getLoopType();

    public double getPositionRatio();

    public void setPositionRatio(double positionRatio);

    public float getEndRatio();

    public float getStartRatio();

    public void setEndRatio(float endRatio);

    public void setStartRatio(float startRatio);

    public void setLoopPointsFraction(float startRatio, float endRatio);

    public void setGain(float gain);

    public void addGrainIntervalSegment(float destination, float duration);

    public void addPitchEnvelopeSegment(float destination, float duration);

    public void addRandomEnvelopeSegment(float destination, float duration);

    public void addRandomPanEnvelopeSegment(float destination, float duration);

    public void addRateEnvelopeSegment(float destination, float duration);

    public float getAudioOutValue();

    public float getPitchEnvelopeValue();

    public float getRateEnvelopeValue();

    public float getGrainIntervalEnvelopeValue();

    public float getRandomnessValue();

    public float getRandomPanValue();
}
