package in.omerjerk.processing.video.android;

import in.omerjerk.processing.video.android.callbacks.MediaPlayerHandlerCallback;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.view.Surface;
import processing.core.PApplet;

public class Movie extends VideoBase implements MediaPlayerHandlerCallback {
    
    private MediaPlayerHandler handler;
    private MediaPlayer player;
    
    private boolean looping = false;
	
	public Movie(PApplet parent, String fileName) {
	    super(parent);
	    MediaMetadataRetriever metaRetriever = new MediaMetadataRetriever();
	    metaRetriever.setDataSource(fileName);
	    String height = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
	    String width = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
	    init(Integer.valueOf(width), Integer.valueOf(height), ARGB);
	    
	    HandlerThread backgroundThread = new HandlerThread("MediaPlayer");
	    backgroundThread.start();
	    handler = new MediaPlayerHandler(backgroundThread.getLooper());
	    handler.setCallback(this);
	    handler.sendMessage(handler.obtainMessage(MediaPlayerHandler.MSG_INIT_PLAYER, fileName));
	}
	
	public void play() {
	    handler.sendMessage(handler.obtainMessage(MediaPlayerHandler.MSG_START_PLAYER));
	}
	
	public void loop() {
	    looping = true;
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
	    
	    public MediaPlayerHandler(Looper looper) {
            super(looper);
        }

        public void setCallback (MediaPlayerHandlerCallback cb) {
	        this.callback = cb;
	    }
	    
	    @Override
	    public void handleMessage(Message msg) {
	        switch (msg.what) {
	        case MSG_INIT_PLAYER:
	            String fileName = (String) msg.obj;
	            callback.initPlayer(fileName);
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
	public void initPlayer(String fileName) {
	    player = new MediaPlayer();
	    try {
            player.setDataSource(activity, Uri.parse(fileName));
            player.setSurface(new Surface(mSurfaceTexture));
            player.setLooping(looping);
            player.prepare();
        } catch (Exception e) {
            e.printStackTrace();
        }
	}
	
	@Override
	public void startPlayer() {
	    player.start();
	}
}
