package com.github.ledlogic.gpxanalyzer;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.imageio.ImageIO;
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
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.ZoneId;
import java.util.List;

/**
 * Simple plotting component for elevation profiles
 * 
 * Plots altitude (Y-axis) vs cumulative distance traveled along the path (X-axis).
 * This is NOT the straight-line distance from start, but the actual path distance
 * (s-distance curve) calculated by summing distances between consecutive GPS points.
 * Points are displayed in chronological order based on timestamps if available.
 */
public class ElevationPlotter extends JPanel implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private List<TrackPoint> points;
    private static final int PADDING = 60;
    private String filename; // Name to display in title
    
    public ElevationPlotter(List<TrackPoint> points, String filename) {
        this.points = points;
        this.filename = filename;
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
        
        // Draw data points and timestamps
        drawDataPointsAndTimestamps(g2, minDist, maxDist, minAlt, maxAlt);
        
        // Draw title
        g2.setColor(Color.BLACK);
        g2.setFont(new Font("Arial", Font.BOLD, 16));
        String title = filename != null && !filename.isEmpty() 
                      ? filename 
                      : "Elevation Profile";
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
            String label = String.format("%.2f m", alt);
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
        boolean useMeters = maxDist < 1000; // Use meters if total distance < 1km
        
        for (int i = 0; i <= numXDivisions; i++) {
            double dist = minDist + (maxDist - minDist) * i / numXDivisions;
            int x = (int) scaleX(dist, minDist, maxDist);
            
            // Grid line
            g2.setColor(Color.LIGHT_GRAY);
            g2.drawLine(x, PADDING, x, getHeight() - PADDING);
            
            // Label - format based on distance
            String label;
            if (useMeters) {
                label = String.format("%.2f m", dist);
            } else {
                label = String.format("%.2f km", dist / 1000.0);
            }
            FontMetrics fm = g2.getFontMetrics();
            int labelWidth = fm.stringWidth(label);
            g2.setColor(Color.BLACK);
            g2.drawString(label, x - labelWidth / 2, getHeight() - PADDING + 20);
        }
        
        // X-axis title - adjust based on units
        g2.setFont(new Font("Arial", Font.BOLD, 12));
        String xTitle = useMeters ? "Distance (meters)" : "Distance (kilometers)";
        FontMetrics fm = g2.getFontMetrics();
        int xTitleWidth = fm.stringWidth(xTitle);
        g2.drawString(xTitle, (getWidth() - xTitleWidth) / 2, getHeight() - 10);
    }
    
    private void drawDataPointsAndTimestamps(Graphics2D g2, double minDist, double maxDist,
                                             double minAlt, double maxAlt) {
        if (points == null || points.isEmpty()) {
            return;
        }
        
        // Calculate which points to show (avoid overcrowding)
        int pointsToShow = Math.min(points.size(), 15); // Max 15 timestamps for readability
        int interval = Math.max(1, points.size() / pointsToShow);
        
        // Draw dots and timestamps for selected points
        g2.setFont(new Font("Arial", Font.BOLD, 11)); // Larger, bold font
        
        for (int i = 0; i < points.size(); i += interval) {
            TrackPoint point = points.get(i);
            
            double x = scaleX(point.distanceFromStart, minDist, maxDist);
            double y = scaleY(point.altitude, minAlt, maxAlt);
            
            // Draw larger dot at data point
            g2.setColor(new Color(70, 130, 180));
            g2.fillOval((int)(x - 4), (int)(y - 4), 8, 8);
            
            // Draw timestamp and elevation if available
            if (point.timestamp != null) {
                String timeStr = formatTimeWithElevation(point.timestamp, point.altitude);
                
                // Draw background for better readability
                FontMetrics fm = g2.getFontMetrics();
                int textWidth = fm.stringWidth(timeStr);
                int textHeight = fm.getHeight();
                
                // Position text above point
                int textX = (int)x - textWidth / 2;
                int textY = (int)y - 12; // Further above point
                
                // If near top of chart, place below instead
                if (textY - textHeight < PADDING + 10) {
                    textY = (int)y + textHeight + 8;
                }
                
                // Draw semi-transparent white background (no border)
                g2.setColor(new Color(255, 255, 255, 220));
                g2.fillRect(textX - 3, textY - textHeight + 2, textWidth + 6, textHeight + 2);
                
                // Draw text in dark color (no border around box)
                g2.setColor(new Color(0, 0, 0));
                g2.drawString(timeStr, textX, textY);
            }
        }
        
        // Always show first and last point if we have timestamps
        if (points.size() > 1 && points.get(0).timestamp != null) {
            // Ensure first point is marked with GREEN
            TrackPoint first = points.get(0);
            double x = scaleX(first.distanceFromStart, minDist, maxDist);
            double y = scaleY(first.altitude, minAlt, maxAlt);
            g2.setColor(new Color(0, 180, 0)); // Bright green for start
            g2.fillOval((int)(x - 5), (int)(y - 5), 10, 10);
            g2.setColor(new Color(0, 100, 0)); // Dark green outline
            g2.drawOval((int)(x - 5), (int)(y - 5), 10, 10);
            
            // Ensure last point is marked with RED
            TrackPoint last = points.get(points.size() - 1);
            x = scaleX(last.distanceFromStart, minDist, maxDist);
            y = scaleY(last.altitude, minAlt, maxAlt);
            g2.setColor(new Color(255, 0, 0)); // Bright red for end
            g2.fillOval((int)(x - 5), (int)(y - 5), 10, 10);
            g2.setColor(new Color(150, 0, 0)); // Dark red outline
            g2.drawOval((int)(x - 5), (int)(y - 5), 10, 10);
        }
    }
    
    private String formatTimeWithElevation(Instant timestamp, double altitude) {
        // Format as "elevation @ HH:MM" in local time zone
        ZonedDateTime zdt = timestamp.atZone(ZoneId.systemDefault());
        int hour = zdt.getHour();
        String ampm = hour < 12 ? "am" : "pm";
        int displayHour = hour == 0 ? 12 : (hour > 12 ? hour - 12 : hour);
        return String.format("%.2f m @ %d:%02d %s", altitude, displayHour, zdt.getMinute(), ampm);
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
    
    public static void createAndShowGUI(List<TrackPoint> points, String filename) {
        JFrame frame = new JFrame("Elevation Profile - " + filename);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        ElevationPlotter plotter = new ElevationPlotter(points, filename);
        frame.add(plotter);
        
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
    
    /**
     * Saves the elevation profile plot to a PNG file
     * @param points The track points to plot
     * @param filename The name to display in the title
     * @param outputPath The output file path (should end with .png)
     * @throws IOException if the file cannot be written
     */
    public static void saveToPNG(List<TrackPoint> points, String filename, String outputPath) 
            throws IOException {
        // Create the plotter component
        ElevationPlotter plotter = new ElevationPlotter(points, filename);
        
        // Set size (same as GUI)
        int width = 1000;
        int height = 600;
        plotter.setSize(width, height);
        
        // Create a BufferedImage to draw on
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        
        // Enable antialiasing for better quality
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                           RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                           RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        // Paint the plotter onto the image
        plotter.paint(g2);
        g2.dispose();
        
        // Save the image as PNG
        File outputFile = new File(outputPath);
        ImageIO.write(image, "png", outputFile);
    }
}
