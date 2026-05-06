#!/bin/bash

# Ensure the script stops on the first error
set -e

# Note: JetBrains Compose packaging requires a full JDK (which includes 'jpackage'), 
# not just a JRE. If you get a 'jpackage missing' error locally, you will need to
# point JAVA_HOME to a standard JDK (like Temurin or OpenJDK 17/21).
# We discovered a full JDK at ~/.jdks/openjdk-26 which has jpackage. 
# Using this instead of JBR to fix the 'jpackage missing' error.
export JAVA_HOME=/home/eevee/.jdks/openjdk-26
export PATH=$JAVA_HOME/bin:$PATH

echo "Compiling and packaging Discord RPC GUI for Production..."
echo "This will generate the installer for your current OS (e.g., .deb for Linux)."

# This Gradle task dynamically builds the proper format for whatever OS you run it on
./gradlew packageReleaseDistributionForCurrentOS --no-daemon

echo ""
echo "=========================================="
echo "✅ Build Complete!"
echo "You can find your generated installers inside:"
echo " -> build/compose/binaries/main/"
echo "=========================================="
