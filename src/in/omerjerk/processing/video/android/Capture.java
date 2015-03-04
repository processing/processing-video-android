package in.omerjerk.processing.video.android;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.view.Gravity;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PImage;

@SuppressWarnings("deprecation")
public class Capture extends PImage implements PConstants {

	private static final boolean DEBUG = true;

	public static void log(String log) {
		if (DEBUG)
			System.out.println(log);
	}

	private PApplet context;

	private Camera mCamera;
	private Camera.Parameters parameters;
	private Size previewSize;

	private int previewWidth, previewHeight;

	private static ArrayList<String> camerasList = new ArrayList<String>();

	private static final String KEY_FRONT_CAMERA = "front-camera-%d";
	private static final String KEY_BACK_CAMERA = "back-camera-%d";

	private int selectedCamera = 0;

	public Capture(PApplet context) {
		this(context, -1, -1);
	}

	public Capture(PApplet context, int width, int height) {
		this.context = context;
		this.width = width;
		this.height = height;
	}

	public void setCamera(String camera) {
		if (camera == null || camera.equals("")) {
			selectedCamera = 0;
		} else {
			selectedCamera = camerasList.indexOf(camera);
		}
		log("Selected camera = " + selectedCamera);
		try {
			mCamera = Camera.open(selectedCamera);
			createPreviewWindow();
		} catch (Exception e) {
			log("Couldn't open the Camera");
			e.printStackTrace();
		}
	}

	private void createPreviewWindow() {
		final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
				WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
				WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
						| WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
						| WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
						| WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH);

		params.gravity = Gravity.TOP | Gravity.LEFT;
		params.height = 1;
		params.width = 1;

		parameters = mCamera.getParameters();
		setPreviewSize();
		mCamera.setParameters(parameters);
		previewSize = parameters.getPreviewSize();
		init(previewSize.height, previewSize.width, ARGB);

		log("Width = " + previewSize.width);
		log("height = " + previewSize.height);
		final WindowManager windowManager = (WindowManager) context
				.getSystemService(Context.WINDOW_SERVICE);
		context.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				CameraPreview mPreview = new CameraPreview(context, mCamera);
				windowManager.addView(mPreview, params);
				mCamera.setPreviewCallback(previewCallback);
			}
		});
	}

	public String[] list() {
		if (true) {
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

	private Camera.PreviewCallback previewCallback = new Camera.PreviewCallback() {

		@Override
		public void onPreviewFrame(byte[] frame, Camera camera) {
			pixels = Utils.convertYUV420_NV21toRGB8888(frame,
					previewSize.width, previewSize.height);
			pixels = Utils.rotateRGBDegree90(pixels, previewSize.width,
					previewSize.height);
			updatePixels();
		}
	};

	private class CameraPreview extends SurfaceView implements
			SurfaceHolder.Callback {

		private Camera mCamera;
		private SurfaceHolder mHolder;

		public CameraPreview(Context context, Camera camera) {
			super(context);
			this.mCamera = camera;

			mHolder = getHolder();
			mHolder.addCallback(this);
			mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		}

		@Override
		public void surfaceCreated(SurfaceHolder holder) {
			try {
				mCamera.setPreviewDisplay(holder);
				mCamera.startPreview();
			} catch (IOException e) {
				Log.d("PROCESSING",
						"Error setting camera preview: " + e.getMessage());
				e.printStackTrace();
			}
		}

		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width,
				int height) {
			// If your preview can change or rotate, take care of those events
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
				mCamera.setPreviewDisplay(mHolder);
				mCamera.startPreview();

			} catch (Exception e) {
				Log.d("PROCESSING",
						"Error starting camera preview: " + e.getMessage());
				e.printStackTrace();
			}
		}

		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
			// do nothing
		}
	}

	private void setPreviewSize() {
		if (!(width == -1 || height == -1)) {
			parameters.setPreviewSize(previewWidth, previewHeight);
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
}
