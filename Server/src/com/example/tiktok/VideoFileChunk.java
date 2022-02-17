package com.example.tiktok;

import java.io.Serializable;

public class VideoFileChunk implements Serializable {

	private static final long serialVersionUID = 6L;
	private String videoName;
	private int order;
	private char mC; // more chunks like more fragments in networking
	private byte[] chunk;
	private String message;

	public VideoFileChunk(String videoName, int order, char mC, byte[] chunk) {
		this.videoName = videoName;
		this.order = order;
		this.mC = mC;
		this.chunk = chunk;
	}

	public String getvideoName() {
		return this.videoName;
	}

	public int getorder() {
		return this.order;
	}

	public char getMC() {
		return this.mC;
	}

	public byte[] getChunk() {
		return this.chunk;
	}

	public String getMessage() {
		return this.message;
	}
}