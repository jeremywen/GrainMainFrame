package com.jeremywentworth.main;

import com.jeremywentworth.audio.AbstractGranulator;
import com.jeremywentworth.audio.beadslib.BeadsGranulator;
import com.jeremywentworth.audio.GranulatorObserver;
import com.jeremywentworth.commons.LoggingOutputStream;
import com.jeremywentworth.commons.Utils;
import java.awt.Point;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import net.beadsproject.beads.core.AudioContext;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

/*
 * @author Jeremy Wentworth
 */
public class GrainMainFrame extends JFrame {

    public static final String PRODUCT_NAME = "GMF";
    public static final String HELP_FILE_NAME = "Help.txt";
    public static final String WEB_SITE = "www.jeremywentworth.com";
    private List<File> droppedFiles;
    private GraphicsPanel graphicsPanel;
    private Point dndLocation;
    private boolean changingLoopPoints = false;
    private boolean runningAsVST = false;
    private boolean lockLoopEndPoint = false;
    private float loopLengthWhenLocked = -1;
    private float maxGain = 0.75f;
    private float currentGain = 0.1f;
    private float maxPitchMultiplier = 5;
    private float maxGrainRate = 3;
    private float maxGrainIntervalRate = 300;
    private int maxRandomness = 500;
    private int maxDelayTime = 5000;
    private int defaultEnvelopeTime = 0;
    private int pitchStepCount = 12;
    private ControlHandler controlHandler;
    private AbstractGranulator granulator;
    public static ArrayList<GrainMainFrame> AllGrainMainFrames = new ArrayList<GrainMainFrame>();
    private Preferences preferences = Preferences.userNodeForPackage(GrainMainFrame.class);
    private OSCHandler oscHandler;
    private static Logger log = Logger.getLogger(GrainMainFrame.class);

    public GrainMainFrame() {
        this(new File("./samples"), null);
    }

    public GrainMainFrame(AudioContext audioContext) {
        this(new File("./samples"), audioContext);
    }

