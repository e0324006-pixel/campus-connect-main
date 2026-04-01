#!/bin/bash
# ================================================
#  Campus Connect — Build & Run Script
#  Usage: ./run.sh
# ================================================

set -e

PROJECT_DIR="$(cd "$(dirname "$0")" && pwd)"
SRC_DIR="$PROJECT_DIR/src"
OUT_DIR="$PROJECT_DIR/out"
DATA_DIR="$PROJECT_DIR/data"

echo "=============================================="
echo "  Campus Connect Build Script"
echo "=============================================="
echo ""

# Check Java
if ! command -v javac &>/dev/null; then
  echo "ERROR: javac not found. Please install JDK 11 or later."
  echo "  Ubuntu/Debian: sudo apt install default-jdk"
  echo "  macOS:         brew install openjdk"
  exit 1
fi

echo "✓ Java found: $(javac -version 2>&1)"
echo ""

# Clean and create output directory
rm -rf "$OUT_DIR"
mkdir -p "$OUT_DIR"
mkdir -p "$DATA_DIR"

# Gather all source files
echo "📦 Collecting source files..."
find "$SRC_DIR" -name "*.java" > /tmp/campus_sources.txt
SOURCE_COUNT=$(wc -l < /tmp/campus_sources.txt)
echo "   Found $SOURCE_COUNT Java files"
echo ""

# Compile
echo "🔨 Compiling..."
javac -d "$OUT_DIR" \
      -sourcepath "$SRC_DIR" \
      -source 11 -target 11 \
      @/tmp/campus_sources.txt

echo "✅ Compilation successful!"
echo ""

# Run
echo "🚀 Starting Campus Connect server..."
echo ""
java -cp "$OUT_DIR" Main
