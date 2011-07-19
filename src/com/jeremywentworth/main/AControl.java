package com.jeremywentworth.main;

import com.jeremywentworth.commons.Utils;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

/**
 *
 * @author Jeremy Wentworth
 */
public class AControl {

    private GraphicsPanel graphicsPanel;
    private double xRatio, yRatio;
    private boolean dragging;
    private double size = 35;
    private Color color = Utils.getRandomColor(50, 100);
    private String name = "Control";
    private String abreviation = "CT";
    private boolean showingFullName = true;
    private boolean visible = true;

    public AControl() {
    }

    public AControl(String name, String abreviation) {
        setName(name);
        setAbreviation(abreviation);
    }

    public void draw(Graphics2D graphics2D) {
        if (visible) {
            graphics2D.setColor(color);
            Shape shape = getRoundRectangle();
            graphics2D.fill(shape);
            graphics2D.draw(shape);

            if (graphicsPanel.isTextVisible()) {
                //boolean mouseIsOver = graphicsPanel.getMousePosition()!=null && shape.getBounds2D().contains(graphicsPanel.getMousePosition());
                graphics2D.setColor(Color.WHITE);
                Point.Double centerPoint = getCenterPoint();
                graphics2D.drawString(
                        showingFullName ? name : abreviation,
                        Utils.toFloat(centerPoint.getX() - getHalfSize() + 1),
                        Utils.toFloat(centerPoint.getY() + getHalfSize() - 4));
            }
        }
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public Ellipse2D.Double getEllipse() {
        Point.Double centerPoint = getCenterPoint();
        Ellipse2D.Double ellipse = new Ellipse2D.Double(
                centerPoint.getX() - getHalfSize(),
                centerPoint.getY() - getHalfSize(),
                size,
                size);
        return ellipse;
    }

    public Rectangle2D.Double getRectangle() {
        Point.Double centerPoint = getCenterPoint();
        Rectangle2D.Double rect = new Rectangle2D.Double(
                centerPoint.getX() - getHalfSize(),
                centerPoint.getY() - getHalfSize(),
                size,
                size);
        return rect;
    }

    public RoundRectangle2D.Double getRoundRectangle() {
        Point.Double centerPoint = getCenterPoint();
        RoundRectangle2D.Double rect = new RoundRectangle2D.Double(
                centerPoint.getX() - getHalfSize(),
                centerPoint.getY() - getHalfSize(),
                size,
                size, 3, 3);
        return rect;
    }

    public void setCenterPointRatios(double xRatio, double yRatio) {
        this.xRatio = xRatio;
        this.yRatio = yRatio;
    }

    public Point.Double getCenterPoint() {
        int borderPadding = Utils.toInt(getSize()) + 10;

        Point.Double centerPoint = new Point.Double(
                xRatio * graphicsPanel.getWidth(),
                yRatio * graphicsPanel.getHeight());

        if (centerPoint.getX() + borderPadding > graphicsPanel.getWidth()) {
            centerPoint.setLocation(graphicsPanel.getWidth() - borderPadding, centerPoint.getY());
        }
        if (centerPoint.getX() - borderPadding < 0) {
            centerPoint.setLocation(borderPadding, centerPoint.getY());
        }
        if (centerPoint.getY() + borderPadding > graphicsPanel.getHeight()) {
            centerPoint.setLocation(centerPoint.getX(), graphicsPanel.getHeight() - borderPadding);
        }
        if (centerPoint.getY() - borderPadding < 0) {
            centerPoint.setLocation(centerPoint.getX(), borderPadding);
        }
        return centerPoint;
    }

    public boolean isDragging() {
        return dragging;
    }

    public void setDragging(boolean dragging) {
        this.dragging = dragging;
    }

    public GraphicsPanel getGraphicsPanel() {
        return graphicsPanel;
    }

    public void setGraphicsPanel(GraphicsPanel graphicsPanel) {
        this.graphicsPanel = graphicsPanel;
    }

    public double getHalfSize() {
        return size / 2.0;
    }

    public double getSize() {
        return size;
    }

    public void setSize(double size) {
        this.size = size;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAbreviation() {
        return abreviation;
    }

    public void setAbreviation(String abreviation) {
        this.abreviation = abreviation;
    }

    public boolean isShowingFullName() {
        return showingFullName;
    }

    public void setShowingFullName(boolean showingFullName) {
        this.showingFullName = showingFullName;
    }
}
