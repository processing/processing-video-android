package in.omerjerk.processing.video.android;

import in.omerjerk.processing.video.android.callbacks.CameraHandlerCallback;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import processing.core.PApplet;

@SuppressWarnings("deprecation")
public class Capture extends VideoBase implements CameraHandlerCallback {

	private Camera mCamera;

	private static ArrayList<String> camerasList = new ArrayList<String>();

	private static final String KEY_FRONT_CAMERA = "front-camera-%d";
	private static final String KEY_BACK_CAMERA = "back-camera-%d";

	private int selectedCamera = -1;

	private CameraHandler mCameraHandler;

	public Capture(PApplet parent) {
		this(parent, -1, -1);
	}

	public Capture(final PApplet parent, int width, int height) {
		super(parent);
		if (width == -1 || height == -1) {
		    width = 720;
		    height = 720;
		}
		init(width, height, ARGB);
		initalizeFrameBuffer();

		activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mCameraHandler = new CameraHandler(Capture.this);
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
		mCameraHandler.sendMessage(mCameraHandler.obtainMessage(
				CameraHandler.MSG_START_CAMERA, new Integer(selectedCamera)));
		mCameraHandler.sendMessage(mCameraHandler.obtainMessage(
				CameraHandler.MSG_SET_SURFACE_TEXTURE, mSurfaceTexture));
	}

	public void start() {
		mCameraHandler.sendMessage(mCameraHandler.obtainMessage(
				CameraHandler.MSG_START_PREVIEW));
	}

	@Override
	public void onPause() {
		log("pause called");
		isAvailable = false;
		if (mCamera != null) {
			mCamera.release();
        }
	}

	@Override
	public void onResume() {
		log("resume called");
		initalizeFrameBuffer();
		glView.queueEvent(new Runnable() {
			@Override
			public void run() {
		        //If camera is not null, the activity was started already and we're coming back from a pause.
				if (mCamera != null) {
					log("Starting Camera in resume");
					mCameraHandler.sendMessage(mCameraHandler.obtainMessage(
							CameraHandler.MSG_START_CAMERA, new Integer(selectedCamera)));
					mCameraHandler.sendMessage(mCameraHandler.obtainMessage(
							CameraHandler.MSG_SET_SURFACE_TEXTURE, mSurfaceTexture));
					mCameraHandler.sendMessage(mCameraHandler.obtainMessage(
							CameraHandler.MSG_START_PREVIEW));
				}
			}
		});
	}
	
	public static String[] list() {
		//The following check has to be commented to make list() method static
//		if (applet.getPackageManager().hasSystemFeature(
//				PackageManager.FEATURE_CAMERA)) {
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
//		}
//		return null;
	}

	static class CameraHandler extends Handler {
        public static final int MSG_SET_SURFACE_TEXTURE = 0;
        public static final int MSG_START_CAMERA = 1;
        public static final int MSG_STOP_CAMERA = 2;
        public static final int MSG_START_PREVIEW = 3;

        // Weak reference to the Activity; only access this from the UI thread.
        private CameraHandlerCallback callback;

        public CameraHandler(CameraHandlerCallback c) {
        	callback = c;
        }

        @Override  // runs on UI thread
        public void handleMessage(Message inputMessage) {
            int what = inputMessage.what;

            if (callback == null) {
                return;
            }

            switch (what) {
                case MSG_SET_SURFACE_TEXTURE:
                	callback.handleSetSurfaceTexture((SurfaceTexture) inputMessage.obj);
                    break;
                case MSG_START_CAMERA:
                	callback.startCamera((Integer) inputMessage.obj);
                	break;
                case MSG_START_PREVIEW:
                	callback.startPreview();
                	break;
                case MSG_STOP_CAMERA:
                	callback.stopCamera();
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
	public void handleSetSurfaceTexture(SurfaceTexture st) {
		try {
			mCamera.setPreviewTexture(mSurfaceTexture);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void startCamera(Integer cameraId) {
		System.out.println("Start Camera Impl");
		if (cameraId == null) {
			cameraId = 0;
		}
		try {
		    log("Starting camera with camera id = " + cameraId);
			mCamera = Camera.open(cameraId);
			mCamera.setDisplayOrientation(90);
		} catch (Exception e) {
			log("Couldn't open the Camera");
			e.printStackTrace();
		}
	}
	
	@Override
	public void stopCamera() {
		if (mCamera != null) {
			mCamera.release();
			mCamera = null;
		}
	}

	@Override
	public void startPreview() {
		if (parent.getSurfaceHolder().getSurface() == null) {
			// preview surface does not exist
			return;
		}

		// start preview with new settings
		try {
			mCamera.startPreview();
			log("Started the preview");
		} catch (Exception e) {
			Log.d("PROCESSING",
					"Error starting camera preview: " + e.getMessage());
			e.printStackTrace();
		}
	}
}
