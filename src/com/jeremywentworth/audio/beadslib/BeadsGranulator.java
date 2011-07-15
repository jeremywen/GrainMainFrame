/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jeremywentworth.audio.beadslib;

import com.jeremywentworth.audio.AbstractGranulator;
import com.jeremywentworth.audio.SampleLoopType;
import com.jeremywentworth.commons.Utils;
import com.jeremywentworth.main.GrainMainFrame;
import java.io.File;
import javax.sound.sampled.AudioFileFormat.Type;
import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.data.*;
import net.beadsproject.beads.ugens.*;
import org.apache.log4j.Logger;

/**
 *
 * @author Jeremy Wentworth
 */
public class BeadsGranulator extends AbstractGranulator {

    private AudioContext audioContext;
    private GranularSamplePlayer player;
    private Envelope gainEnvelope;
    private Envelope grainIntervalEnvelope;
    private Envelope grainPitchEnvelope;
    private Envelope grainRateEnvelope;
    private Envelope grainRandomEnvelope;
    private Envelope grainRandomPanEnvelope;
    private float grainIntervalDestination;
    private float grainPitchDestination;
    private float grainRateDestination;
    private float grainRandomDestination;
    private float grainRandomPanDestination;
    private Gain mainGain;
    private BeadsDelay beadsDelay;
    private RecordToFile recordToFile;
    private static Logger log = Logger.getLogger(BeadsGranulator.class);

    public BeadsGranulator(GrainMainFrame grainMainFrame) {
        this(grainMainFrame, null);
    }

    public BeadsGranulator(GrainMainFrame grainMainFrame, AudioContext audioContext) {
        super(grainMainFrame);
        if (grainMainFrame.isRunningAsVST()) {
//            player = new GranSamPlayVst(audioContext, 2);
        } else {
            audioContext = new AudioContext();
        }
        this.audioContext = audioContext;
        player = new GranularSamplePlayer(audioContext, 2);
        gainEnvelope = new Envelope(audioContext, grainMainFrame.getCurrentGain());
        mainGain = new Gain(audioContext, 2, gainEnvelope);
        mainGain.addInput(player);
        audioContext.out.addInput(mainGain);
        if (!grainMainFrame.isRunningAsVST()) {
            audioContext.start();
        }
    }

    @Override
    public void startPlaying() {
        if (fileToGranulate == null || !fileToGranulate.isFile()) {
            log.debug("can't play - fileToGranulate = " + fileToGranulate);
            return;
        }
        setPaused(true);
        Sample newSample = null;
        Sample oldSample = null;
        try {
            newSample = SampleManager.sample(fileToGranulate.getPath());
            oldSample = player.getSample();
            player.setSample(newSample);
        } catch (Exception ex) {
            log.error(ex, ex);
            showFadeMessage("Unable to load sample.", true);
            if (oldSample == null) {
                return;
            } else {
                try {
                    player.setSample(oldSample);
                } catch (Exception ex2) {
                    showFadeMessage("Unable to load sample.", true);
                    return;
                }
            }
        }

        File file = new File(player.getSample().getFileName());
        log.debug("<html>Playing " + Utils.getMegString(file) + " " + file + "<br/>" + Utils.getMemoryString() + "</html>");
        resetGain();
        reset();
        player.start();
    }

    @Override
    public void reset() {
        if (grainPitchEnvelope == null) {
            grainPitchEnvelope = new Envelope(audioContext, 0);
            player.setPitch(grainPitchEnvelope);
        }
        if (grainIntervalEnvelope == null) {
            grainIntervalEnvelope = new Envelope(audioContext, 0);
            player.setGrainInterval(grainIntervalEnvelope);
        }
        if (grainRandomEnvelope == null) {
            grainRandomEnvelope = new Envelope(audioContext, 0);
            player.setRandomness(grainRandomEnvelope);
        }
        if (grainRandomPanEnvelope == null) {
            grainRandomPanEnvelope = new Envelope(audioContext, 0);
            player.setRandomPan(grainRandomPanEnvelope);
        }
        if (grainRateEnvelope == null) {
            grainRateEnvelope = new Envelope(audioContext, 0);
            player.setRate(grainRateEnvelope);
        }
        addPitchEnvelopeSegment(1, 0);
        addGrainIntervalSegment(20, 0);
        addRandomEnvelopeSegment(0.25f, 0);
        addRandomPanEnvelopeSegment(0.75f, 0);
        addRateEnvelopeSegment(1, 0);

        setPositionRatio(this.startRatio);
        setLoopPointsFraction(this.startRatio, this.endRatio);
        setLoopType(SampleLoopType.Alternating);
        notifyObservers();
    }

