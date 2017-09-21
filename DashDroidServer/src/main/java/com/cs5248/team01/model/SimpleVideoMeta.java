package com.cs5248.team01.model;

public class SimpleVideoMeta {
	private String name;
	private String thumbnailBase64;
	
	public SimpleVideoMeta(String name, String thumbnail) {
		this.name = name;
		this.thumbnailBase64 = thumbnail;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return this.name;
	}
	
	public void setThumbnailBase64(String t) {
		this.thumbnailBase64 = t;
	}
	
	public String getThumbnailBase64() {
		return this.thumbnailBase64;
	}
	
}
