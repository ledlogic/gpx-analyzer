package com.github.ledlogic.gpxanalyzer;

import java.io.Serializable;
import java.time.Instant;

/**
 * Represents a single GPS track point with location, altitude, and distance information
 */
public class TrackPoint implements Serializable, Comparable<TrackPoint> {
    private static final long serialVersionUID = 1L;
    
    public double latitude;
    public double longitude;
    public double altitude;
    public double distanceFromStart; // cumulative distance in meters
    public double distanceFromPrevious; // distance from previous point in meters
    public Instant timestamp; // time of the track point (may be null)
    
    public TrackPoint(double lat, double lon, double alt) {
        this.latitude = lat;
        this.longitude = lon;
        this.altitude = alt;
        this.distanceFromStart = 0;
        this.distanceFromPrevious = 0;
        this.timestamp = null;
    }
    
    public TrackPoint(double lat, double lon, double alt, Instant timestamp) {
        this(lat, lon, alt);
        this.timestamp = timestamp;
    }
    
    @Override
    public String toString() {
        return String.format("Distance: %.2fm, Altitude: %.2fm, Time: %s", 
                           distanceFromStart, altitude, 
                           timestamp != null ? timestamp.toString() : "N/A");
    }
    
    @Override
    public int compareTo(TrackPoint other) {
        // Sort by timestamp if both have timestamps
        if (this.timestamp != null && other.timestamp != null) {
            return this.timestamp.compareTo(other.timestamp);
        }
        // If no timestamps, maintain original order (return 0)
        return 0;
    }
}
