package in.omerjerk.processing.video.android;

import java.util.ArrayList;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PImage;

@SuppressWarnings("deprecation")
public class Capture extends PImage implements PConstants {
	
	private static Context context;
	
	private static ArrayList<String> camerasList = new ArrayList<String>();
	
	private static final String KEY_FRONT_CAMERA = "front-camera-%d";
	private static final String KEY_BACK_CAMERA = "back-camera-%d";
	
	private int selectedCamera = 0;
	
	public Capture (PApplet context) {
		this(context, null);
	}
	
	public Capture (PApplet context, String camera) {
		Capture.context = context;
		if (camera == null || camera.equals("")) {
			selectedCamera = 0;
		} else {
			selectedCamera = camerasList.indexOf(camera);
		}
	}

	public static String[] list() {
		ensureContext();
		if (true) {
			int nOfCameras = Camera.getNumberOfCameras();
			for (int i = 0; i < nOfCameras; ++i) {
				Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
				Camera.getCameraInfo(i, cameraInfo);
				if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
					camerasList.add(String.format(KEY_FRONT_CAMERA, i));
				} else {
					//Back Camera
					camerasList.add(String.format(KEY_BACK_CAMERA, i));
				}
			}
			String[] array = new String[nOfCameras];
			camerasList.toArray(array);
			return array;
		}
		return null;
	}
	
	private static void ensureContext() {
		if (context == null) {
			context = PApplet.getInstance();
			if (context == null) {
				throw new RuntimeException("Create the instance of Capture class first.");
			}			
		}
	}
}