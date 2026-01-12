# GPS Elevation Profile Analyzer

A Java application that loads GPS track files (GPX format) and creates precise elevation profiles showing altitude vs. horizontal distance.

## Package Structure

```
src/
└── com/
    └── github/
        └── ledlogic/
            └── gpxanalyzer/
                ├── TrackPoint.java
                ├── GPXElevationProfile.java
                ├── ElevationPlotter.java
                └── ElevationProfileApp.java
```

## Quick Start

### Using the provided scripts (Linux/Mac):

```bash
# Compile the project
./compile.sh

# Run with sample data
./run.sh sample_track.gpx

# Run with your own GPX file
./run.sh your_track.gpx --csv output.csv
```

### Manual compilation and execution:

See the sections below for detailed compilation and usage instructions.

## Features

- **GPX File Support**: Parses standard GPX files with track points
- **Distance Calculation**: Uses Haversine formula for accurate horizontal distances
- **Elevation Profile**: Plots altitude on Y-axis vs. cumulative distance on X-axis
- **Data Export**: Export data to CSV for use in spreadsheet applications
- **Graphical Display**: Built-in Java Swing visualization
- **Statistics**: Displays track statistics (total distance, elevation range, etc.)

## Files Included

**Source Files** (in `src/com/github/ledlogic/gpxanalyzer/`):
- `TrackPoint.java` - Data class representing a GPS track point
- `GPXElevationProfile.java` - Core parser and data processing
- `ElevationPlotter.java` - Graphical plotting component
- `ElevationProfileApp.java` - Main application

**Sample Data**:
- `sample_track.gpx` - Sample GPX file for testing

**Helper Scripts** (Linux/Mac):
- `compile.sh` - Compiles the project
- `run.sh` - Runs the application with arguments

## Compilation

From the project root directory, compile all files with:

```bash
javac -d bin src/com/github/ledlogic/gpxanalyzer/*.java
```

Or compile individually in order:

```bash
javac -d bin src/com/github/ledlogic/gpxanalyzer/TrackPoint.java
javac -d bin src/com/github/ledlogic/gpxanalyzer/GPXElevationProfile.java
javac -d bin src/com/github/ledlogic/gpxanalyzer/ElevationPlotter.java
javac -d bin src/com/github/ledlogic/gpxanalyzer/ElevationProfileApp.java
```

This will create the compiled `.class` files in the `bin/` directory with the proper package structure.

## Usage

### Basic Usage (with GUI)

```bash
java -cp bin com.github.ledlogic.gpxanalyzer.ElevationProfileApp sample_track.gpx
```

This will:
1. Parse the GPX file
2. Calculate distances and extract altitudes
3. Display statistics in the console
4. Show a graphical elevation profile

### Export to CSV

```bash
java -cp bin com.github.ledlogic.gpxanalyzer.ElevationProfileApp sample_track.gpx --csv output.csv
```

### Console-Only Mode (No GUI)

```bash
java -cp bin com.github.ledlogic.gpxanalyzer.ElevationProfileApp sample_track.gpx --no-gui --csv output.csv
```

### Using Just the Parser

```bash
java -cp bin com.github.ledlogic.gpxanalyzer.GPXElevationProfile sample_track.gpx
```

or with custom CSV output:

```bash
java -cp bin com.github.ledlogic.gpxanalyzer.GPXElevationProfile sample_track.gpx my_output.csv
```

## Data Structure

Each track point contains:

- **latitude**: GPS latitude
- **longitude**: GPS longitude  
- **altitude**: Elevation in meters
- **distanceFromStart**: Cumulative horizontal distance from start (meters)
- **distanceFromPrevious**: Distance from previous point (meters)

## CSV Output Format

The exported CSV contains:

```
Distance_m,Altitude_m,Distance_km,Altitude_ft
0.00,100.00,0.000,328.08
145.23,110.50,0.145,362.53
...
```

## Supported File Formats

### GPX (Recommended - Best for this use case)

GPX is the standard format for GPS tracks and is the most straightforward for elevation profiles:

- **Pros**: 
  - Native support for track points with elevation
  - Widely supported by GPS devices and apps
  - Simple XML structure
  - No compression to deal with

- **Format**: XML-based with `<trkpt>` elements containing lat, lon, and `<ele>` for elevation

### Why GPX over KML/KMZ?

While KML and KMZ are also popular:

1. **KML**: More complex XML structure designed for Google Earth visualization
   - Can contain tracks, but structure varies
   - Requires more parsing logic for different schemas
   
2. **KMZ**: Compressed KML (ZIP format)
   - Requires unzipping before parsing
   - Same parsing complexity as KML

**Recommendation**: Use GPX files for best results. Most GPS devices and apps (Garmin, Strava, AllTrails, etc.) can export to GPX.

## How Distance Calculation Works

The program uses the **Haversine formula** to calculate the great-circle distance between consecutive GPS coordinates:

```
d = 2R × arcsin(√(sin²(Δlat/2) + cos(lat1) × cos(lat2) × sin²(Δlon/2)))
```

Where:
- R = Earth's radius (6,371,000 meters)
- Δlat = difference in latitude
- Δlon = difference in longitude

This provides accurate horizontal distances regardless of elevation changes.

## Example Output

```
Parsing GPX file: sample_track.gpx
Successfully parsed 26 track points.

=== Track Statistics ===
Total Points: 26
Total Distance: 3.86 km (2.40 miles)
Min Altitude: 90.00 m (295.28 ft)
Max Altitude: 475.50 m (1559.71 ft)
Elevation Range: 385.50 m (1264.43 ft)

Data exported to: output.csv
```

## Plotting Your Own Data

To use with your own GPS tracks:

1. Export your GPS data as GPX from your device/app
2. Run the program with your file:
   ```bash
   java ElevationProfileApp your_track.gpx
   ```

## Extending to Support KML/KMZ

If you need KML/KMZ support, here's what to add:

### For KML:
- Parse `<coordinates>` elements in `<LineString>` or `<gx:Track>`
- Coordinates format: `lon,lat,alt` (note: longitude first!)

### For KMZ:
- Use `java.util.zip.ZipInputStream` to extract
- Find the `.kml` file inside
- Parse as KML

Would you like me to add KML/KMZ support as well?

## Integration with Plotting Libraries

The data can be easily exported to CSV and used with:

- **Excel/Google Sheets**: Import CSV and create charts
- **Python matplotlib**: Read CSV and plot
- **R**: Use for statistical analysis and plotting
- **JFreeChart**: Java-based charting library (can be integrated)

## Requirements

- Java 8 or higher
- No external dependencies (uses only standard Java libraries)

## License

This project is licensed under the MIT License - see the LICENSE file for details.

You are free to:
- Use commercially
- Modify
- Distribute
- Use privately
- Sublicense

The only requirement is to include the original copyright and license notice in any copy of the software.
