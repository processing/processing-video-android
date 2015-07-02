/**
 * Getting started example for processing video for Android
 * By Umair Khan
 */

import in.omerjerk.processing.video.android.*;

Capture cap;

void setup() {
  
  size(640, 360, P2D);
  
  cap = new Capture(this);
  String[] list = cap.list();
  cap.setCamera(list[0]);
  cap.start();
}

void draw() {
  image(cap, 0, 0);
}
