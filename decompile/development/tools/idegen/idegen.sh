#!/bin/bash

if [ ! -d development ]; then
    echo "Error: Run from the root of the tree."
    exit 1
fi

idegenjar=development/tools/idegen/idegen.jar
if [ "" = "$idegenjar" ]; then
    echo "Couldn't find idegen.jar. Please run make first."
else 
    java -cp $idegenjar Main
fi
