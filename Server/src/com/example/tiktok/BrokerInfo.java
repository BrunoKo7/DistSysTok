package com.example.tiktok;

import java.util.ArrayList;
import java.util.HashMap;

public class BrokerInfo {

	HashMap<String, ArrayList<Publisher>> brokerTopics; // <brokerID, ArrayList<RegisteredPublishers>>
	HashMap<String, ArrayList<Consumer>> subscribers; // <brokerID, ArrayList<RegisteredConsumers>>

	public BrokerInfo() {
		this.brokerTopics = new HashMap<>();
		this.subscribers = new HashMap<>();
	}

	public void addPublisher(String brokerID, Publisher publisher) {

		if (!this.brokerTopics.containsKey(brokerID)) {
			ArrayList<Publisher> arraylist = new ArrayList<>();
			arraylist.add(publisher);
			this.brokerTopics.put(brokerID, arraylist);
		} else {
			this.brokerTopics.get(brokerID).add(publisher);
		}
	}

	public void addHashTag(String brokerID, String publisher, ArrayList<String> hashTagsPublished) {

		ArrayList<Publisher> tempArrayList = this.brokerTopics.get(brokerID);
		for (Publisher p : tempArrayList) {
			if (p.getChannelName().equals(publisher)) {
				p.addHashTagsPublished(hashTagsPublished);
				break;
			}
		}
	}

	// Receives a topic and returns for every Broker its Publishers that have posted
	// videos related to the topic.
	public HashMap<String, ArrayList<String>> getInfoAboutTopic(String topic) {

		HashMap<String, ArrayList<String>> infoAboutTopic = new HashMap<>();

		for (String brokerID : this.brokerTopics.keySet()) {
			ArrayList<Publisher> temp = this.brokerTopics.get(brokerID);
			boolean flag = false;
			for (Publisher p : temp) {
				if (p.getHashTagsPublished().contains(topic) || p.getChannelName().equals(topic)) {
					if (!flag) {
						ArrayList<String> arraylist = new ArrayList<>();
						arraylist.add(p.getChannelName());
						infoAboutTopic.put(brokerID, arraylist);
						flag = true;
					} else {
						infoAboutTopic.get(brokerID).add(p.getChannelName());
					}
				}
			}
		}
		return infoAboutTopic;
	}

	public HashMap<String, ArrayList<Publisher>> getBrokerTopics() {
		return this.brokerTopics;
	}

	public void updateBrokerTopics(HashMap<String, ArrayList<Publisher>> brokerTopics) {
		this.brokerTopics = brokerTopics;
	}

	public void addConsumer(String brokerID, Consumer consumer) {

		if (!this.subscribers.containsKey(brokerID)) {
			ArrayList<Consumer> arraylist = new ArrayList<>();
			arraylist.add(consumer);
			this.subscribers.put(brokerID, arraylist);
		} else {
			this.subscribers.get(brokerID).add(consumer);
		}

	}

	public void addInterests(String brokerID, String consumer, String interest) {

		ArrayList<Consumer> tempArrayList = this.subscribers.get(brokerID);
		for (Consumer c : tempArrayList) {
			if (c.getChannelName().equals(consumer)) {
				c.addTopic(interest);
				break;
			}
		}
	}

	public HashMap<String, ArrayList<String>> getInfoAboutSubscribers(ArrayList<String> topics) {

		HashMap<String, ArrayList<String>> infoAboutSubscribers = new HashMap<>();

		for (String brokerID : this.subscribers.keySet()) {
			ArrayList<Consumer> temp = this.subscribers.get(brokerID);
			int i = 0;
			boolean flag = false;
			for (Consumer c : temp) {
				for (String str : c.getTopics()) {
					if (topics.contains(str)) {
						if (!flag) {
							ArrayList<String> arraylist = new ArrayList<>();
							arraylist.add(c.getChannelName());
							infoAboutSubscribers.put(brokerID, arraylist);
							flag = true;
						} else {
							infoAboutSubscribers.get(brokerID).add(c.getChannelName());
						}
						break;
					}
				}
				i++;
			}
		}
		return infoAboutSubscribers;
	}

	public HashMap<String, ArrayList<Consumer>> getSubscribers() {
		return this.subscribers;
	}

	public void updateSubscribers(HashMap<String, ArrayList<Consumer>> subscribers) {
		this.subscribers = subscribers;
	}

	public void printBrokerTopics() {

		for (String brokerID : this.brokerTopics.keySet()) {
			for (Publisher p : this.brokerTopics.get(brokerID)) {
				System.out.println(brokerID + " has " + p.getChannelName());
			}
		}
	}

	public void printSubscribers() {

		for (String brokerID : this.brokerTopics.keySet()) {
			for (Consumer c : this.subscribers.get(brokerID)) {
				System.out.println(brokerID + " has " + c.getChannelName());
			}
		}
	}

	public void printHashMapWithString(HashMap<String, ArrayList<String>> hashmap) {

		for (String brokerID : hashmap.keySet()) {
			for (String str : hashmap.get(brokerID)) {
				System.out.println(brokerID + " has " + str);
			}
		}
	}

	// Testing purposes
	public static void main(String[] args) { // Example : brokerTopics data

		String brokerID = "127.0.0.1:4321";
		BrokerInfo brinfo = new BrokerInfo();
		brinfo.addPublisher(brokerID, new Publisher("Aggelos"));
		brinfo.addPublisher("127.0.0.1:4322", new Publisher("Alex"));
		brinfo.addPublisher(brokerID, new Publisher("Nikos"));
		brinfo.printBrokerTopics(); // a publisher pushes a video with a specific video
		ArrayList<String> topics = new ArrayList<>();
		topics.add("#viral");
		brinfo.addHashTag(brokerID, "Aggelos", topics);
		brinfo.addHashTag(brokerID, "Nikos", topics);
		brinfo.addHashTag("127.0.0.1:4322", "Alex", topics);
		brinfo.printHashMapWithString(brinfo.getInfoAboutTopic("#viral"));

		// Example : subscribers data
		brinfo.addConsumer(brokerID, new Consumer("Brouno"));
		brinfo.addConsumer(brokerID, new Consumer("Nikos"));
		brinfo.addConsumer("127.0.0.1:4322", new Consumer("Aggelos"));
		brinfo.addConsumer("127.0.0.1:4323", new Consumer("Alex"));
		brinfo.printSubscribers();
		brinfo.addInterests(brokerID, "Brouno", "Aggelos");
		brinfo.addInterests(brokerID, "Nikos", "Aggelos");
		brinfo.addInterests("127.0.0.1:4322", "Aggelos", "#viral");
		brinfo.addInterests(brokerID, "Brouno", "#viral");
		brinfo.addInterests(brokerID, "Nikos", "#viral");
		ArrayList<String> interests = new ArrayList<>();
		interests.add("Aggelos");
		interests.add("#viral");
		brinfo.printHashMapWithString(brinfo.getInfoAboutSubscribers(interests));
	}

}