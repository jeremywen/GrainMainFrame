/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jeremywentworth.audio.beadslib;

import com.jeremywentworth.commons.Utils;
import java.io.File;
import net.beadsproject.beads.data.Sample;
import net.beadsproject.beads.data.SampleManager;

/**
 *
 * @author Jeremy Wentworth
 */
public class BeadsUtils {

    public static float getRandomTimeInSample(Sample sample) {
        return (float) (Math.random() * sample.getLength());
    }

    public static Sample getRandomSample(String folder) {
        File file = Utils.getRandomFile(folder);
        if (file == null) {
            return null;
        }
        return SampleManager.sample(file.getPath());
    }
}
