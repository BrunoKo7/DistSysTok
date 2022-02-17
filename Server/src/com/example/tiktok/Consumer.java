package com.example.tiktok;

import java.io.Serializable;
import java.util.ArrayList;

public class Consumer implements Serializable {

	private static final long serialVersionUID = 34L;

	private String channelName;
	private ArrayList<String> topics;

	public Consumer(String channelName) {
		this.channelName = channelName;
		this.topics = new ArrayList<>();
	}

	public ArrayList<String> getTopics() {
		return topics;
	}

	public void addTopic(String topic) {
		this.topics.add(topic);
	}

	public String getChannelName() {
		return this.channelName;
	}
}