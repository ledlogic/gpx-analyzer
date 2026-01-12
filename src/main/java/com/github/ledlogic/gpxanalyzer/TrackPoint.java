package com.github.ledlogic.gpxanalyzer;

import java.io.Serializable;

/**
 * Represents a single GPS track point with location, altitude, and distance information
 */
public class TrackPoint implements Serializable {
    private static final long serialVersionUID = 1L;
    
    public double latitude;
    public double longitude;
    public double altitude;
    public double distanceFromStart; // cumulative distance in meters
    public double distanceFromPrevious; // distance from previous point in meters
    
    public TrackPoint(double lat, double lon, double alt) {
        this.latitude = lat;
        this.longitude = lon;
        this.altitude = alt;
        this.distanceFromStart = 0;
        this.distanceFromPrevious = 0;
    }
    
    @Override
    public String toString() {
        return String.format("Distance: %.2fm, Altitude: %.2fm", 
                           distanceFromStart, altitude);
    }
}
