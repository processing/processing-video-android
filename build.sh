#!/bin/sh
rm dist/video_android.zip
ant dist
rm -rf ~/sketchbook/libraries/video_android/
unzip dist/video_android.zip -d ~/sketchbook/libraries/