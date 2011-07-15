/*
 * MainPanel.java
 *
 * Created on Mar 20, 2010, 10:58:33 AM
 */
package com.jeremywentworth.main;

import com.jeremywentworth.audio.GranulatorInterface;
import com.jeremywentworth.audio.SampleLoopType;
import com.jeremywentworth.commons.AudioUtilities;
import com.jeremywentworth.commons.FileDrop;
import com.jeremywentworth.commons.TransferableFileList;
import com.jeremywentworth.commons.Utils;
import java.awt.*;
import java.awt.dnd.DragGestureEvent;
import java.awt.geom.*;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import javax.imageio.ImageIO;
import javax.swing.border.EmptyBorder;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.CharUtils;
import org.apache.log4j.Logger;

/**
 * @author Jeremy Wentworth
 */
public class GraphicsPanel extends javax.swing.JPanel implements DragGestureListener {

    private GrainMainFrame grainMainFrame;
    private int recordPercent = 0;//0%-100%
    private boolean recordPercentIncreasing = true;
    private boolean textVisible = true;
    private boolean helpVisible = false;
    private boolean fadeMessageIsError = false;
    private int fadeMessageOpacityPercent = 0;//0%-100%
    private String fadeMessage = "";
    private Color dragOverColor = new Color(30, 150, 255);//light blue
    private Font textFont = new Font("Arial", Font.PLAIN, 9);
    private MouseHandler mainPanelMouse;
    private FileDrop fileDrop;
    private Rectangle2D webSiteLinkBounds;
    private Rectangle2D recordedFileStringRect;
    private Rectangle2D currentSampleStringRect;
    private Polygon currentWaveForm;
    private File fileWhenWaveformMade;
    private Rectangle2D panelSizeWhenWaveformMade;
    private TransferableFileList lastDragFileList;
    private static Logger log = Logger.getLogger(GraphicsPanel.class);

    public GraphicsPanel(GrainMainFrame grainMainFrame) {
        this.grainMainFrame = grainMainFrame;
        initComponents();
        mainPanelMouse = new MouseHandler(grainMainFrame);
        addMouseListener(mainPanelMouse);
        addMouseMotionListener(mainPanelMouse);
        addMouseWheelListener(mainPanelMouse);
        setLayout(null);
        setupFileDrop();
        startRenderThread();
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        Graphics2D graphics2D = (Graphics2D) g;
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        drawBackground(graphics2D);

        drawRecordGraphics(graphics2D);
        drawFadeMessage(graphics2D);
        drawSampleName(graphics2D);

        if (grainMainFrame.getFile() == null) {
            setHelpVisible(true);
        } else {
            if (grainMainFrame.isChangingLoopPoints()) {
//                drawGuideLines(graphics2D);
            }
            drawPositionLines(graphics2D);
            grainMainFrame.getControlHandler().draw(graphics2D);
        }

        if (fileDrop.isDraggingOver()) {
            Rectangle2D dragOverBorder = new Rectangle2D.Double(1, 1, getWidth() - 3, getHeight() - 3);
            graphics2D.setStroke(new BasicStroke(4));
            graphics2D.setColor(dragOverColor);
            graphics2D.draw(dragOverBorder);
        }

        if (helpVisible) {
            drawHelp(graphics2D);
            drawWebSite(graphics2D);
        }

        setCursorType();
    }

    private void setCursorType() {
        if (getMousePosition() != null) {
            boolean mouseOverSample = currentSampleStringRect != null && currentSampleStringRect.contains(getMousePosition());
            boolean mouseOverRecording = recordedFileStringRect != null && recordedFileStringRect.contains(getMousePosition());
            boolean mouseOverWebSite = isWebSiteClickable(getMousePosition());
            if (mouseOverSample || mouseOverRecording || mouseOverWebSite) {
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            } else {
                setCursor(Cursor.getDefaultCursor());
            }
        }
    }

    private void drawBackground(Graphics2D graphics2D) {
        int width = getWidth();
        int height = getHeight();

        Point2D gradientPoint1 = new Point2D.Float(Utils.toFloat(0), Utils.toFloat(0));
        Point2D gradientPoint2 = new Point2D.Float(Utils.toFloat(0), Utils.toFloat(height));

        Color gradientColor1 = new Color(75, 75, 75);//Color.LIGHT_GRAY;
        Color gradientColor2 = new Color(210, 210, 240);//Color.BLACK;

        if (mainPanelMouse.isRecordingMouseEvents()) {
            gradientColor1 = new Color(75, 0, 0);//Color.RED;
        } else if (mainPanelMouse.isPlayingMouseEvents()) {
            gradientColor1 = new Color(0, 75, 0);//Color.GREEN;
        }

        GradientPaint gradientPaint = new GradientPaint(gradientPoint2, gradientColor2, gradientPoint1, gradientColor1, true);
        graphics2D.setPaint(gradientPaint);

        Rectangle2D rectangle2D = new Rectangle2D.Double(0, 0, width, height);
        graphics2D.fill(rectangle2D);
        graphics2D.draw(rectangle2D);
    }

