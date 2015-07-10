package in.omerjerk.processing.video.android;

import java.io.IOException;

import in.omerjerk.processing.video.android.callbacks.MediaPlayerHandlerCallback;
import android.content.res.AssetFileDescriptor;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
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
	    AssetFileDescriptor afd = null;
	    try {
            afd = activity.getAssets().openFd(fileName);
            MediaMetadataRetriever metaRetriever = new MediaMetadataRetriever();
            metaRetriever.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            String height = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
            String width = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
            init(Integer.valueOf(width), Integer.valueOf(height), ARGB);
        } catch (IOException e) {
            e.printStackTrace();
        }
	    
	    initalizeFrameBuffer();
	    
	    HandlerThread backgroundThread = new HandlerThread("MediaPlayer");
	    backgroundThread.start();
	    handler = new MediaPlayerHandler(backgroundThread.getLooper());
	    handler.setCallback(this);
	    handler.sendMessage(handler.obtainMessage(MediaPlayerHandler.MSG_INIT_PLAYER, afd));
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
	public void onResume() {
	    initalizeFrameBuffer();
	}
	
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
	            AssetFileDescriptor afd = (AssetFileDescriptor) msg.obj;
	            callback.initPlayer(afd);
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
	public void initPlayer(AssetFileDescriptor afd) {
	    player = new MediaPlayer();
	    try {
            player.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            System.out.println("texture id = " + mTextureId);
            while (mTextureId == 0) {
                Thread.sleep(100);
            }
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
