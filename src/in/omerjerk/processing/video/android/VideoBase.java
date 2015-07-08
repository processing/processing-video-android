package in.omerjerk.processing.video.android;

import java.nio.IntBuffer;

import in.omerjerk.processing.video.android.helpers.FullFrameRect;
import in.omerjerk.processing.video.android.helpers.GlUtil;
import in.omerjerk.processing.video.android.helpers.Texture2dProgram;
import android.app.Activity;
import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PImage;
import processing.opengl.PGL;
import processing.opengl.PGraphicsOpenGL;
import processing.opengl.Texture;

public abstract class VideoBase extends PImage implements PConstants,
        SurfaceTexture.OnFrameAvailableListener {

    protected static final boolean DEBUG = true;

    protected Activity activity;
    
    protected boolean isAvailable = false;

    public static void log(String log) {
        if (DEBUG)
            System.out.println(log);
    }

    protected GLSurfaceView glView;
    protected SurfaceTexture mSurfaceTexture;
    protected FullFrameRect mFullScreen;
    protected int mTextureId;
    protected final float[] mSTMatrix = new float[16];

    // private Texture customTexture;
    protected PGraphicsOpenGL pg;

    protected PGraphicsOpenGL destpg;

    IntBuffer frameBuffers = IntBuffer.allocate(1);
    IntBuffer renderBuffers = IntBuffer.allocate(1);
    IntBuffer customTexture = IntBuffer.allocate(1);

    protected IntBuffer pixelBuffer;
    
    public abstract void onResume();
    public abstract void onPause();
    
    public VideoBase(PApplet parent) {
        super();
        this.parent = parent;
        
        parent.registerMethod("pause", this);
        parent.registerMethod("resume", this);
        
        glView = (GLSurfaceView) parent.getSurfaceView();
        pg = (PGraphicsOpenGL)parent.g;
//      customTexture = new Texture(pg, width, height);
//      customTexture.invertedY(true);
        glView.queueEvent(new Runnable() {
            @Override
            public void run() {
                createSurfaceTexture();
                prepareFrameBuffers();
            }
        });
//      pg.setCache(this, customTexture);
        activity = parent.getActivity();
    }
   
    public boolean available() {
        return isAvailable;
    }

    protected void createSurfaceTexture() {
        mFullScreen = new FullFrameRect(new Texture2dProgram(
                Texture2dProgram.ProgramType.TEXTURE_EXT));
        mTextureId = mFullScreen.createTextureObject();
        mSurfaceTexture = new SurfaceTexture(mTextureId);
        mSurfaceTexture.setOnFrameAvailableListener(this);
    }

    protected void prepareFrameBuffers() {
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        
        //Generate frame buffer
        GLES20.glGenFramebuffers(1, frameBuffers);
        GlUtil.checkGlError("glGenFramebuffers");
        //Bind frame buffer
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBuffers.get(0));
        GlUtil.checkGlError("glBindFramebuffer");
        
        //Generate render buffers
        GLES20.glGenRenderbuffers(1, renderBuffers);
        GlUtil.checkGlError("glGenRenderbuffers");
        //Bind render buffers
        GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, renderBuffers.get(0));
        GlUtil.checkGlError("glBindRenderbuffer");
        //Allocate memory to render buffers
        GLES20.glRenderbufferStorage(GLES20.GL_RENDERBUFFER, GLES20.GL_DEPTH_COMPONENT16, width, height);
        GlUtil.checkGlError("glRenderbufferStorage");
        
        //Attach render buffer to frame buffer
        GLES20.glFramebufferRenderbuffer(GLES20.GL_FRAMEBUFFER, GLES20.GL_DEPTH_ATTACHMENT,
                GLES20.GL_RENDERBUFFER, renderBuffers.get(0));
        GlUtil.checkGlError("glFramebufferRenderbuffer");
        
        GLES20.glGenTextures(1, customTexture);
        GlUtil.checkGlError("glGenTextures");

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, customTexture.get(0));
        GlUtil.checkGlError("glBindTexture");
        
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, width, height, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);
        GlUtil.checkGlError("glTexImage2D");

        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0,
                GLES20.GL_TEXTURE_2D, customTexture.get(0), 0);
        GlUtil.checkGlError("glFramebufferTexture2D");
        
        /*
        //No sure if this is required in opengl es 2. Ignoring as of now.
        IntBuffer drawBuffers = IntBuffer.allocate(1);
        drawBuffers.put(0, GLES20.GL_COLOR_ATTACHMENT0);
        GLES30.glDrawBuffers(1, drawBuffers);
        */
        
        // See if GLES is happy with all this.
        int status = GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER);
        if (status != GLES20.GL_FRAMEBUFFER_COMPLETE) {
            throw new RuntimeException("Framebuffer not complete, status=" + status);
        }
    }

    @Override
    public void onFrameAvailable(final SurfaceTexture surfaceTexture) {
        glView.queueEvent(new Runnable() {
            @Override
            public void run() {
                log("onFrameAvailable");
                isAvailable = true;
                surfaceTexture.updateTexImage();

                GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER,
                        frameBuffers.get(0));
                GLES20.glViewport(0, 0, width, height);
                surfaceTexture.getTransformMatrix(mSTMatrix);
                mFullScreen.drawFrame(mTextureId, mSTMatrix);

                getImage(false);

                /*
                 * pixelBuffer.position(0); GLES20.glReadPixels(0, 0, width,
                 * height, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE,
                 * pixelBuffer); pixelBuffer.position(0);
                 * pixelBuffer.get(Capture.this.pixels); updatePixels();
                 */

                // Fall back to default frame buffer. Not sure if needed.
                GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
            }
        });
    }
    
    public void pause() {
        onPause();
        glView.queueEvent(new Runnable() {
            @Override
            public void run() {
                if (mSurfaceTexture != null) {
                    mSurfaceTexture.release();
                    mSurfaceTexture = null;
                }
                if (mFullScreen != null) {
                    mFullScreen.release(false);     // assume the GLSurfaceView EGL context is about
                    mFullScreen = null;             //  to be destroyed
                }
            }
        });
    }
    
    public void resume() {
        glView.queueEvent(new Runnable() {
            @Override
            public void run() {
                createSurfaceTexture();
                prepareFrameBuffers();
            }
        });
        onResume();
    }

    public void getImage(boolean loadPixels) {

        if (destpg == null || destpg.width != width || destpg.height != height) {
            destpg = (PGraphicsOpenGL) parent.createGraphics(width, height,
                    PConstants.P2D);
            destpg.pgl.setGlThread(Thread.currentThread());
        }

        destpg.beginDraw();
        destpg.background(0, 0);
        PGL pgl = destpg.beginPGL();
        pgl.drawTexture(PGL.TEXTURE_2D, customTexture.get(0), width, height, 0,
                0, width, height);
        destpg.endPGL();
        destpg.endDraw();

        // Uses the PGraphics texture as the cache object for the image
        Texture tex = destpg.getTexture();
        pg.setCache(this, tex);
        if (loadPixels) {
            super.loadPixels();
            tex.get(this.pixels);
            this.setLoaded(false);
        }
    }
    
    @Override
    public void loadPixels() {
        super.loadPixels();
        //It's ultra slow right now

        if (pixelBuffer == null) {
            pixelBuffer = IntBuffer.allocate(width * height);
        }
        pixelBuffer.position(0);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBuffers.get(0));
        GLES20.glViewport(0, 0, width, height);
        GLES20.glReadPixels(0, 0, width, height, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, pixelBuffer);
        pixelBuffer.position(0);
        pixelBuffer.get(this.pixels);
    }
}
