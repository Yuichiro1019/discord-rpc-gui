#!/bin/bash
export JAVA_HOME=/home/eevee/.local/share/JetBrains/Toolbox/apps/intellij-idea/jbr
export PATH=$JAVA_HOME/bin:$PATH
export WAYLAND_DISPLAY=""
export _JAVA_AWT_WM_NONREPARENTING=1

echo "Starting Discord RPC GUI..."
./gradlew run --no-daemon
