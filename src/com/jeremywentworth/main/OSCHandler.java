/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jeremywentworth.main;

import com.jeremywentworth.audio.GranulatorInterface;
import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Logger;
import oscP5.OscMessage;
import oscP5.OscP5;

/**
 *
 * @author Jeremy Wentworth
 */
public class OSCHandler {

    private GrainMainFrame grainMainFrame;
    private static Logger log = Logger.getLogger(OSCHandler.class);

    public OSCHandler(GrainMainFrame grainMainFrame) {
        this.grainMainFrame = grainMainFrame;
        OscP5 oscP5 = new OscP5(this, 8000);
    }

    void oscEvent(OscMessage message) {
        log.debug("---------------------------------------------------------");
        log.debug("message = " + message);
        log.debug("addrPattern = " + message.addrPattern());
        log.debug("typetag = " + message.typetag());
        log.debug("timetag = " + message.timetag());
        log.debug("arguments = " + ArrayUtils.toString(message.arguments()));
        if (message.addrPattern() != null && message.typetag() != null) {
            try {
                handleSonicLifeiPhoneApp(message);
                handleTouchOSCiPhoneApp(message);
            } catch (Exception ex) {
                log.error(ex, ex);
            }
        }
        log.debug("---------------------------------------------------------");
    }

    private void handleSonicLifeiPhoneApp(OscMessage message) {
        float[] floatArgs = getFloatArguments(message);
        if (floatArgs.length == 0) {
            return;
        }
        if (grainMainFrame != null) {
            GranulatorInterface granulator = grainMainFrame.getGranulator();
            float absVal = Math.abs(floatArgs[0]);
            if (message.addrPattern().equals("/x")) {
                granulator.setStartRatio(absVal);
            }
            if (message.addrPattern().equals("/y")) {
                granulator.setEndRatio(absVal);
            }
            if (message.addrPattern().equals("/z")) {
                granulator.setPositionRatio(absVal);
            }
        }
    }
    boolean[] toggles = new boolean[4];
    float xArg;
    float yArg;

    private void handleTouchOSCiPhoneApp(OscMessage message) {
        float[] floatArgs = getFloatArguments(message);
        if (floatArgs.length == 0) {
            return;
        }
        if (grainMainFrame != null) {
            GranulatorInterface granulator = grainMainFrame.getGranulator();
            if (message.addrPattern().equals("/3/xy")) {
                //Simple Layout Screen 3 is the x,y controller
                xArg = floatArgs[0];
                yArg = floatArgs[1];

            } else if (message.addrPattern().equals("/3/toggle1")) {
                toggles[0] = floatArgs[0] == 1;

            } else if (message.addrPattern().equals("/3/toggle2")) {
                toggles[1] = floatArgs[0] == 1;

            } else if (message.addrPattern().equals("/3/toggle3")) {
                toggles[2] = floatArgs[0] == 1;

            } else if (message.addrPattern().equals("/3/toggle4")) {
                toggles[3] = floatArgs[0] == 1;

            }
            if (areAllTogglesOff()) {
                granulator.reset();
            } else {
                updateGranulator();
            }
        }
    }

    private boolean areAllTogglesOff() {
        for (boolean b : toggles) {
            if (b) {
                return false;
            }
        }
        return true;
    }

    private void updateGranulator() {
        GranulatorInterface granulator = grainMainFrame.getGranulator();
        if (toggles[0]) {
            granulator.setLoopPointsFraction(xArg, yArg);
        }
        if (toggles[1]) {
            granulator.addPitchEnvelopeSegment(xArg, grainMainFrame.getDefaultEnvelopeTime());
            granulator.addRateEnvelopeSegment(yArg, grainMainFrame.getDefaultEnvelopeTime());
        }
        if (toggles[2]) {
            granulator.addRandomPanEnvelopeSegment(xArg, grainMainFrame.getDefaultEnvelopeTime());
            granulator.addRandomEnvelopeSegment(yArg, grainMainFrame.getDefaultEnvelopeTime());
        }
    }

    private float[] getFloatArguments(OscMessage message) {
        char[] typeChars = message.typetag().toCharArray();
        Object[] args = message.arguments();
        float[] floatArgs = new float[args.length];
        for (int i = 0; i < args.length; i++) {
            if (typeChars[i] == 'i' || typeChars[i] == 'f') {
                try {
                    floatArgs[i] = ((Number) message.arguments()[i]).floatValue();
                } catch (Exception ex) {
                }
            }
        }
//        log.debug("floatArgs = " + ArrayUtils.toString(floatArgs));
        return floatArgs;
    }

    public static void main(String[] args) {
        new OSCHandler(null);
    }
}
