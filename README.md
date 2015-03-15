# Processing Video implementation for Android
In this branch, I'm maintaing another approach to render camera preview on processing's `GLSurfaceView`.
The idea is to render camera preview on a `SurfaceTexture` and as soon as the new frame is received, copy the data from this `SurfaceTexture` to processing's underlying texure. 
We cannot directly render to processing's own texture because it's bound `GLES20.GL_TEXTURE_2D` target, whereas for the preview to happen, the texture should be bound to `GLES11Ext.GL_TEXTURE_EXTERNAL_OES`.

As of now, I've written pretty much the OpenGL code but a lot still remains to be fixed.

For an alternative, but working, ultra slow approach head over to pixel_manipulation branch.
