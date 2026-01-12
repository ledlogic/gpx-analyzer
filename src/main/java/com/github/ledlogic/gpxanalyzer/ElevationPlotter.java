package com.github.ledlogic.gpxanalyzer;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.io.Serializable;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * Simple plotting component for elevation profiles
 */
public class ElevationPlotter extends JPanel implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private List<TrackPoint> points;
    private static final int PADDING = 60;
    
    public ElevationPlotter(List<TrackPoint> points) {
        this.points = points;
        setPreferredSize(new Dimension(1000, 600));
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                           RenderingHints.VALUE_ANTIALIAS_ON);
        
        if (points == null || points.isEmpty()) {
            g2.drawString("No data to display", getWidth() / 2 - 50, getHeight() / 2);
            return;
        }
        
        // Calculate min/max values
        double minDist = 0;
        double maxDist = points.get(points.size() - 1).distanceFromStart;
        double minAlt = Double.MAX_VALUE;
        double maxAlt = Double.MIN_VALUE;
        
        for (TrackPoint point : points) {
            minAlt = Math.min(minAlt, point.altitude);
            maxAlt = Math.max(maxAlt, point.altitude);
        }
        
        // Add some padding to altitude range
        double altRange = maxAlt - minAlt;
        minAlt -= altRange * 0.1;
        maxAlt += altRange * 0.1;
        
        // Draw axes
        g2.setColor(Color.BLACK);
        g2.setStroke(new BasicStroke(2));
        
        // Y-axis
        g2.drawLine(PADDING, PADDING, PADDING, getHeight() - PADDING);
        // X-axis
        g2.drawLine(PADDING, getHeight() - PADDING, 
                   getWidth() - PADDING, getHeight() - PADDING);
        
        // Draw grid and labels
        drawGrid(g2, minDist, maxDist, minAlt, maxAlt);
        
        // Draw elevation profile
        g2.setColor(new Color(70, 130, 180));
        g2.setStroke(new BasicStroke(2));
        
        Path2D path = new Path2D.Double();
        boolean first = true;
        
        for (TrackPoint point : points) {
            double x = scaleX(point.distanceFromStart, minDist, maxDist);
            double y = scaleY(point.altitude, minAlt, maxAlt);
            
            if (first) {
                path.moveTo(x, y);
                first = false;
            } else {
                path.lineTo(x, y);
            }
        }
        
        g2.draw(path);
        
        // Fill area under the curve
        Path2D fillPath = new Path2D.Double(path);
        fillPath.lineTo(scaleX(maxDist, minDist, maxDist), getHeight() - PADDING);
        fillPath.lineTo(scaleX(minDist, minDist, maxDist), getHeight() - PADDING);
        fillPath.closePath();
        
        g2.setColor(new Color(70, 130, 180, 50));
        g2.fill(fillPath);
        
        // Draw title
        g2.setColor(Color.BLACK);
        g2.setFont(new Font("Arial", Font.BOLD, 16));
        String title = "Elevation Profile";
        FontMetrics fm = g2.getFontMetrics();
        int titleWidth = fm.stringWidth(title);
        g2.drawString(title, (getWidth() - titleWidth) / 2, 30);
    }
    
    private void drawGrid(Graphics2D g2, double minDist, double maxDist, 
                         double minAlt, double maxAlt) {
        g2.setColor(Color.LIGHT_GRAY);
        g2.setStroke(new BasicStroke(1));
        Font labelFont = new Font("Arial", Font.PLAIN, 10);
        g2.setFont(labelFont);
        
        // Draw horizontal grid lines and Y-axis labels (altitude)
        int numYDivisions = 8;
        for (int i = 0; i <= numYDivisions; i++) {
            double alt = minAlt + (maxAlt - minAlt) * i / numYDivisions;
            int y = (int) scaleY(alt, minAlt, maxAlt);
            
            // Grid line
            g2.drawLine(PADDING, y, getWidth() - PADDING, y);
            
            // Label
            String label = String.format("%.0f m", alt);
            FontMetrics fm = g2.getFontMetrics();
            int labelWidth = fm.stringWidth(label);
            g2.setColor(Color.BLACK);
            g2.drawString(label, PADDING - labelWidth - 10, y + 5);
            g2.setColor(Color.LIGHT_GRAY);
        }
        
        // Y-axis title
        g2.setColor(Color.BLACK);
        g2.setFont(new Font("Arial", Font.BOLD, 12));
        AffineTransform orig = g2.getTransform();
        g2.rotate(-Math.PI / 2);
        g2.drawString("Altitude (meters)", -getHeight() / 2 - 50, 20);
        g2.setTransform(orig);
        g2.setFont(labelFont);
        
        // Draw vertical grid lines and X-axis labels (distance)
        int numXDivisions = 10;
        for (int i = 0; i <= numXDivisions; i++) {
            double dist = minDist + (maxDist - minDist) * i / numXDivisions;
            int x = (int) scaleX(dist, minDist, maxDist);
            
            // Grid line
            g2.setColor(Color.LIGHT_GRAY);
            g2.drawLine(x, PADDING, x, getHeight() - PADDING);
            
            // Label
            String label = String.format("%.1f km", dist / 1000.0);
            FontMetrics fm = g2.getFontMetrics();
            int labelWidth = fm.stringWidth(label);
            g2.setColor(Color.BLACK);
            g2.drawString(label, x - labelWidth / 2, getHeight() - PADDING + 20);
        }
        
        // X-axis title
        g2.setFont(new Font("Arial", Font.BOLD, 12));
        String xTitle = "Distance (kilometers)";
        FontMetrics fm = g2.getFontMetrics();
        int xTitleWidth = fm.stringWidth(xTitle);
        g2.drawString(xTitle, (getWidth() - xTitleWidth) / 2, getHeight() - 10);
    }
    
    private double scaleX(double value, double min, double max) {
        double range = max - min;
        if (range == 0) return PADDING;
        return PADDING + (value - min) / range * (getWidth() - 2 * PADDING);
    }
    
    private double scaleY(double value, double min, double max) {
        double range = max - min;
        if (range == 0) return getHeight() - PADDING;
        return getHeight() - PADDING - (value - min) / range * (getHeight() - 2 * PADDING);
    }
    
    public static void createAndShowGUI(List<TrackPoint> points) {
        JFrame frame = new JFrame("Elevation Profile");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        ElevationPlotter plotter = new ElevationPlotter(points);
        frame.add(plotter);
        
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