    private void drawRecordGraphics(Graphics2D graphics2D) {
        if (grainMainFrame.getGranulator().isRecordingToFile()) {

            if (recordPercentIncreasing) {
                if (recordPercent < 50) {
                    recordPercent++;
                } else {
                    recordPercentIncreasing = false;
                }
            } else {
                if (recordPercent > 0) {
                    recordPercent--;
                } else {
                    recordPercentIncreasing = true;
                }
            }

            double recordRatio = recordPercent / 100.0;
            Composite compositeBefore = graphics2D.getComposite();
            AlphaComposite alpha = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.13f);
            graphics2D.setComposite(alpha);

            /**
             * Idea
             * Make record ball follow an arc
             * from sample file name to recorded file name
             * and increase diameter in the center of the window.
             */
            int diameter = Utils.toInt(Math.min(getWidth(), getHeight()) * 0.4);

            int centerX = Utils.toInt((getWidth() - diameter) * 0.5);
            int centerY = Utils.toInt((getHeight() - diameter) * 0.5);

            Ellipse2D ellipse2D = new Ellipse2D.Double(centerX, centerY, diameter, diameter);

            Point2D centerPoint = new Point2D.Float(Utils.toFloat(ellipse2D.getCenterX()), Utils.toFloat(ellipse2D.getCenterY()));
            Point2D outsidePoint = new Point2D.Float(Utils.toFloat(ellipse2D.getCenterX()), Utils.toFloat(ellipse2D.getMinY()));

            Color gradientColor1 = new Color(120, 0, 0);//red
            Color gradientColor2 = new Color(175, 175, 175);//gray
            GradientPaint recordWavGradientPaint = new GradientPaint(outsidePoint, gradientColor2, centerPoint, gradientColor1, true);
            graphics2D.setPaint(recordWavGradientPaint);
            graphics2D.fill(ellipse2D);
            graphics2D.draw(ellipse2D);

            graphics2D.setComposite(compositeBefore);

            try {
                graphics2D.setFont(textFont);
                graphics2D.setColor(gradientColor1);
                String path = "Recording: " + grainMainFrame.getGranulator().getFileRecordingTo().getCanonicalPath();
                Utils.drawAlignedString(graphics2D, path, getHeight() - 5, Utils.Alignment.Left);
            } catch (IOException ex) {
                log.error(ex, ex);
            }
        } else if (grainMainFrame.getGranulator().isFileRecorded()) {
            try {
                Color gradientColor1 = new Color(120, 0, 0);//red
                graphics2D.setFont(textFont);
                graphics2D.setColor(gradientColor1);
                String path = "(Drag) Recorded: " + grainMainFrame.getGranulator().getFileRecordingTo().getCanonicalPath();
                recordedFileStringRect = Utils.drawAlignedString(graphics2D, path, getHeight() - 5, Utils.Alignment.Left);
            } catch (IOException ex) {
                log.error(ex, ex);
            }
        } else {
            recordPercent = 0;
        }
    }

    private void drawGuideLines(Graphics2D graphics2D) {
        GranulatorInterface granulator = grainMainFrame.getGranulator();
        int endLinesColor = Utils.toInt(Math.abs(granulator.getPositionRatio()) * 255) % 255;
        graphics2D.setColor(new Color(endLinesColor, endLinesColor, endLinesColor));
        graphics2D.setStroke(new BasicStroke(4, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        AlphaComposite alpha = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.03f);
        Composite compositeBefore = graphics2D.getComposite();
        graphics2D.setComposite(alpha);
        int offset = 10;
        graphics2D.draw(new Line2D.Double(offset, offset, getWidth() - offset, getHeight() - offset));
        graphics2D.draw(new Line2D.Double(getWidth() - offset, offset, offset, getHeight() - offset));
        graphics2D.draw(new Line2D.Double(offset, getHeight() / 2, getWidth() - offset, getHeight() / 2));
        graphics2D.setComposite(compositeBefore);
    }

    private void drawPositionLines(Graphics2D graphics2D) {
        GranulatorInterface granulator = grainMainFrame.getGranulator();

        int endLinesColor = Utils.toInt(Math.abs(granulator.getPositionRatio()) * 255) % 255;
        graphics2D.setColor(new Color(endLinesColor, endLinesColor, endLinesColor));
        graphics2D.setStroke(new BasicStroke(4, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        double startX = granulator.getStartRatio() * getWidth();
        double posX = granulator.getPositionRatio() * getWidth();
        double endX = granulator.getEndRatio() * getWidth();
        double bufferLevelY = getHeight() / 2.0 + (granulator.getAudioOutValue() * 5) * (getHeight() / 2.0);

        graphics2D.draw(new Line2D.Double(startX, 4, startX, getHeight() - 4));
        int mult = 1;

        graphics2D.draw(new Line2D.Double(endX, 4, endX, getHeight() - 4));
        graphics2D.draw(new Line2D.Double(posX, 4, posX, getHeight() - 4));

        double yOffset = bufferLevelY;//getHeight() / 2.0
        if (granulator.getLoopType() == SampleLoopType.Forward
                || granulator.getLoopType() == SampleLoopType.Alternating) {
            //triangle indicator
            graphics2D.draw(new Line2D.Double(posX + 1 * mult, yOffset - 4, posX + 4 * mult, yOffset));
            graphics2D.draw(new Line2D.Double(posX + 1 * mult, yOffset + 4, posX + 4 * mult, yOffset));
        }
        if (granulator.getLoopType() == SampleLoopType.Backward || granulator.getLoopType() == SampleLoopType.Alternating) {
            //triangle indicator
            graphics2D.draw(new Line2D.Double(posX - 1 * mult, yOffset - 4, posX - 4 * mult, yOffset));
            graphics2D.draw(new Line2D.Double(posX - 1 * mult, yOffset + 4, posX - 4 * mult, yOffset));
        }


        //Gain level on pos marker
//        mainFrame.getControlList().setGainLevel(posX, bufferLevelY);
    }

    private void drawSampleName(Graphics2D graphics2D) {
        try {
            int width = getWidth();
            int height = getHeight();

            graphics2D.setFont(textFont);
            graphics2D.setColor(Color.LIGHT_GRAY);
            int textY = 10;//height - 5 (for bottom)

            if (grainMainFrame.getFile() == null) {

                Utils.drawAlignedString(graphics2D, "Drop Files In Window", textY, Utils.Alignment.Center);
                currentSampleStringRect = null;

            } else {

                currentSampleStringRect = Utils.drawAlignedString(graphics2D, "(Drag) Sample: " + grainMainFrame.getFile().getCanonicalPath(), textY, Utils.Alignment.Left);
                boolean fileChanged = fileWhenWaveformMade == null || !fileWhenWaveformMade.equals(grainMainFrame.getFile());
                boolean windowSizeChanged = panelSizeWhenWaveformMade == null || !panelSizeWhenWaveformMade.equals(this.getBounds());
                if (currentWaveForm == null || fileChanged || windowSizeChanged) {
                    fileWhenWaveformMade = grainMainFrame.getFile();
                    panelSizeWhenWaveformMade = this.getBounds();
                    currentWaveForm = AudioUtilities.createWaveForm(fileWhenWaveformMade, getWidth(), getHeight());
                }
                graphics2D.draw(currentWaveForm);

            }
        } catch (Exception ex) {
            log.error(ex, ex);
        }
    }

    private void drawFadeMessage(Graphics2D graphics2D) {
        if (fadeMessageOpacityPercent > 0) {
            Composite compositeBefore = graphics2D.getComposite();
            Font fontBefore = graphics2D.getFont();

            float opacityRatio = fadeMessageOpacityPercent / 100.0f;
            AlphaComposite alpha = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacityRatio * 0.40f);
            graphics2D.setComposite(alpha);

            if (fadeMessageIsError) {
                graphics2D.setColor(Color.YELLOW);
            } else {
                graphics2D.setColor(dragOverColor);
            }
            int heightVal = Utils.toInt(getHeight() * (1 - opacityRatio) + (getHeight() * 0.40));
            Font fadeFont = new Font(textFont.getName(), Font.BOLD, Utils.toInt(getWidth() * 0.03));
            graphics2D.setFont(fadeFont);
            Utils.drawAlignedString(graphics2D, fadeMessage, heightVal, Utils.Alignment.Center);
//            double width = getHeight() * 0.10;
//            graphics2D.setStroke(new BasicStroke((int) width, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
//            graphics2D.draw(new Line2D.Double(getWidth() / 2, getHeight() * 0.20, getWidth() / 2, getHeight() * 0.60));
//            graphics2D.draw(new Line2D.Double(getWidth() / 2, getHeight() * 0.60 + width + 5, getWidth() / 2, getHeight() * 0.60 + (width * 1.5)));
            graphics2D.setComposite(compositeBefore);
            graphics2D.setFont(fontBefore);
            fadeMessageOpacityPercent -= 2;
        } else {
            fadeMessageOpacityPercent = 0;
        }
    }

    private void drawHelp(Graphics2D graphics2D) {
        // gets the current clipping area
        Rectangle clip = graphics2D.getClipBounds();
        AlphaComposite alpha = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.75f);
        Composite compositeBefore = graphics2D.getComposite();
        graphics2D.setComposite(alpha);

        // fills the background
        graphics2D.setColor(Color.BLACK);
        graphics2D.fillRect(clip.x, clip.y, clip.width, clip.height);
        graphics2D.setColor(Color.WHITE);
        int currentY = 10;
        int lineSpacingY = 12;
        int indentX = 10;
        graphics2D.setFont(textFont);

        try {
            java.util.List readLines = FileUtils.readLines(new File(GrainMainFrame.HELP_FILE_NAME));
            for (Object object : readLines) {
                String string = (String) object;
                boolean firstLetterUpperCase = string.length() > 0 && CharUtils.isAsciiAlphaUpper(string.charAt(0));
                graphics2D.drawString(string, indentX * (firstLetterUpperCase ? 2 : 3), currentY += lineSpacingY);
            }
        } catch (IOException ex) {
            log.error(ex, ex);
        }

        try {
            BufferedImage image = ImageIO.read(getClass().getResourceAsStream("MyLogoSimple100.png"));
            graphics2D.drawImage(image, getWidth() - 100, (getHeight() - image.getHeight()) / 2, null);
        } catch (IOException ex) {
            log.error(ex, ex);
        }

        graphics2D.setComposite(compositeBefore);
    }

    private void drawWebSite(Graphics2D graphics2D) {
        int width = getWidth();
        int height = getHeight();

        Color webSiteColor = new Color(8, 151, 255);
        if (isWebSiteClickable(getMousePosition())) {
            webSiteColor = new Color(58, 201, 255);
        }

        graphics2D.setColor(webSiteColor);
        graphics2D.setFont(textFont);
        webSiteLinkBounds = Utils.drawAlignedString(graphics2D, GrainMainFrame.WEB_SITE, height - 30, Utils.Alignment.Center);

        if (isWebSiteClickable(getMousePosition())) {
            graphics2D.setStroke(new BasicStroke(1));
            Line2D line2D = new Line2D.Double(
                    webSiteLinkBounds.getX(),
                    webSiteLinkBounds.getMaxY(),
                    webSiteLinkBounds.getMaxX(),
                    webSiteLinkBounds.getMaxY());
            graphics2D.draw(line2D);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        } else {
            setCursor(Cursor.getDefaultCursor());
        }
    }

    private void startRenderThread() {
        Thread renderThread = new Thread() {

            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(50);
                        repaint();
                    } catch (InterruptedException ex) {
                        //ignore
                    }
                }
            }
        };
        renderThread.start();
    }

    public boolean isTextVisible() {
        return textVisible;
    }

    public void setTextVisible(boolean textVisible) {
        this.textVisible = textVisible;
        this.repaint();
    }

    public boolean isWebSiteClickable(Point mouseLocation) {
        if (mouseLocation == null || !helpVisible || webSiteLinkBounds == null) {
            return false;
        }
        return webSiteLinkBounds.contains(mouseLocation);
    }

    public boolean isHelpVisible() {
        return helpVisible;
    }

    public void setHelpVisible(boolean helpVisible) {
        this.helpVisible = helpVisible;
    }

    public MouseHandler getMainPanelMouse() {
        return mainPanelMouse;
    }

    public void showFadeMessage(String message, boolean isError) {
        this.fadeMessageOpacityPercent = 100;
        this.fadeMessageIsError = isError;
        this.fadeMessage = message;
    }

    private void setupFileDrop() {
        FileDrop.Listener listener = new FileDrop.Listener() {

            @Override
            public void filesDropped(File[] files) {
                if (lastDragFileList != null && Arrays.asList(files).containsAll(lastDragFileList)) {
                    return;
                }
                GraphicsPanel.this.grainMainFrame.setDroppedFiles(files);
            }
        };
        EmptyBorder emptyBorder = new EmptyBorder(0, 0, 0, 0);
        fileDrop = new FileDrop(this, emptyBorder, listener);
        new DragSource().createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_COPY, this);

    }

    @Override
    public void dragGestureRecognized(DragGestureEvent dge) {
        if (recordedFileStringRect != null && recordedFileStringRect.contains(dge.getDragOrigin())) {
            lastDragFileList = new TransferableFileList();
            lastDragFileList.add(grainMainFrame.getGranulator().getFileRecordingTo());
            dge.startDrag(null, lastDragFileList);
        } else if (currentSampleStringRect != null && currentSampleStringRect.contains(dge.getDragOrigin())) {
            lastDragFileList = new TransferableFileList();
            lastDragFileList.add(grainMainFrame.getGranulator().getFileToGranulate());
            dge.startDrag(null, lastDragFileList);
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setLayout(new java.awt.GridLayout());
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