    public GrainMainFrame(File defaultFileOrFolder, AudioContext audioContext) {
        setRunningAsVST(audioContext != null);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        AllGrainMainFrames.add(this);
//        setUndecorated(true);
        setTitle(PRODUCT_NAME + " - Press h for Help");
        graphicsPanel = new GraphicsPanel(this);
        getContentPane().add(graphicsPanel);
        granulator = new BeadsGranulator(this, audioContext);
        controlHandler = new ControlHandler(this);

        new KeyHandler(GrainMainFrame.this);
        addKeyListener(new KeyAdapter() {

            @Override
            public void keyTyped(KeyEvent e) {
                System.out.println("key = " + e);
            }
        });

        droppedFiles = new ArrayList<File>();
        if (defaultFileOrFolder != null && defaultFileOrFolder.exists()) {
            droppedFiles.add(defaultFileOrFolder);
        }
        handleDroppedFiles(PlayWhichOne.Random);
        granulator.reset();

        boolean shouldStartPaused = false;
        granulator.setPaused(shouldStartPaused);

        new Thread() {

            @Override
            public void run() {
                oscHandler = new OSCHandler(GrainMainFrame.this);
            }
        }.start();

        setBounds(Utils.getBoundsFromPreferences(preferences, 500, 500));
        log.debug("bounds = " + getBounds());
        addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent e) {
                Utils.putBoundsInPreferences(GrainMainFrame.this.getBounds(), preferences);
                AllGrainMainFrames.remove(GrainMainFrame.this);
                GrainMainFrame.this.dispose();
                if (AllGrainMainFrames.size() == 0) {
                    handleExit();
                }
            }
        });
    }

    public static void main(String[] args) {
        PropertyConfigurator.configure("GrainMainFrameLog.properties");
        LoggingOutputStream elos = new LoggingOutputStream(Logger.getRootLogger(), Level.ERROR);
        System.setErr(new PrintStream(elos, true));
        log.debug("Starting...");
        Utils.logSystemProperties();
        Utils.setToSystemLookAndFeel();
        int spawnCount = 0;
        if (args != null && args.length > 0) {
            try {
                spawnCount = Integer.parseInt(args[0].trim());
            } catch (NumberFormatException ex) {
                log.error(ex, ex);
            }
        }

        GrainMainFrame grainMainFrame = new GrainMainFrame();
        grainMainFrame.setVisible(true);
        grainMainFrame.spawnChild(spawnCount);
    }

    public void handleExit() {
        if (granulator.isRecordingToFile()) {
            try {
                granulator.saveRecordedFile();
            } catch (Exception ex) {
                log.error(ex, ex);
            }
        }
        granulator.stopAudio();
        GrainMainFrame.this.dispose();
        if (!runningAsVST) {
            System.exit(0);
        }
    }

    public void spawnChild(int spawnCount) {
        for (int i = 0; i < spawnCount; i++) {
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    GrainMainFrame child = new GrainMainFrame();
//            child.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
                    child.setSize(GrainMainFrame.this.getSize());
                    child.setLocation((int) GrainMainFrame.this.getBounds().getMaxX(), GrainMainFrame.this.getY());
                    child.setVisible(true);
                }
            });
        }
    }

    public void arrangeFrames() {
        Utils.centerFrames(AllGrainMainFrames);
    }

    public List<File> getDroppedFiles() {
        return droppedFiles;
    }

    public enum PlayWhichOne {

        Random, First, Last;
    }

    public void handleDroppedFiles(PlayWhichOne playWhichOne) {
        if (droppedFiles == null || droppedFiles.size() == 0) {
            log.debug("droppedFiles =  " + droppedFiles);
            return;
        }

        File fileToPlay = null;
        if (playWhichOne == PlayWhichOne.First) {
            fileToPlay = droppedFiles.get(0);
        } else if (playWhichOne == PlayWhichOne.Last) {
            fileToPlay = droppedFiles.get(droppedFiles.size() - 1);
        } else {
            File[] fileArray = new File[droppedFiles.size()];
            for (int i = 0; i < droppedFiles.size(); i++) {
                fileArray[i] = droppedFiles.get(i);
            }
            fileToPlay = Utils.getRandomFile(fileArray);
        }

        if (fileToPlay == null || !Utils.sampleOrFolderFilter.accept(fileToPlay)) {
            log.debug("Can't play this: " + fileToPlay);
            return;
        }

        try {
            granulator.setPaused(true);
            granulator.setFileToGranulate(fileToPlay);
            granulator.startPlaying();
        } catch (Exception ex) {
            log.error(ex, ex);
            getGraphicsPanel().showFadeMessage("Unable to handle file.", true);
            return;
        }
    }

    public void setDroppedFiles(File[] droppedFiles) {
        ArrayList<File> arrayList = new ArrayList<File>();
        for (File file : droppedFiles) {
            arrayList.add(file);
        }
        setDroppedFiles(arrayList);
    }

    public void setDroppedFiles(List<File> droppedFiles) {
        this.droppedFiles = droppedFiles;
        handleDroppedFiles(PlayWhichOne.Random);
    }

    public Point getDndLocation() {
        return dndLocation;
    }

    public void setDndLocation(Point dndLocation) {
        this.dndLocation = dndLocation;
    }

    public File getFile() {
        return granulator.getFileToGranulate();
    }

    public float getCurrentGain() {
        return currentGain;
    }

    public void setCurrentGain(float currentGain) {
        this.currentGain = currentGain;
        granulator.setGain(currentGain);
        log.debug("currentGain = " + currentGain);
    }

    public float getMaxGain() {
        return maxGain;
    }

    public float getMaxGrainRate() {
        return maxGrainRate;
    }

    public int getMaxRandomness() {
        return maxRandomness;
    }

    public int getMaxDelayTime() {
        return maxDelayTime;
    }

    public float getMaxGrainIntervalRate() {
        return maxGrainIntervalRate;
    }

    public float getMaxPitchMultiplier() {
        return maxPitchMultiplier;
    }

    public boolean isChangingLoopPoints() {
        return changingLoopPoints;
    }

    public void setChangingLoopPoints(boolean changingLoopPoints) {
        this.changingLoopPoints = changingLoopPoints;
    }

    public GraphicsPanel getGraphicsPanel() {
        return graphicsPanel;
    }

    public ControlHandler getControlHandler() {
        return controlHandler;
    }

    public AbstractGranulator getGranulator() {
        return granulator;
    }

    public void addGranulatorObserver(GranulatorObserver granulatorObserver) {
        granulator.addObserver(granulatorObserver);
    }

    public void setGranulator(AbstractGranulator granulator) {
        this.granulator = granulator;
    }

    public int getPitchStepCount() {
        return pitchStepCount;
    }

    public void setPitchStepCount(int pitchStepCount) {
        this.pitchStepCount = pitchStepCount;
    }

    public int getDefaultEnvelopeTime() {
        return defaultEnvelopeTime;
    }

    public boolean isLockLoopEndPoint() {
        return lockLoopEndPoint;
    }

    public void setLockLoopEndPoint(boolean lockLoopEndPoint) {
        this.lockLoopEndPoint = lockLoopEndPoint;
        if (lockLoopEndPoint) {
            loopLengthWhenLocked = granulator.getEndRatio() - granulator.getStartRatio();
        }
    }

    public void setLoopPointsFraction(Point point) {
        if (point == null) {
            return;
        }
        float xRatio = (float) point.x / (float) graphicsPanel.getWidth();
        float yRatio = (float) point.y / (float) graphicsPanel.getHeight();
        if (lockLoopEndPoint) {
            yRatio = xRatio + loopLengthWhenLocked;
        }
        granulator.setLoopPointsFraction(xRatio, yRatio);
    }

    public void addGrainIntervalSegment(Point point) {
        if (point == null) {
            return;
        }
        float xRatio = (float) point.x / (float) graphicsPanel.getWidth();
        float yRatio = 1 - (float) point.y / (float) graphicsPanel.getHeight();
        granulator.addGrainIntervalSegment(xRatio * getMaxGrainIntervalRate(), defaultEnvelopeTime);
    }

    public void addRateEnvelopeSegment(Point point) {
        if (point == null) {
            return;
        }
        float xRatio = (float) point.x / (float) graphicsPanel.getWidth();
        float yRatio = 1 - (float) point.y / (float) graphicsPanel.getHeight();
        granulator.addRateEnvelopeSegment(xRatio * getMaxGrainRate(), defaultEnvelopeTime);
    }

    public void addRandomPanEnvelopeSegment(Point point) {
        if (point == null) {
            return;
        }
        float xRatio = (float) point.x / (float) graphicsPanel.getWidth();
        float yRatio = (float) point.y / (float) graphicsPanel.getHeight();
        granulator.addRandomPanEnvelopeSegment(xRatio * maxRandomness, defaultEnvelopeTime);
    }

    public void addRandomEnvelopeSegment(Point point) {
        if (point == null) {
            return;
        }
        float xRatio = (float) point.x / (float) graphicsPanel.getWidth();
        float yRatio = (float) point.y / (float) graphicsPanel.getHeight();
        granulator.addRandomEnvelopeSegment(yRatio * maxRandomness, defaultEnvelopeTime);
    }

    public void addPitchEnvelopeSegment(Point point) {
        if (point == null) {
            return;
        }
        float xRatio = (float) point.x / (float) graphicsPanel.getWidth();
        float yRatio = 1 - (float) point.y / (float) graphicsPanel.getHeight();
        granulator.addPitchEnvelopeSegment(yRatio * getMaxPitchMultiplier(), defaultEnvelopeTime);
    }

    public void setDelayParams(Point point) {
        if (point == null) {
            return;
        }
        float xRatio = (float) point.x / (float) graphicsPanel.getWidth();
        float yRatio = 1 - (float) point.y / (float) graphicsPanel.getHeight();
        granulator.setDelayTime(xRatio * getMaxDelayTime());
        if (xRatio < 0.5) {
            granulator.setDelayFeedback(yRatio);
        } else {
            granulator.setDelayWet(yRatio);
        }
    }

    public boolean isRunningAsVST() {
        return runningAsVST;
    }

    public void setRunningAsVST(boolean runningAsVST) {
        this.runningAsVST = runningAsVST;
    }
}
