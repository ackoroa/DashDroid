package com.cs5248.team01.model;

import org.apache.log4j.Logger;

public class HLS {
	final static Logger logger = Logger.getLogger(HLS.class.getSimpleName());
	
	private Video video;
	
	
	
	public HLS(Video video) {
		this.video = video;
	}
	
	private void addLine(StringBuilder b, String text) {
		b.append(text).append("\n");
	}
	public void writeHLS() {
		
		StringBuilder builder = new StringBuilder();
		addLine(builder, "#EXTM3U");
		addLine(builder, "#EXT-X-VERSION:5");
		addLine(builder, "");
		addLine(builder, "#EXT-X-STREAM-INF:BANDWIDTH=128000,CODECS=\"mp4a.40.2,avc1.4d401e\",RESOLUTION=426x240");
		//addLine(builder, createStreamFile())
		addLine(builder, "#EXT-X-STREAM-INF:BANDWIDTH=256000,CODECS=\"mp4a.40.2,avc1.4d401e\",RESOLUTION=640x360");
		//addLine(builder, createStreamFile())
		addLine(builder, "#EXT-X-STREAM-INF:BANDWIDTH=512000,CODECS=\"mp4a.40.2,avc1.4d401e\",RESOLUTION=854x480");
		//addLine(builder, createStreamFile())
		
		//write to file
		
	}
	
	public String createStreamFile() {
		StringBuilder builder = new StringBuilder();
		addLine(builder,  "#EXTM3U");
		addLine(builder, "");
		addLine(builder, "EXT-VERSION:3");
		addLine(builder, "EXT-X-MEDIA-SEQUENCE:0");
		addLine(builder, "EXT-X-TARGETDURATION:3");
		addLine(builder, "");
		//for each segments
		addLine(builder, "#EXTINF:3");
		addLine(builder, "URL");
		
		return "url path";
		
	}
	
	
	
	
}
