import in.omerjerk.processing.video.android.*;

Capture cap;

PShader bwShader;

void setup() {
  
  fullScreen(P2D);
  String[] list = Capture.list();
  cap = new Capture(this, list[0]);
  
  cap.start();
  bwShader = loadShader("fragmentShader.glsl");
}

void draw() {
  cap.read();
  shader(bwShader);
  image(cap, 0, 0);
}