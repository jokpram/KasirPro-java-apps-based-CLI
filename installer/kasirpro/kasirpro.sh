#!/bin/bash
echo ""
echo "================================================"
echo "       KASIR PRO - Sistem Kasir Modern"
echo "================================================"
echo ""

# Check if Java is installed
if ! command -v java &> /dev/null; then
    echo "ERROR: Java tidak ditemukan!"
    echo ""
    echo "Silakan install Java 21 atau lebih tinggi:"
    echo "  - Ubuntu/Debian: sudo apt install openjdk-21-jdk"
    echo "  - macOS: brew install openjdk@21"
    echo ""
    exit 1
fi

# Show Java version
echo "Java Version: $(java -version 2>&1 | head -n 1)"
echo ""

# Get script directory
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

# Run the application
echo "Menjalankan KASIR PRO..."
echo ""
java -jar "$SCRIPT_DIR/kasirpro-1.0.0-all.jar"
