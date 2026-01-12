#!/bin/bash
# Run the GPX Analyzer application

if [ ! -d "bin" ]; then
    echo "Error: Project not compiled. Please run ./compile.sh first"
    exit 1
fi

if [ $# -eq 0 ]; then
    echo "Usage: ./run.sh <gpx-file> [options]"
    echo ""
    echo "Options:"
    echo "  --csv <file>     Export data to CSV file"
    echo "  --no-gui         Don't display graphical plot"
    echo ""
    echo "Examples:"
    echo "  ./run.sh sample_track.gpx"
    echo "  ./run.sh track.gpx --csv output.csv"
    echo "  ./run.sh track.gpx --no-gui --csv data.csv"
    exit 1
fi

java -cp bin com.github.ledlogic.gpxanalyzer.ElevationProfileApp "$@"
