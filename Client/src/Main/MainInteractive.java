package Main;

import java.io.IOException;
import java.util.Scanner;

public class MainInteractive {

	private Node node;

	public static void main(String[] args) {

		MainInteractive main = new MainInteractive();
		Scanner in = new Scanner(System.in);

		while (true) {
			System.out.println("Insert new command");
			String name = in.nextLine();
			switch (name) {

			case "C":
				System.out.println("Input Username");
				String userName = in.nextLine();
				main.connect(userName);
				break;

			case "FP":
				System.out.println("Input publisher name");
				String nameOfPublisher = in.nextLine();
				main.followPublisher(nameOfPublisher);
				break;
			case "PV":
				System.out.println("Input video name");
				String nameOfVideo = in.nextLine();
				main.pushVideo(nameOfVideo);
				break;
			case "RT":
				System.out.println("Input topic name");
				String topic = in.nextLine();
				main.requestTopic(topic);
				break;
			}

		}
	}

	public void connect(String userName) {
		this.node = new Node(userName);
		try {
			this.node.connectPublisher("127.0.0.1", 4322);
			this.node.connectConsumer("127.0.0.1", 4322);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void followPublisher(String name) {
		this.node.getConsumer().follow(name);
		this.node.getConsumer().requestATopic(name);
	}

	public void pushVideo(String nameOfVideo) {
		VideoFile nvideoFile = new VideoFile(nameOfVideo);
		if (nvideoFile.readCorrectly()) {
			node.getPublisher().push(nvideoFile);
		} else {
			System.err.println("Video with given name does not exist. Please push video again.");
		}
	}

	public void requestTopic(String topic) {
		this.node.getConsumer().requestATopic(topic);
	}
}