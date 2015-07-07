package in.omerjerk.processing.video.android;

import in.omerjerk.processing.video.android.callbacks.MediaPlayerHandlerCallback;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.Surface;
import processing.core.PApplet;

public class Movie extends VideoBase implements MediaPlayerHandlerCallback {
    
    private MediaPlayerHandler handler;
    private MediaPlayer player;
	
	public Movie(PApplet parent) {
		this(parent, -1, -1);
	}
	
	public Movie(PApplet parent, int width, int height) {
	    super(parent);
	    new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                handler = new MediaPlayerHandler();
                Looper.loop();
            }
        }).start();
	}
	
	@Override
	public void onPause() {
	}
	
	@Override
	public void onResume() {}
	
	private class MediaPlayerHandler extends Handler {
	    
	    public static final int MSG_INIT_PLAYER = 0;
	    public static final int MSG_START_PLAYER = 1;
	    
	    MediaPlayerHandlerCallback callback;
	    
	    public void setCallback (MediaPlayerHandlerCallback cb) {
	        this.callback = cb;
	    }
	    
	    @Override
	    public void handleMessage(Message msg) {
	        switch (msg.what) {
	        case MSG_INIT_PLAYER:
	            callback.initPlayer();
	            break;
            case MSG_START_PLAYER:
                callback.startPlayer();
                break;
            default:
                break;
            }
	    }
	}
	
	@Override
	public void initPlayer() {
	    player = new MediaPlayer();
	    player.setSurface(new Surface(mSurfaceTexture));
	}
	
	@Override
	public void startPlayer() {
	}
}
