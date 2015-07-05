package in.omerjerk.processing.video.android;

import processing.core.PApplet;

public class Movie extends VideoBase {
	
	public Movie(PApplet parent) {
		this(parent, -1, -1);
	}
	
	public Movie(PApplet parent, int width, int height) {
		if (width == -1 || height == -1) {
			width = 720;
			height = 1280;
		}
		init(width, height, ARGB);
	}
}
