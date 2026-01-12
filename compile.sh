#!/bin/bash
# Compile the GPX Analyzer project

echo "Compiling GPX Elevation Profile Analyzer..."

# Create bin directory if it doesn't exist
mkdir -p bin

# Compile all Java files
javac -d bin src/com/github/ledlogic/gpxanalyzer/*.java

if [ $? -eq 0 ]; then
    echo "Compilation successful!"
    echo "Class files are in the bin/ directory"
    echo ""
    echo "To run the application:"
    echo "  java -cp bin com.github.ledlogic.gpxanalyzer.ElevationProfileApp sample_track.gpx"
else
    echo "Compilation failed!"
    exit 1
fi
