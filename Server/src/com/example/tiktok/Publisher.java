package com.example.tiktok;

import java.io.Serializable;
import java.util.ArrayList;

public class Publisher implements Serializable {

	private static final long serialVersionUID = 12L;

	private String channelName;
	private ArrayList<String> hashTagsPublished;

	public Publisher(String channelName) {
		this.channelName = channelName;
		this.hashTagsPublished = new ArrayList<>();
	}

	public String getChannelName() {
		return this.channelName;
	}

	public ArrayList<String> getHashTagsPublished() {
		return this.hashTagsPublished;
	}

	public void addHashTagsPublished(ArrayList<String> newHashTagsPublished) {
		// Avoid duplication
		for (String hashTagsPublished : newHashTagsPublished) {
			if (!this.hashTagsPublished.contains(hashTagsPublished)) {
				this.hashTagsPublished.add(hashTagsPublished);
			}
		}
	}

	public void addHashTag(String topic) {
		this.hashTagsPublished.add(topic);
	}

}