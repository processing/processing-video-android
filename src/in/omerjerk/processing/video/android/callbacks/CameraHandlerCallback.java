package in.omerjerk.processing.video.android.callbacks;

import android.graphics.SurfaceTexture;

public interface CameraHandlerCallback {
    public void handleSetSurfaceTexture(SurfaceTexture texture);
    public void startCamera(Integer cameraId);
    public void startPreview();
    public void stopCamera();
}
