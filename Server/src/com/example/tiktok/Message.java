package com.example.tiktok;

import java.io.Serializable;

public class Message implements Serializable {

	private static final long serialVersionUID = 46L;

	private char from; // C-> consumer, P-> publisher, B-> broker
	private String channelName;
	private String requestType;
	// RV -> requested video
	// RSL -> request subscribers list
	// RPL -> request publisher list
	// NV-> new video
	// NC -> new consumer
	// NP -> new publisher, NC -> new consumer
	// RT -> requested topic
	// GBI -> get broker information
	// ST -> set topic

	public Message(char from, String channelName, String requestType) {
		this.from = from;
		this.channelName = channelName;
		this.requestType = requestType;
	}

	public char getFrom() {
		return this.from;
	}

	public String getChannelName() {
		return this.channelName;
	}

	public String getRequestType() {
		return this.requestType;
	}
}