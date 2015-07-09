package in.omerjerk.processing.video.android.callbacks;

import android.content.res.AssetFileDescriptor;

public interface MediaPlayerHandlerCallback {
    public void startPlayer();
    public void initPlayer(AssetFileDescriptor fileName);
}
