import in.omerjerk.processing.video.android.*;

Capture cap;

PShader bwShader;

void setup() {
  
  fullscreen(P2D);
  
  cap = new Capture(this);
  String[] list = cap.list();
  cap.setCamera(list[0]);
  cap.start();
  bwShader = loadShader("fragmentShader.glsl");
}

void draw() {
  cap.read();
  shader(bwShader);
  image(cap, 0, 0);
}
