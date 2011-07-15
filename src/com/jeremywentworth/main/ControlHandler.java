package com.jeremywentworth.main;

import com.jeremywentworth.audio.AbstractGranulator;
import com.jeremywentworth.audio.GranulatorObserver;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Jeremy Wentworth
 */
public class ControlHandler implements GranulatorObserver {

    private AControl gainLimit;
    private AControl pitch;
    private AControl rate;
    private AControl intervalRate;
    private AControl randomness;
    private AControl randomnessPan;
//    private AControl delayLevels;
    private List<AControl> arrayList;
    private GrainMainFrame grainMainFrame;

    public ControlHandler(GrainMainFrame grainMainFrame) {
        this.grainMainFrame = grainMainFrame;
        grainMainFrame.addGranulatorObserver(this);
        arrayList = new ArrayList<AControl>();

        gainLimit = new AControl("Volume", "VL");
        arrayList.add(gainLimit);

        pitch = new AControl("Pitch", "PI");
        arrayList.add(pitch);

        rate = new AControl("Rate", "RT");
        arrayList.add(rate);

        intervalRate = new AControl("Interval", "IV");
        arrayList.add(intervalRate);

        randomness = new AControl("Rnd", "RA");
        randomness.setVisible(false);
        arrayList.add(randomness);

        randomnessPan = new AControl("RndPan", "RP");
        randomnessPan.setVisible(false);
        arrayList.add(randomnessPan);

//        delayLevels = new AControl("Delay Levels", "DL");
//        arrayList.add(delayLevels);


        for (AControl circleControl : arrayList) {
            circleControl.setGraphicsPanel(grainMainFrame.getGraphicsPanel());
        }
    }

    public void draw(Graphics2D graphics2D) {
        for (AControl circleControl : arrayList) {
            circleControl.draw(graphics2D);
        }
    }

    @Override
    public void update(AbstractGranulator granulator) {
        //Gain level on right
        double gainControlRatio =
                1 - grainMainFrame.getCurrentGain()
                / grainMainFrame.getMaxGain();
        this.gainLimit.setCenterPointRatios(1, gainControlRatio);

        //Interval level on top
        double intervalRateRatio =
                granulator.getGrainIntervalEnvelopeValue()
                / grainMainFrame.getMaxGrainIntervalRate();
        this.intervalRate.setCenterPointRatios(intervalRateRatio, 0);

        //Pitch level on left
        double pitchRatio =
                granulator.getPitchEnvelopeValue()
                / grainMainFrame.getMaxPitchMultiplier();
        this.pitch.setCenterPointRatios(0, 1 - pitchRatio);

        //Rate level on top
        double rateRatio =
                granulator.getRateEnvelopeValue()
                / grainMainFrame.getMaxGrainRate();
        this.rate.setCenterPointRatios(rateRatio, 1);

        double rndRatio =
                granulator.getRandomnessValue()
                / grainMainFrame.getMaxRandomness();
        this.randomness.setCenterPointRatios(0, rndRatio);

        double rndPanRatio =
                granulator.getRandomPanValue()
                / grainMainFrame.getMaxRandomness();
        this.randomnessPan.setCenterPointRatios(rndPanRatio, 0);

//        double delayLevelsRatio =
//                granulator.getDelayTime()
//                / grainMainFrame.getMaxDelayTime();
//        this.delayLevels.setCenterPointRatios(delayLevelsRatio, 0);
    }

    public AControl getGainLimit() {
        return gainLimit;
    }

    public AControl getIntervalRate() {
        return intervalRate;
    }

    public AControl getPitch() {
        return pitch;
    }

    public AControl getRandomness() {
        return randomness;
    }

    public AControl getRandomnessPan() {
        return randomnessPan;
    }

    public AControl getRate() {
        return rate;
    }

    public List<AControl> getList() {
        return arrayList;
    }

    public List<AControl> getArrayList() {
        return arrayList;
    }
}