    @Override
    public void startRecordingToFile() {
        try {
            lastFileRecordedAndSaved = getNewFileToRecord();
            log.debug("Recording to " + lastFileRecordedAndSaved);
            recordToFile = new RecordToFile(audioContext, 2, lastFileRecordedAndSaved, Type.WAVE);
            recordToFile.addInput(mainGain);
            recordToFile.start();
            audioContext.out.addDependent(recordToFile);
            log.debug("Starting recording...");
        } catch (Exception ex) {
            log.error(ex, ex);
            showFadeMessage("Unable to start recording.", true);
        }
    }

    @Override
    public void saveRecordedFile() {
        if (!isRecordingToFile()) {
            log.debug("nothing to save!!");
            return;
        }
        recordToFile.kill();
        recordToFile = null;
    }

    @Override
    public boolean isRecordingToFile() {
        return recordToFile != null;
    }

    @Override
    public void setPaused(boolean paused) {
        player.pause(paused);
    }

    @Override
    public void changeLoopType() {
        if (player.getLoopType() == SamplePlayer.LoopType.LOOP_FORWARDS) {
            player.setLoopType(SamplePlayer.LoopType.LOOP_BACKWARDS);
        } else if (player.getLoopType() == SamplePlayer.LoopType.LOOP_BACKWARDS) {
            player.setLoopType(SamplePlayer.LoopType.LOOP_ALTERNATING);
        } else if (player.getLoopType() == SamplePlayer.LoopType.LOOP_ALTERNATING) {
            player.setLoopType(SamplePlayer.LoopType.LOOP_FORWARDS);
        }
        notifyObservers();
    }

    @Override
    public void setLoopType(SampleLoopType sampleLoopType) {
        if (sampleLoopType == SampleLoopType.Forward) {
            player.setLoopType(SamplePlayer.LoopType.LOOP_FORWARDS);
        } else if (sampleLoopType == SampleLoopType.Backward) {
            player.setLoopType(SamplePlayer.LoopType.LOOP_BACKWARDS);
        } else if (sampleLoopType == SampleLoopType.Alternating) {
            player.setLoopType(SamplePlayer.LoopType.LOOP_ALTERNATING);
        }
        notifyObservers();
    }

    @Override
    public SampleLoopType getLoopType() {
        if (player == null || player.getLoopType() == SamplePlayer.LoopType.LOOP_FORWARDS) {
            return SampleLoopType.Forward;
        } else if (player.getLoopType() == SamplePlayer.LoopType.LOOP_BACKWARDS) {
            return SampleLoopType.Backward;
        } else if (player.getLoopType() == SamplePlayer.LoopType.LOOP_ALTERNATING) {
            return SampleLoopType.Alternating;
        } else {
            return null;
        }
    }

    @Override
    public double getPositionRatio() {
        return getSamplePosition() / getSampleLength();
    }

    @Override
    public void setPositionRatio(double positionRatio) {
        if (player == null) {
            return;
        }
        player.setPosition(getSampleLength() * positionRatio);
        notifyObservers();
    }

    @Override
    public float getAudioOutValue() {
        if (audioContext != null) {
            return audioContext.out.getValue();
        } else {
            return 0;
        }
    }

    @Override
    public float getPitchEnvelopeValue() {
        return grainPitchDestination;
    }

    @Override
    public float getRateEnvelopeValue() {
        return grainRateDestination;
    }

    @Override
    public float getRandomPanValue() {
        return grainRandomPanDestination;
    }

    @Override
    public float getRandomnessValue() {
        return grainRandomDestination;
    }

    @Override
    public float getGrainIntervalEnvelopeValue() {
        return grainIntervalDestination;
    }

    @Override
    public double getSamplePosition() {
        if (player != null) {
            return player.getPosition();
        } else {
            return 0;
        }
    }

