/**
 * Getting started example for processing video for Android
 * By Umair Khan
 */

import in.omerjerk.processing.video.android.*;

Capture cap;

void setup() {
  
  //size(640, 360, P3D);
  
  cap = new Capture(this);
  String[] list = cap.list();
  //cap.setCamera(list[0]);
}

void draw() {
  //image(cap, 0, 0);
}

public String sketchRenderer() {
  return P2D;
}
