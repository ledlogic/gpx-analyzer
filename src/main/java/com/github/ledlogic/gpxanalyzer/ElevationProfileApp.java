package com.github.ledlogic.gpxanalyzer;

import javax.swing.SwingUtilities;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Main application for loading GPS track files and displaying elevation profiles
 * Can process single files or entire directories of GPX files
 */
public class ElevationProfileApp {
    
    public static void main(String[] args) {
        if (args.length < 1) {
            printUsage();
            return;
        }
        
        String inputPath = args[0];
        boolean showGui = true;
        
        // Parse command line arguments
        for (int i = 1; i < args.length; i++) {
            if (args[i].equals("--no-gui")) {
                showGui = false;
            }
        }
        
        File input = new File(inputPath);
        
        if (!input.exists()) {
            System.err.println("Error: File or directory does not exist: " + inputPath);
            System.exit(1);
        }
        
        List<File> gpxFiles = new ArrayList<>();
        String outputDirectory; // Directory where CSVs and PNGs will be saved
        
        // Determine if input is a file or directory
        if (input.isDirectory()) {
            System.out.println("Processing directory: " + inputPath);
            File[] files = input.listFiles((dir, name) -> 
                name.toLowerCase().endsWith(".gpx"));
            
            if (files == null || files.length == 0) {
                System.err.println("Error: No GPX files found in directory: " + inputPath);
                System.exit(1);
            }
            
            Arrays.sort(files); // Sort files alphabetically
            gpxFiles.addAll(Arrays.asList(files));
            outputDirectory = input.getAbsolutePath();
            System.out.println("Found " + gpxFiles.size() + " GPX file(s)");
        } else {
            // Single file
            if (!inputPath.toLowerCase().endsWith(".gpx")) {
                System.err.println("Error: File must have .gpx extension: " + inputPath);
                System.exit(1);
            }
            gpxFiles.add(input);
            outputDirectory = input.getParent() != null ? input.getParent() : ".";
        }
        
        System.out.println("Output directory for CSV and PNG files: " + outputDirectory);
        System.out.println();
        
        // Process each GPX file
        final boolean finalShowGui = showGui;
        final String finalOutputDirectory = outputDirectory;
        
        for (File gpxFile : gpxFiles) {
            try {
                System.out.println("=".repeat(60));
                System.out.println("Processing: " + gpxFile.getName());
                System.out.println("=".repeat(60));
                
                List<TrackPoint> points = GPXElevationProfile.parseGPX(
                    gpxFile.getAbsolutePath());
                
                System.out.println("Successfully loaded " + points.size() + " track points.");
                
                GPXElevationProfile.printStatistics(points);
                
                String baseFilename = gpxFile.getName().replace(".gpx", "");
                
                // Always export to CSV
                String csvPath = new File(finalOutputDirectory, baseFilename + ".csv").getPath();
                GPXElevationProfile.exportToCSV(points, csvPath);
                System.out.println("Data exported to: " + csvPath);
                
                // Always save PNG plot
                String pngPath = new File(finalOutputDirectory, baseFilename + ".png").getPath();
                ElevationPlotter.saveToPNG(points, baseFilename, pngPath);
                System.out.println("Plot saved to: " + pngPath);
                
                // Display GUI if requested
                if (finalShowGui) {
                    final List<TrackPoint> finalPoints = points;
                    
                    SwingUtilities.invokeLater(() -> {
                        ElevationPlotter.createAndShowGUI(finalPoints, baseFilename);
                    });
                    
                    // Small delay between windows for multiple files
                    if (gpxFiles.size() > 1) {
                        Thread.sleep(300);
                    }
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
                System.out.println();
                
            } catch (Exception e) {
                System.err.println("Error processing " + gpxFile.getName() + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        System.out.println("=".repeat(60));
        System.out.println("Processing complete. Processed " + gpxFiles.size() + " file(s).");
        System.out.println("CSV and PNG files saved to: " + finalOutputDirectory);
        System.out.println("=".repeat(60));
    }
    
    private static void printUsage() {
        System.out.println("GPS Elevation Profile Analyzer");
        System.out.println("==============================");
        System.out.println();
        System.out.println("Usage: java com.github.ledlogic.gpxanalyzer.ElevationProfileApp <file-or-directory> [options]");
        System.out.println();
        System.out.println("Arguments:");
        System.out.println("  <file-or-directory>  Single GPX file or directory containing GPX files");
        System.out.println();
        System.out.println("Options:");
        System.out.println("  --no-gui             Don't display graphical plots (faster batch processing)");
        System.out.println();
        System.out.println("Output:");
        System.out.println("  CSV and PNG files are automatically saved in the same directory as the input GPX file(s)");
        System.out.println();
        System.out.println("Examples:");
        System.out.println("  Single file:");
        System.out.println("    java -cp bin com.github.ledlogic.gpxanalyzer.ElevationProfileApp track.gpx");
        System.out.println("    → Creates: track.csv and track.png in the same directory");
        System.out.println();
        System.out.println("  Directory (all GPX files):");
        System.out.println("    java -cp bin com.github.ledlogic.gpxanalyzer.ElevationProfileApp ./gpx_tracks/");
        System.out.println("    → Creates CSV and PNG for each GPX file in ./gpx_tracks/");
        System.out.println();
        System.out.println("  Directory without GUI (batch mode):");
        System.out.println("    java -cp bin com.github.ledlogic.gpxanalyzer.ElevationProfileApp ./tracks/ --no-gui");
        System.out.println("    → Fast batch processing, saves all CSV and PNG files");
    }
}
