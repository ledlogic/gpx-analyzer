package com.github.ledlogic.gpxanalyzer;

import java.io.File;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Parses GPX files and calculates elevation profile data
 * (horizontal distance vs altitude)
 */
public class GPXElevationProfile {
    
    /**
     * Calculates the distance between two GPS coordinates using Haversine formula
     * @return distance in meters
     */
    private static double haversineDistance(double lat1, double lon1, 
                                           double lat2, double lon2) {
        final double R = 6371000; // Earth's radius in meters
        
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return R * c;
    }
    
    /**
     * Parses a GPX file and extracts track points with altitude and distance data
     */
    public static List<TrackPoint> parseGPX(String filePath) throws Exception {
        List<TrackPoint> points = new ArrayList<>();
        
        File gpxFile = new File(filePath);
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(gpxFile);
        
        // Get all track points (trkpt elements)
        NodeList trkptList = doc.getElementsByTagName("trkpt");
        
        List<TrackPoint> unsortedPoints = new ArrayList<>();
        
        for (int i = 0; i < trkptList.getLength(); i++) {
            Element trkpt = (Element) trkptList.item(i);
            
            // Get latitude and longitude from attributes
            double lat = Double.parseDouble(trkpt.getAttribute("lat"));
            double lon = Double.parseDouble(trkpt.getAttribute("lon"));
            
            // Get elevation from child element
            double altitude = 0;
            NodeList eleList = trkpt.getElementsByTagName("ele");
            if (eleList.getLength() > 0) {
                altitude = Double.parseDouble(eleList.item(0).getTextContent());
            }
            
            // Get timestamp from child element (if available)
            Instant timestamp = null;
            NodeList timeList = trkpt.getElementsByTagName("time");
            if (timeList.getLength() > 0) {
                try {
                    String timeStr = timeList.item(0).getTextContent();
                    timestamp = Instant.parse(timeStr);
                } catch (Exception e) {
                    // If timestamp parsing fails, continue without it
                    System.err.println("Warning: Could not parse timestamp at index " + i);
                }
            }
            
            TrackPoint point = new TrackPoint(lat, lon, altitude, timestamp);
            unsortedPoints.add(point);
        }
        
        // Sort points chronologically if timestamps are available
        Collections.sort(unsortedPoints);
        
        // Now calculate cumulative distances on the sorted points
        double cumulativeDistance = 0;
        TrackPoint previousPoint = null;
        
        for (TrackPoint point : unsortedPoints) {
            // Calculate distance from previous point
            if (previousPoint != null) {
                double distance = haversineDistance(
                    previousPoint.latitude, previousPoint.longitude,
                    point.latitude, point.longitude
                );
                point.distanceFromPrevious = distance;
                cumulativeDistance += distance;
            }
            
            point.distanceFromStart = cumulativeDistance;
            points.add(point);
            previousPoint = point;
        }
        
        return points;
    }
    
    /**
     * Exports the elevation profile data to CSV format for easy plotting
     */
    public static void exportToCSV(List<TrackPoint> points, String outputPath) 
            throws Exception {
        java.io.PrintWriter writer = new java.io.PrintWriter(outputPath);
        
        writer.println("Distance_m,Altitude_m,Distance_km,Altitude_ft");
        
        for (TrackPoint point : points) {
            writer.printf("%.2f,%.2f,%.3f,%.2f%n",
                         point.distanceFromStart,
                         point.altitude,
                         point.distanceFromStart / 1000.0,
                         point.altitude * 3.28084); // convert to feet
        }
        
        writer.close();
    }
    
    /**
     * Prints statistics about the track
     */
    public static void printStatistics(List<TrackPoint> points) {
        if (points.isEmpty()) {
            System.out.println("No track points found.");
            return;
        }
        
        double minAlt = Double.MAX_VALUE;
        double maxAlt = Double.MIN_VALUE;
        double totalDistance = points.get(points.size() - 1).distanceFromStart;
        
        for (TrackPoint point : points) {
            minAlt = Math.min(minAlt, point.altitude);
            maxAlt = Math.max(maxAlt, point.altitude);
        }
        
        double elevationGain = maxAlt - minAlt;
        
        System.out.println("\n=== Track Statistics ===");
        System.out.printf("Total Points: %d%n", points.size());
        
        // Display distance in meters if < 1km, otherwise in km
        if (totalDistance < 1000) {
            System.out.printf("Total Distance: %.2f m (%.2f ft)%n", 
                             totalDistance, totalDistance * 3.28084);
        } else {
            System.out.printf("Total Distance: %.2f km (%.2f miles)%n", 
                             totalDistance / 1000.0, totalDistance / 1609.34);
        }
        
        System.out.printf("Min Altitude: %.2f m (%.2f ft)%n", 
                         minAlt, minAlt * 3.28084);
        System.out.printf("Max Altitude: %.2f m (%.2f ft)%n", 
                         maxAlt, maxAlt * 3.28084);
        System.out.printf("Elevation Range: %.2f m (%.2f ft)%n", 
                         elevationGain, elevationGain * 3.28084);
    }
    
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: java GPXElevationProfile <gpx-file> [output-csv]");
            System.out.println("Example: java GPXElevationProfile track.gpx output.csv");
            return;
        }
        
        String gpxFile = args[0];
        String csvFile = args.length > 1 ? args[1] : "elevation_profile.csv";
        
        try {
            System.out.println("Parsing GPX file: " + gpxFile);
            List<TrackPoint> points = parseGPX(gpxFile);
            
            System.out.println("Successfully parsed " + points.size() + " track points.");
            
            printStatistics(points);
            
            exportToCSV(points, csvFile);
            System.out.println("\nElevation profile exported to: " + csvFile);
            
            // Print first few points as sample
            System.out.println("\n=== Sample Data (first 5 points) ===");
            for (int i = 0; i < Math.min(5, points.size()); i++) {
                System.out.println(points.get(i));
            }
            
        } catch (Exception e) {
            System.err.println("Error processing GPX file: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
