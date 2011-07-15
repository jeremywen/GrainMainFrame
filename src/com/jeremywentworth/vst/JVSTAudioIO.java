package com.jeremywentworth.vst;

import net.beadsproject.beads.core.AudioIO;
import net.beadsproject.beads.core.UGen;

public class JVSTAudioIO extends AudioIO {

    private float[][] inputs;
    private UGen sourceUGen;

    public JVSTAudioIO() {
    }

    public void setSourceUGen(UGen sourceUGen) {
        this.sourceUGen = sourceUGen;
    }

    @Override
    protected UGen getAudioInput(int[] channels) {
        return sourceUGen;
    }

    @Override
    protected boolean start() {
        return true;
    }

    protected void process(float[][] in, float[][] out) {
        update();
//        for (int i = 0; i < out.length; i++) {
//            out[i] = context.out.getOutBuffer(i);
//        }
        fillMatrixFromUGen(out, context.out);
    }

    public static void fillMatrixFromUGen(float[][] matrix, UGen uGen) {
        matrix = new float[uGen.getOuts()][uGen.getOutBuffer(0).length];
        for (int channel = 0; channel < matrix.length; channel++) {
            float[] channelBuffer = uGen.getOutBuffer(channel);
            for (int buffIndex = 0; buffIndex < channelBuffer.length; buffIndex++) {
                float bufferVal = channelBuffer[buffIndex];
                matrix[channel][buffIndex] = bufferVal;
            }
        }
    }

//    private class JVSTAudioInput extends UGen {
//
//        private int[] channels;
//
//        public JVSTAudioInput(AudioContext context, int[] channels) {
//            super(context, channels.length);
//            this.channels = channels;
//            outputInitializationRegime = OutputInitializationRegime.NULL;
//            outputPauseRegime = OutputPauseRegime.ZERO;
//        }
//
//        @Override
//        public void calculateBuffer() {
//            for (int i = 0; i < outs; i++) {
//                bufOut[i] = inputs[channels[i]];
//            }
//        }
//    }
}
