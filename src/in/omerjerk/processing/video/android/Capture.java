package in.omerjerk.processing.video.android;

import java.util.ArrayList;
import java.util.List;

import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.opengl.GLSurfaceView;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.TextureView.SurfaceTextureListener;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.core.PImage;
import processing.core.PApplet;
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
	private Camera.Parameters parameters;

	private int previewWidth, previewHeight;

	private static ArrayList<String> camerasList = new ArrayList<String>();

	private static final String KEY_FRONT_CAMERA = "front-camera-%d";
	private static final String KEY_BACK_CAMERA = "back-camera-%d";

	private int selectedCamera = -1;
	
	private SurfaceTexture mSurfaceTexture;
	private Texture mTexture;

	public Capture(PApplet context) {
		this(context, -1, -1);
	}

	public Capture(final PApplet applet, int width, int height) {
		this.applet = applet;
		this.width = width;
		this.height = height;
		applet.registerMethod("pause", this);
		applet.registerMethod("resume", this);
		
		mTexture = (Texture) applet.g.getCache(this);
		if (mTexture != null) {
			System.out.println("glname = " + mTexture.glName);
			mSurfaceTexture = new SurfaceTexture(mTexture.glName);
			mSurfaceTexture.setOnFrameAvailableListener(this);
		}
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
		// If the preview can change or rotate, take care of those events
		// here.
		// Make sure to stop the preview before resizing or reformatting it.

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
		List<Camera.Size> sizes = camera.getParameters()
				.getSupportedPreviewSizes();
		for (Size size : sizes) {
			System.out.println(size.width + "x" + size.height);
		}
	}

	@Override
	public void handleSetSurfaceTexture(SurfaceTexture st) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onFrameAvailable(final SurfaceTexture surfaceTexture) {
		// TODO Auto-generated method stub
		System.out.println("OnFrameAvailable");
		((GLSurfaceView) applet.getSurfaceView()).queueEvent(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				surfaceTexture.updateTexImage();
				
			}
		});
		
	}

}
