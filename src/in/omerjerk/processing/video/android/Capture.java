package in.omerjerk.processing.video.android;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.SurfaceHolder;
import processing.core.PConstants;
import processing.core.PApplet;
import processing.core.PImage;
import processing.opengl.PGL;
import processing.opengl.PGraphicsOpenGL;
import processing.opengl.Texture;

@SuppressWarnings("deprecation")
public class Capture extends PImage implements PConstants,
		SurfaceHolder.Callback, CameraHandlerCallback, SurfaceTexture.OnFrameAvailableListener {

	private static final boolean DEBUG = true;

	public static void log(String log) {
		if (DEBUG)
			System.out.println(log);
	}

	private PApplet applet;

	private Camera mCamera;

	private static ArrayList<String> camerasList = new ArrayList<String>();

	private static final String KEY_FRONT_CAMERA = "front-camera-%d";
	private static final String KEY_BACK_CAMERA = "back-camera-%d";

	private int selectedCamera = -1;

	private GLSurfaceView glView;
	private SurfaceTexture mSurfaceTexture;
	private FullFrameRect mFullScreen;
	private int mTextureId;
	private final float[] mSTMatrix = new float[16];
	
//	private Texture customTexture;
	private PGraphicsOpenGL pg;
	private IntBuffer pixelBuffer;
	
	private PGraphicsOpenGL destpg;
	PGL pgl;

	private CameraHandler mCameraHandler;
	
	IntBuffer frameBuffers = IntBuffer.allocate(1);
	IntBuffer renderBuffers = IntBuffer.allocate(1);
	IntBuffer customTexture = IntBuffer.allocate(1);

	public Capture(PApplet context) {
		this(context, -1, -1);
	}

	public Capture(final PApplet applet, int width, int height) {
		super();
		this.applet = applet;
		if (width == -1 || height == -1) {
			//TODO: Temp hack. Needs to be handled intelligently.
			width = 720;
			height = 1280;
		}
		init(width, height, ARGB);
		pixelBuffer = IntBuffer.allocate(width * height);
		pixelBuffer.position(0);
		
		applet.registerMethod("pause", this);
		applet.registerMethod("resume", this);
		glView = (GLSurfaceView) applet.getSurfaceView();
		pg = (PGraphicsOpenGL)applet.g;
//		customTexture = new Texture(pg, width, height);
//		customTexture.invertedY(true);
		log("cusotm texture address = " + customTexture.get(0));
//		pg.setCache(this, customTexture);
		applet.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mCameraHandler = new CameraHandler(Capture.this);
			}
		});

		glView.queueEvent(new Runnable() {
			@Override
			public void run() {
				mFullScreen = new FullFrameRect(
		                new Texture2dProgram(Texture2dProgram.ProgramType.TEXTURE_EXT));
				mTextureId = mFullScreen.createTextureObject();

				mSurfaceTexture = new SurfaceTexture(mTextureId);
				mSurfaceTexture.setOnFrameAvailableListener(Capture.this);
//				mCameraHandler.sendMessage(mCameraHandler.obtainMessage(CameraHandler.MSG_START_CAMERA, null));
				startCameraImpl(0);
				System.out.println("sent starting message to UI thread");
				prepareFrameBuffers();
			}
		});
	}

	public void setCamera(String camera) {
		if (camera == null || camera.equals("")) {
			selectedCamera = 0;
		} else {
			selectedCamera = camerasList.indexOf(camera);
		}
		log("Selected camera = " + selectedCamera);
		startCameraImpl(selectedCamera);
	}
	
	public void startCameraImpl(int cameraId) {
		try {
			mCamera = Camera.open(cameraId);
			mCamera.setDisplayOrientation(90);
			startPreview(applet.getSurfaceHolder());
		} catch (Exception e) {
			log("Couldn't open the Camera");
			e.printStackTrace();
		}
	}
	
	public void pause() {
		log("pause called");
		if (mCamera != null) {
			mCamera.release();
        }
		/*
		if (mFullScreen != null) {
            mFullScreen.release(false);     // assume the GLSurfaceView EGL context is about
            mFullScreen = null;             //  to be destroyed
        }*/
	}
	
	public void resume() {
		log("resume called");
		if (selectedCamera != -1) {
			startCameraImpl(selectedCamera);
		}
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		startPreview(holder);
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		startPreview(holder);
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// TODO: Release Camera resources
	}

	public String[] list() {
		if (applet.getPackageManager().hasSystemFeature(
				PackageManager.FEATURE_CAMERA)) {
			int nOfCameras = Camera.getNumberOfCameras();
			for (int i = 0; i < nOfCameras; ++i) {
				Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
				Camera.getCameraInfo(i, cameraInfo);
				if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
					camerasList.add(String.format(KEY_FRONT_CAMERA, i));
				} else {
					// Back Camera
					camerasList.add(String.format(KEY_BACK_CAMERA, i));
				}
			}
			String[] array = new String[nOfCameras];
			camerasList.toArray(array);
			return array;
		}
		return null;
	}

	private void startPreview(SurfaceHolder mHolder) {

		if (mHolder.getSurface() == null) {
			// preview surface does not exist
			return;
		}

		// stop preview before making changes
		try {
			mCamera.stopPreview();
		} catch (Exception e) {
			// ignore: tried to stop a non-existent preview
		}

		// set preview size and make any resize, rotate or
		// reformatting changes here

		// start preview with new settings
		try {
			mCamera.setPreviewTexture(mSurfaceTexture);
			mCamera.startPreview();
			log("Started the preview");
		} catch (Exception e) {
			Log.d("PROCESSING",
					"Error starting camera preview: " + e.getMessage());
			e.printStackTrace();
		}
	}	

	static class CameraHandler extends Handler {
        public static final int MSG_SET_SURFACE_TEXTURE = 0;
        public static final int MSG_START_CAMERA = 1;

        // Weak reference to the Activity; only access this from the UI thread.
        private CameraHandlerCallback callback;

        public CameraHandler(CameraHandlerCallback c) {
        	callback = c;
        }

        @Override  // runs on UI thread
        public void handleMessage(Message inputMessage) {
            int what = inputMessage.what;
            log("CameraHandler [" + this + "]: what=" + what);

//            MainActivity activity = mWeakActivity.get();
            if (callback == null) {
                return;
            }

            switch (what) {
                case MSG_SET_SURFACE_TEXTURE:
                	callback.handleSetSurfaceTexture((SurfaceTexture) inputMessage.obj);
                    break;
                case MSG_START_CAMERA:
                	callback.startCamera();
                	break;
                default:
                    throw new RuntimeException("unknown msg " + what);
            }
        }
    }

	public Camera getCamera() {
		return mCamera;
	}

	public static void printCompatibleResolutionsList(Capture capture) {
		Camera camera = capture.getCamera();
		boolean selfOpen = false;
		if (camera == null) {
			camera = Camera.open(0);
			selfOpen = true;
		}
			
		List<Camera.Size> sizes = camera.getParameters()
				.getSupportedPreviewSizes();
		for (Size size : sizes) {
			System.out.println(size.width + "x" + size.height);
		}
		if (selfOpen) {
			camera.release();
		}
	}

	@Override
	public void handleSetSurfaceTexture(SurfaceTexture st) {}
	
	@Override
	public void startCamera() {
		System.out.println("Start Camera Impl");
		startCameraImpl(0);
	};

	@Override
	public void onFrameAvailable(final SurfaceTexture surfaceTexture) {
		glView.queueEvent(new Runnable() {
			@Override
			public void run() {
				System.out.println("onFrameAvailable");
				surfaceTexture.updateTexImage();
				
				GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBuffers.get(0));
				GLES20.glViewport(0, 0, width, height);
				surfaceTexture.getTransformMatrix(mSTMatrix);
				mFullScreen.drawFrame(mTextureId, mSTMatrix);
				
				getImage(false);

				/*
				pixelBuffer.position(0);
				GLES20.glReadPixels(0, 0, width, height, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, pixelBuffer);
				pixelBuffer.position(0);
				pixelBuffer.get(Capture.this.pixels);
				updatePixels(); */
				
				//Fall back to default frame buffer
				GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
			}
		});
	}

	public void prepareFrameBuffers() {
		
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
		//No sure if this is required in opengl 2. Ignoring as of now.
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
	
	//The following method has been copied from Syphon library for processing
	public void getImage(boolean loadPixels) {
        
	    if (destpg == null || destpg.width != width || destpg.height != height) {
	    	    destpg = (PGraphicsOpenGL) parent.createGraphics(width, height, PConstants.P2D);
	    	    destpg.pgl.setGlThread(Thread.currentThread());
	    }
	    
	    destpg.beginDraw();
	    destpg.background(0, 0);
	    PGL pgl = destpg.beginPGL();
	    pgl.drawTexture(PGL.TEXTURE_2D, customTexture.get(0), width, height,
	                    0, 0, width, height);
	    destpg.endPGL();
	    destpg.endDraw();

	    // Uses the PGraphics texture as the cache object for the image
	    Texture tex = destpg.getTexture();
	    pg.setCache(this, tex);
	    if (loadPixels) {
	      this.loadPixels();
	      tex.get(this.pixels);
	      this.setLoaded(false);
	    }
	}
}
