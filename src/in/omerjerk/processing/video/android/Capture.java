package in.omerjerk.processing.video.android;

import java.util.ArrayList;
import java.util.List;

import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.util.Log;
import android.view.SurfaceHolder;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PImage;

@SuppressWarnings("deprecation")
public class Capture extends PImage implements PConstants,
		SurfaceHolder.Callback {

	private static final boolean DEBUG = true;

	public static void log(String log) {
		if (DEBUG)
			System.out.println(log);
	}

	private PApplet applet;

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

	public Capture(PApplet applet, int width, int height) {
		this.applet = applet;
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
			mCamera.setDisplayOrientation(90);
			if (applet.canDraw())
				startPreview(applet.getSurfaceHolder());
		} catch (Exception e) {
			log("Couldn't open the Camera");
			e.printStackTrace();
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
