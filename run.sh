#!/bin/bash

echo "=============================="
echo " Compiling JavaFX Project..."
echo "=============================="

javac -d bin \
--module-path /usr/share/openjfx/lib \
--add-modules javafx.controls \
src/model/*.java src/scheduler/*.java src/gui/MainApp.java

if [ $? -ne 0 ]; then
    echo "Compilation Failed ❌"
    exit 1
fi

echo "=============================="
echo " Running Application..."
echo "=============================="

java -cp bin \
--module-path /usr/share/openjfx/lib \
--add-modules javafx.controls \
gui.MainApp
