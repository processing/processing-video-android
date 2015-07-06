package in.omerjerk.processing.video.android;

import processing.core.PApplet;

public class Movie extends VideoBase implements MediaPlayerHandlerCallback {
    
    public interface MediaPlayerHandlerCallback {
        public void start();
    }
	
	public Movie(PApplet parent) {
		this(parent, -1, -1);
	}
	
	public Movie(PApplet parent, int width, int height) {
	    super(parent);
	}
	
	@Override
	public void onPause() {
	}
	
	@Override
	public void onResume() {}
}
