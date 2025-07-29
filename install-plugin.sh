#!/bin/bash

# JMeter WebSocket Samplers Plugin Installer
# This script installs the plugin to your Homebrew JMeter installation

JMETER_HOME="/opt/homebrew/opt/jmeter/libexec"
PLUGIN_DIR="$JMETER_HOME/lib/ext"

echo "Installing JMeter WebSocket Samplers Plugin..."
echo "JMETER_HOME: $JMETER_HOME"
echo "Plugin directory: $PLUGIN_DIR"

# Build the project
echo "Building the project..."
./gradlew clean jar

# Remove old versions if they exist
echo "Removing old versions..."
rm -f "$PLUGIN_DIR"/JMeterWebSocketSamplers-*.jar

# Copy the new version
echo "Installing new version..."
cp build/libs/JMeterWebSocketSamplers-*.jar "$PLUGIN_DIR/"

echo "Installation complete!"
echo "Plugin files installed:"
ls -la "$PLUGIN_DIR"/JMeterWebSocketSamplers-*.jar

echo ""
echo "You can now start JMeter and the WebSocket samplers will be available." 