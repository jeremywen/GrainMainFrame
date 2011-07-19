/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jeremywentworth.audio;

/**
 *
 * @author Jeremy Wentworth
 */
public interface Delayable {

    public void setDelayTime(float timeInMillis);

    public void setDelayFeedback(float feedbackLevel);

    public void setDelayWet(float wetLevel);

    public float getDelayTime();

    public float getDelayFeedback();

    public float getDelayWet();

    public void setDelayEnabled(boolean enabled);

    public boolean isDelayEnabled();
}
