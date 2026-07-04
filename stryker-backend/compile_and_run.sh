#!/bin/bash
set -e

mkdir -p target/classes

echo "Finding Java source files..."
find src/main/java -name "*.java" > sources.txt

echo "Compiling Java files..."
javac -cp "lib/*" -d target/classes @sources.txt

echo "Launching STRYKER Match Engine Server..."
java -cp "target/classes:lib/*" com.stryker.Main 8080

rm -f sources.txt
