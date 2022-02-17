package Main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import java.io.*;
import java.net.*;

public class Publisher implements Runnable {

	private Socket socket = null;
	private ObjectOutputStream objOS;
	private ObjectInputStream objIS;
	private String ipAddress;
	private int port;

	private ChannelName channelName;
	private ArrayList<String> brokersInfo;
	private HashFunctionMD5 hash = new HashFunctionMD5();
	private HashMap<String, ArrayList<String>> temp; // Subscriber list received from Broker.

	public Thread thread;

	public Publisher(ChannelName channelName) {
		this.channelName = channelName;
	}

	public void start() {
		this.thread = new Thread(this);
		this.thread.start();
	}

	// Receives the IP and Port number of a random Broker.
	// The Broker sends back all Brokers' information.
	// If the initial connection was not the appropriate (according to the
	// channelName's hash), the consumer connects to the correct Broker.
	public void connect(String ipAddress, int port) {
		try {
			this.socket = new Socket(InetAddress.getByName(ipAddress), port);

			this.objOS = new ObjectOutputStream(socket.getOutputStream());
			this.objIS = new ObjectInputStream(socket.getInputStream());

			Message message = new Message('P', this.channelName.getChannelName(), "GBI");

			this.objOS.writeObject(message);
			this.objOS.flush();

			brokersInfo = (ArrayList<String>) this.objIS.readObject();
			System.out.println(">Consumer: brokersInfo well-received");
			for (String str : brokersInfo) {
				System.out.println(str);
			}

			hash.hashByteintoInt(brokersInfo);

			String chosenBroker = hash.chooseBroker(hash.hashFunction(this.channelName.getChannelName()));
			this.ipAddress = ipAddress;
			this.port = port;

			if (!chosenBroker.equals(ipAddress + ":" + String.valueOf(port))) {
				disconnect();

				this.socket = new Socket(InetAddress.getByName(chosenBroker.split(":")[0]),
						Integer.parseInt((chosenBroker.split(":"))[1]));
				this.ipAddress = chosenBroker.split(":")[0];
				this.port = Integer.parseInt((chosenBroker.split(":"))[1]);

				this.objOS = new ObjectOutputStream(socket.getOutputStream());
				this.objIS = new ObjectInputStream(socket.getInputStream());
			}

			Message nmessage = new Message('P', this.channelName.getChannelName(), "NP");

			this.objOS.writeObject(nmessage);
			this.objOS.flush();
			start();

		} catch (UnknownHostException uException) {
			uException.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	public void disconnect() {
		try {
			objOS.writeObject("BYE");
			objOS.flush();
			objOS.close();
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// Receives the videoFile to be sent.
	// Asks the Broker for the subscriber list.
	// Sends to each Broker who's responsible for some of the subscribers the
	// videoFileChunks through the ActionsForServer class.
	public void push(VideoFile videoFile) {

		try {
			channelName.addVideo(videoFile.getVideoName(), videoFile);

			// Publisher requests it's subscribers from broker for a specific set of
			// hashtags.
			Message message = new Message('P', this.channelName.getChannelName(), "RSL");

			System.out.println(">Publisher " + this.channelName.getChannelName() + " sends message: RSL");

			this.objOS.writeObject(message);
			this.objOS.flush();

			this.objOS.writeObject(videoFile.getAssociatedHashTags());
			this.objOS.flush();

			Thread.sleep(1000);

			HashMap<String, ArrayList<String>> subscribersInfo = this.getHashMap();
			System.out.println(">Publisher " + this.channelName.getChannelName() + "  receives subscribers list!");

			Set<String> brokerWithSubscribers = subscribersInfo.keySet();

			for (String brokerID : brokerWithSubscribers) {
				System.out.println("BrokerID " + brokerID);
				if (brokerID.equals(ipAddress + ":" + String.valueOf(port))) {
					Thread actionsForPublisher = new ActionsForPublisher(this.objOS, videoFile, this.channelName, "NV",
							null, null);
					actionsForPublisher.start();
				} else {
					Socket socket = new Socket(InetAddress.getByName(brokerID.split(":")[0]),
							Integer.parseInt((brokerID.split(":"))[1]));
					ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
					ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());

					Thread actionsForPublisher = new ActionsForPublisher(out, videoFile, this.channelName, "NV", null,
							null);
					actionsForPublisher.start();

					actionsForPublisher.join();
					in.close();
					out.close();
					socket.close();
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	// Thread implementation part.
	// The Publisher can listen to multiple Brokers in parallel and receive the
	// Broker's messages.
	public void run() {
		System.out.println("Waiting for message");
		while (true) {

			try {
				Message message = (Message) this.objIS.readObject();
				if (message.getFrom() == 'B') {

					if (message.getRequestType().equals("RT")) {
						System.out.println(">Publisher gets request type RT");
						String topic = this.objIS.readUTF();
						String consumer = this.objIS.readUTF();
						
						Thread actionsForPublisher = new ActionsForPublisher(this.objOS, null, this.channelName, "RT",
								topic, consumer);
						actionsForPublisher.start();
					}

					if (message.getRequestType().equals("RSL")) {
						this.temp = (HashMap<String, ArrayList<String>>) this.objIS.readObject();
					}

				}

			} catch (IOException e) {
			} catch (ClassNotFoundException e) {
			}
		}
	}

	public HashMap<String, ArrayList<String>> getHashMap() {
		return this.temp;
	}
}