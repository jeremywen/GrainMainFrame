package com.jeremywentworth.vst;

import com.jeremywentworth.audio.beadslib.BeadsGranulator;
import com.jeremywentworth.main.GrainMainFrame;
import jvst.wrapper.VSTPluginAdapter;
import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.GranularSamplePlayer;
import org.apache.log4j.Logger;

/**
 * http://jvstwrapper.sourceforge.net/
 * @author Jeremy Wentworth
 */
public class GmfVstPluginAdapter extends VSTPluginAdapter {

    private AudioContext audioContext;
    private GrainMainFrame grainMainFrame;
    private BeadsGranulator beadsGranulator;
    private GranularSamplePlayer player;
    private Gain mainGain;
    private JVSTAudioIO jVSTAudioIO;
    private float[][] lastOut;
    private static Logger log = Logger.getLogger(GmfVstPluginAdapter.class);

    public GmfVstPluginAdapter(long Wrapper) {
        super(Wrapper);
        jVSTAudioIO = new JVSTAudioIO();
        audioContext = new AudioContext(jVSTAudioIO);
        grainMainFrame = new GrainMainFrame(audioContext);
        beadsGranulator = (BeadsGranulator) grainMainFrame.getGranulator();
        player = beadsGranulator.getPlayer();
        mainGain = beadsGranulator.getMainGain();
        mainGain.addDependent(player);
        jVSTAudioIO.setSourceUGen(mainGain);
        audioContext.start();
        audioContext.printCallChain();
    }

    public GrainMainFrame getGrainMainFrame() {
        return grainMainFrame;
    }

    @Override
    public void processReplacing(float[][] in, float[][] out, int length) {
        player.update();
        mainGain.update();
        jVSTAudioIO.process(in, out);
        lastOut = out;
    }

    public float[][] getLastOut() {
        return lastOut;
    }

    @Override
    public int canDo(String feature) {
//        if (feature.equals(CANDO_PLUG_1_IN_1_OUT)) {
//            ret = CANDO_YES;
//        }
//        if (feature.equals(CANDO_PLUG_PLUG_AS_CHANNEL_INSERT)) {
//            ret = CANDO_YES;
//        }
//        if (feature.equals(CANDO_PLUG_PLUG_AS_SEND)) {
//            ret = CANDO_YES;
//        }
//        log("canDo: " + feature + " = " + ret);
//        return ret;
        return 0;
    }

    @Override
    public int getPlugCategory() {
        return PLUG_CATEG_SYNTH;
    }

    @Override
    public String getProductString() {
        return GrainMainFrame.PRODUCT_NAME;
    }

    @Override
    public String getProgramNameIndexed(int i, int i1) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getVendorString() {
        return GrainMainFrame.WEB_SITE;
    }

    @Override
    public boolean setBypass(boolean bln) {
        return false;
    }

    @Override
    public boolean string2Parameter(int i, String string) {
        return true;
    }

    @Override
    public int getNumParams() {
        return 1;
    }

    @Override
    public int getNumPrograms() {
        return 1;
    }

    @Override
    public float getParameter(int i) {
        return 13f;
    }

    @Override
    public String getParameterDisplay(int i) {
        return "Param" + i;
    }

    @Override
    public String getParameterLabel(int i) {
        return "Param" + i;
    }

    @Override
    public String getParameterName(int i) {
        return "Param" + i;
    }

    @Override
    public int getProgram() {
        return 0;
    }

    @Override
    public String getProgramName() {
        return "Program";
    }

    @Override
    public void setParameter(int i, float f) {
        //TODO
    }

    @Override
    public void setProgram(int i) {
        //TODO
    }

    @Override
    public void setProgramName(String string) {
        //TODO
    }
}
