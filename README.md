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

# Run with a single file (creates CSV and PNG in same directory)
./run.sh sample_track.gpx

# Process all GPX files in a directory (creates CSV and PNG for each)
./run.sh ./gpx_tracks/

# Batch mode - no GUI windows (faster for many files)
./run.sh ./tracks/ --no-gui
```

**Output**: CSV and PNG files are automatically saved in the same directory as the GPX files.

### Manual compilation and execution:

See the sections below for detailed compilation and usage instructions.

## Features

- **GPX File Support**: Parses standard GPX files with track points
- **Batch Processing**: Process single files or entire directories of GPX files
- **Distance Calculation**: Uses Haversine formula for accurate horizontal distances
- **Elevation Profile**: Plots altitude on Y-axis vs. cumulative distance on X-axis
- **Timestamp Display**: Shows time (HH:MM) at each data point when available
- **Smart Unit Display**: Automatically shows meters for tracks < 1km, kilometers for longer tracks
- **Chronological Ordering**: Sorts points by timestamp to ensure proper sequence
- **Dynamic Titles**: Each plot displays the filename as its title
- **PNG Export**: Save plots as high-quality PNG images
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
- `sample_track.gpx` - Sample GPX file for testing (longer track, displays in km)
- `short_track.gpx` - Short track for testing meter display (< 1km)

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

### Single File Processing

```bash
java -cp bin com.github.ledlogic.gpxanalyzer.ElevationProfileApp sample_track.gpx
```

**Output** (automatically created in same directory):
- `sample_track.csv` - Data file
- `sample_track.png` - Plot image
- GUI window showing the plot

### Directory Processing (Batch Mode)

Process all GPX files in a directory:

```bash
java -cp bin com.github.ledlogic.gpxanalyzer.ElevationProfileApp ./gpx_tracks/
```

This will:
1. Find all `.gpx` files in the directory
2. Parse each file and calculate distances/altitudes
3. Save CSV and PNG for each file in the same directory
4. Display statistics for each file in the console
5. Show a separate plot window for each file (with filename as title)

**Example output structure:**
```
gpx_tracks/
├── morning_run.gpx
├── morning_run.csv      ← automatically created
├── morning_run.png      ← automatically created
├── afternoon_hike.gpx
├── afternoon_hike.csv   ← automatically created
└── afternoon_hike.png   ← automatically created
```

### Batch Mode (No GUI - Faster)

Process files without displaying plots:

```bash
java -cp bin com.github.ledlogic.gpxanalyzer.ElevationProfileApp ./tracks/ --no-gui
```

Perfect for:
- Processing many files quickly
- Server environments without displays
- Automated workflows
- Creating archives

Still creates all CSV and PNG files automatically!

## Output Files

The application automatically creates two output files for each GPX file in the **same directory** as the input:

### CSV File
- **Filename**: Same as GPX file with `.csv` extension
- **Content**: Distance (m & km), Altitude (m & ft) for each point
- **Use**: Data analysis, spreadsheets, further processing

### PNG File
- **Filename**: Same as GPX file with `.png` extension
- **Size**: 1000×600 pixels
- **Quality**: High-quality with antialiasing
- **Content**: Elevation profile plot with title, axes, and labels
- **Use**: Reports, presentations, websites, sharing

### Example
Input: `mountain_hike.gpx` in `/tracks/`

Output:
- `/tracks/mountain_hike.csv` - Data file
- `/tracks/mountain_hike.png` - Plot image

**Benefits**:
- ✅ No need to specify output directories
- ✅ Files stay organized with their source data
- ✅ Easy to find - same location as GPX files
- ✅ Matching names make relationships clear

### Using Just the Parser

For single file parsing:

```bash
java -cp bin com.github.ledlogic.gpxanalyzer.GPXElevationProfile sample_track.gpx
```

## Data Structure

Each track point contains:

- **latitude**: GPS latitude
- **longitude**: GPS longitude  
- **altitude**: Elevation in meters
- **distanceFromStart**: Cumulative horizontal distance from start (meters) - this is the s-distance along the path
- **distanceFromPrevious**: Distance from previous point (meters)
- **timestamp**: Time of the GPS recording (optional, used for chronological sorting)

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

### Important: S-Distance Curve (Path Distance)

The X-axis represents the **cumulative distance traveled along the path** (s-distance curve), NOT the straight-line distance from the start point. This means:

1. **Sequential summation**: Distance from point A → B → C is the sum of distances A→B and B→C
2. **Path following**: The graph shows the actual route taken, including turns and curves
3. **Chronological order**: Points are plotted in chronological order based on timestamps (if available in the GPX file)

**Example**: If you hike 5km north, then 5km south back to your starting point, the graph will show 10km total distance, not 0km.

## Unit Display

The application automatically selects appropriate units based on track length:

- **Short tracks (< 1km)**: Distance displayed in **meters** (e.g., "250.00 m", "750.50 m")
- **Longer tracks (≥ 1km)**: Distance displayed in **kilometers** (e.g., "1.50 km", "5.25 km")

This ensures optimal readability regardless of track length. The axis title also updates accordingly ("Distance (meters)" vs "Distance (kilometers)").

All axis labels display values with 2 decimal places for precision.

## Timestamp Visualization

When GPX files contain timestamp data, the plot displays:

- **Time labels** (HH:MM format) next to each data point
- **Green dot** at the start point
- **Blue dots** at intermediate points
- **Red dot** at the end point

This helps you:
- See when you reached specific points
- Understand your pace throughout the journey
- Identify rest stops or delays
- Plan future trips based on timing

Timestamps are automatically converted to your local timezone and displayed in 24-hour format.

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
