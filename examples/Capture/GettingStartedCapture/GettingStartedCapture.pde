/**
 * Getting started example for processing video for Android
 * By Umair Khan
 */

import in.omerjerk.processing.video.android.*;

Capture cap;

void setup() {
  
  size(720, 1280, P2D);
  
  String[] cameras = Capture.list();
  cap = new Capture(this, cameras[0]);
  cap.start();
}

void draw() {
  image(cap, 0, 0);
}
