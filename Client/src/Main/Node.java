package Main;

import java.io.IOException;

public class Node {

	private Consumer consumer;
	private Publisher publisher;
	private ChannelName channelName;

	public Node(String name) {
		this.channelName = new ChannelName(name);
		this.consumer = new Consumer(this.channelName.getChannelName());
		this.publisher = new Publisher(this.channelName);
	}

	public void connectPublisher(String ipAddress, int port) throws IOException {
		this.publisher.connect(ipAddress, port);
	}

	public void connectConsumer(String ipAddress, int port) {
		this.consumer.connect(ipAddress, port);
	}

	public Consumer getConsumer() {
		return this.consumer;
	}

	public Publisher getPublisher() {
		return this.publisher;
	}
}