import in.omerjerk.processing.video.android.*;

Capture cap;

PShader embossShader;

void setup() {
  fullScreen(P2D);
  String[] cameras = Capture.list();
  cap = new Capture(this, cameras[0]);
  cap.start();
  embossShader = loadShader("fragmentShader.glsl");
}

void draw() {
  cap.read();
  shader(embossShader);
  image(cap, 0, 0);
}
