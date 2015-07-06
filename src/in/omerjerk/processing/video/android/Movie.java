package in.omerjerk.processing.video.android;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import processing.core.PApplet;

public class Movie extends VideoBase implements MediaPlayerHandlerCallback {
    
    private MediaPlayerHandler handler;
    
    public interface MediaPlayerHandlerCallback {
        public void start();
    }
	
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
	    
	    public static final int MSG_START_MEDIA_PLAYER = 0;
	    
	    MediaPlayerHandlerCallback callback;
	    
	    public void setCallback (MediaPlayerHandlerCallback cb) {
	        this.callback = cb;
	    }
	    
	    @Override
	    public void handleMessage(Message msg) {
	        switch (msg.what) {
            case MSG_START_MEDIA_PLAYER:
                callback.start();
                break;
            default:
                break;
            }
	    }
	}
}
