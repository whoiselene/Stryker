#!/bin/bash
set -e

mkdir -p lib

echo "Downloading Gson..."
curl -L -o lib/gson-2.10.1.jar https://repo1.maven.org/maven2/com/google/code/gson/gson/2.10.1/gson-2.10.1.jar

echo "Downloading Java-WebSocket..."
curl -L -o lib/Java-WebSocket-1.5.4.jar https://repo1.maven.org/maven2/org/java-websocket/Java-WebSocket/1.5.4/Java-WebSocket-1.5.4.jar

echo "Downloading SLF4J API..."
curl -L -o lib/slf4j-api-1.7.36.jar https://repo1.maven.org/maven2/org/slf4j/slf4j-api/1.7.36/slf4j-api-1.7.36.jar

echo "Downloading SLF4J Simple..."
curl -L -o lib/slf4j-simple-1.7.36.jar https://repo1.maven.org/maven2/org/slf4j/slf4j-simple/1.7.36/slf4j-simple-1.7.36.jar

echo "All dependencies downloaded to lib/ directory."
