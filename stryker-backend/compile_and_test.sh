#!/bin/bash
set -e

mkdir -p target/classes

echo "Finding Java source files..."
find src/main/java src/test/java -name "*.java" > sources.txt

echo "Compiling Java files..."
javac -cp "lib/*" -d target/classes @sources.txt

echo "Running Unit Tests..."
java -cp "target/classes:lib/*" com.stryker.TestRunner

rm -f sources.txt
