/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jeremywentworth.audio.beadslib;

import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.UGenChain;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.TapIn;
import net.beadsproject.beads.ugens.TapOut;

/**
 *
 * @author Jeremy Wentworth
 */
public class BeadsDelay extends UGenChain {

    private float delayInMillisFloat = 500;
    private float feedbackFloat = 0.5f;
    private float wetLevelFloat = 0.5f;

    public BeadsDelay(AudioContext ac) {
        super(ac, 2, 2);

        Gain dryGain = new Gain(ac, 2, 0.5f);
        drawFromChainInput(dryGain);
        addToChainOutput(dryGain);
        TapIn tapIn = new TapIn(ac, 10000);
        drawFromChainInput(tapIn);

        TapOut tapOut = new TapOut(ac, tapIn, delayInMillisFloat);
        Gain wetGain = new Gain(ac, 2, wetLevelFloat);
        wetGain.addInput(tapOut);
        addToChainOutput(wetGain);

        Gain feedbackGain = new Gain(ac, 2, feedbackFloat);
        feedbackGain.addInput(tapOut);
        tapIn.addInput(feedbackGain);
    }

    public float getDelayInMillisFloat() {
        return delayInMillisFloat;
    }

    public void setDelayInMillisFloat(float delayInMillisFloat) {
        this.delayInMillisFloat = delayInMillisFloat;
    }

    public float getFeedbackFloat() {
        return feedbackFloat;
    }

    public void setFeedbackFloat(float feedbackFloat) {
        this.feedbackFloat = feedbackFloat;
    }

    public float getWetLevelFloat() {
        return wetLevelFloat;
    }

    public void setWetLevelFloat(float wetLevelFloat) {
        this.wetLevelFloat = wetLevelFloat;
    }
}

