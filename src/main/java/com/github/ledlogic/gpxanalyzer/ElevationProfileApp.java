package com.github.ledlogic.gpxanalyzer;

import java.util.List;

import javax.swing.SwingUtilities;

/**
 * Main application for loading GPS track files and displaying elevation profiles
 */
public class ElevationProfileApp {
    
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("GPS Elevation Profile Analyzer");
            System.out.println("==============================");
            System.out.println();
            System.out.println("Usage: java ElevationProfileApp <gpx-file> [options]");
            System.out.println();
            System.out.println("Options:");
            System.out.println("  --csv <file>     Export data to CSV file");
            System.out.println("  --no-gui         Don't display graphical plot");
            System.out.println();
            System.out.println("Examples:");
            System.out.println("  java ElevationProfileApp track.gpx");
            System.out.println("  java ElevationProfileApp track.gpx --csv output.csv");
            System.out.println("  java ElevationProfileApp track.gpx --no-gui --csv data.csv");
            return;
        }
        
        String gpxFile = args[0];
        String csvFile = null;
        boolean showGui = true;
        
        // Parse command line arguments
        for (int i = 1; i < args.length; i++) {
            if (args[i].equals("--csv") && i + 1 < args.length) {
                csvFile = args[++i];
            } else if (args[i].equals("--no-gui")) {
                showGui = false;
            }
        }
        
        try {
            System.out.println("Loading GPX file: " + gpxFile);
            System.out.println();
            
            List<TrackPoint> points = 
                GPXElevationProfile.parseGPX(gpxFile);
            
            System.out.println("Successfully loaded " + points.size() + " track points.");
            
            GPXElevationProfile.printStatistics(points);
            
            if (csvFile != null) {
                GPXElevationProfile.exportToCSV(points, csvFile);
                System.out.println("\nData exported to: " + csvFile);
            }
            
            if (showGui) {
                System.out.println("\nDisplaying elevation profile...");
                SwingUtilities.invokeLater(() -> {
                    ElevationPlotter.createAndShowGUI(points);
                });
            }
            
            // Print sample data
            System.out.println("\n=== Sample Data Points ===");
            System.out.println("Distance (km) | Altitude (m) | Segment Distance (m)");
            System.out.println("---------------------------------------------------");
            
            int sampleInterval = Math.max(1, points.size() / 10);
            for (int i = 0; i < points.size(); i += sampleInterval) {
                TrackPoint p = points.get(i);
                System.out.printf("%12.3f | %12.2f | %20.2f%n",
                                p.distanceFromStart / 1000.0,
                                p.altitude,
                                p.distanceFromPrevious);
            }
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
