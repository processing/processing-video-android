/**
 * Getting started example for processing video for Android
 * By Umair Khan
 */

import in.omerjerk.processing.video.android.*;

Capture cap;

void setup() {
  
  size(720, 1280, P2D);

  //Use this to print list of resolutions supported by the camera
  Capture.printCompatibleResolutionsList(cap);
  
  String[] cameras = Capture.list();
  cap = new Capture(this, cameras[0]);
  cap.start();
}

void draw() {
  image(cap, 0, 0);
}

void captureEvent(Capture c) {
  c.read();
}