    @Override
    public boolean isSamplePaused() {
        return player.isPaused();
    }

    @Override
    public double getSampleLength() {
        if (player != null && player.getSample() != null) {
            return player.getSample().getLength();
        } else {
            return 0;
        }
    }

    @Override
    public void setLoopPointsFraction(float startRatio, float endRatio) {
        if (player != null && player.getSample() != null) {
            this.startRatio = startRatio;
            this.endRatio = endRatio;

            player.setLoopPointsFraction(startRatio, endRatio);
            if (getPositionRatio() < startRatio || getPositionRatio() > endRatio) {
                player.setToLoopStart();
            }

            //just print and dont log this
            System.out.println("loop = " + startRatio + " , " + endRatio);
            notifyObservers();
        }
    }

    @Override
    public void setGain(float gain) {
        if (mainGain != null) {
            mainGain.setGain(gain);
            System.out.println("gain = " + gain);
            notifyObservers();
        }
    }

    @Override
    public void addRateEnvelopeSegment(float destination, float duration) {
        if (grainRateEnvelope != null) {
            grainRateEnvelope.clear();
            grainRateEnvelope.addSegment(destination, duration);
            grainRateDestination = destination;
            notifyObservers();
        }
    }

    @Override
    public void addGrainIntervalSegment(float destination, float duration) {
        if (grainIntervalEnvelope != null && destination > 5) {
            grainIntervalEnvelope.clear();
            grainIntervalEnvelope.addSegment(destination, duration);
            grainIntervalDestination = destination;
            notifyObservers();
        }
    }

    @Override
    public void addPitchEnvelopeSegment(float destination, float duration) {
        if (grainPitchEnvelope != null) {
            grainPitchEnvelope.clear();
            grainPitchEnvelope.addSegment(destination, duration);
            grainPitchDestination = destination;
            notifyObservers();
        }
    }

    @Override
    public void addRandomEnvelopeSegment(float destination, float duration) {
        if (grainRandomEnvelope != null) {
            grainRandomEnvelope.setValue(destination);
            grainRandomDestination = destination;
            notifyObservers();
        }
    }

    @Override
    public void addRandomPanEnvelopeSegment(float destination, float duration) {
        if (grainRandomPanEnvelope != null) {
            System.out.println(destination);
            grainRandomPanEnvelope.setValue(destination);
            grainRandomPanDestination = destination;
            notifyObservers();
        }
    }

    @Override
    public void setDelayEnabled(boolean enabled) {
        this.delayEnabled = enabled;
        if (delayEnabled) {
            if (beadsDelay == null) {
                beadsDelay = new BeadsDelay(audioContext);
                beadsDelay.addInput(player);
                mainGain.addInput(beadsDelay);
                audioContext.out.addDependent(beadsDelay);
                log.debug("Delay is now ON");
            } else {
                return;
            }
        } else {
            if (beadsDelay != null) {
                beadsDelay.kill();
            }
            beadsDelay = null;
            log.debug("Delay is now OFF");
        }
    }

    @Override
    public boolean isDelayEnabled() {
        return delayEnabled;
    }

    @Override
    public float getDelayFeedback() {
        return beadsDelay.getFeedbackFloat();
    }

    @Override
    public float getDelayTime() {
        return beadsDelay.getDelayInMillisFloat();
    }

    @Override
    public float getDelayWet() {
        return beadsDelay.getWetLevelFloat();
    }

    @Override
    public void setDelayFeedback(float feedbackLevel) {
        setDelayEnabled(true);
        beadsDelay.setFeedbackFloat(feedbackLevel);
    }

    @Override
    public void setDelayTime(float timeInMillis) {
        setDelayEnabled(true);
        beadsDelay.setDelayInMillisFloat(timeInMillis);
    }

    @Override
    public void setDelayWet(float wetLevel) {
        setDelayEnabled(true);
        beadsDelay.setWetLevelFloat(wetLevel);
    }

    public GranularSamplePlayer getPlayer() {
        return player;
    }

    public Gain getMainGain() {
        return mainGain;
    }

    public AudioContext getAudioContext() {
        return audioContext;
    }

    @Override
    public void stopAudio() {
        audioContext.stop();
    }
}
