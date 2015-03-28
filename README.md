# Processing Video implementation for Android
This library tries to expose the same API endpoints as that of processing-video library for PC. The sketch from processing-video library works for Android.

**A primitive example :**
```
import in.omerjerk.processing.video.android.*;

Capture cap;

void setup() {
  size(720, 1280, P2D);
  cap = new Capture(this);
  String[] list = cap.list();
  Capture.printCompatibleResolutionsList(cap);
}

void draw() {
  image(cap, 0, 0);
}
```

**Behind the hood :**
The idea is to render camera preview on a `SurfaceTexture` and as soon as the new frame is received, copy the data from this `SurfaceTexture` to a custom texure bound to target `GL_TEXTURE_2D` (Note : We cannot directly render to a texture bound to `GL_TEXTURE_2D` target, because for the preview to happen, the texture must be bound to `GL_TEXTURE_EXTERNAL_OES`). This custom texture is then rendered to a PGraphics object. The backing texture of that PGraphics object is then used as the texture cache for our PImage file which stores the video.
