package Main;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

public class Consumer implements Runnable {

	private Socket socket = null;
	private ObjectOutputStream objOS;
	private ObjectInputStream objIS;
	private String ipAddress;
	private int port;

	private String channelName; // Same as the Publisher channelName in the Node
	private ArrayList<String> topics; // Topics to which the Consumer is subscribed
	private ArrayList<String> brokersInfo; // Contains strings of the form IP+port_number of all Brokers
	private HashFunctionMD5 hash = new HashFunctionMD5();
	private HashMap<String, ArrayList<String>> temp;

	public Thread thread;

	public Consumer(String channelName) {
		this.channelName = channelName;
		this.topics = new ArrayList<>();
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

			Message message = new Message('C', this.channelName, "GBI");

			this.objOS.writeObject(message);
			this.objOS.flush();

			brokersInfo = (ArrayList<String>) this.objIS.readObject();
			System.out.println(">Consumer: brokersInfo well-received");
			for (String str : brokersInfo) {
				System.out.println(str);
			}

			hash.hashByteintoInt(brokersInfo);

			String chosenBroker = hash.chooseBroker(hash.hashFunction(this.channelName));
			this.ipAddress = ipAddress;
			this.port = port;

			if (!chosenBroker.equals(ipAddress + ":" + String.valueOf(port))) {
				disconnect();

				this.socket = new Socket(InetAddress.getByName(chosenBroker.split(":")[0]),
						Integer.parseInt((chosenBroker.split(":"))[1]));
				this.ipAddress = ipAddress;
				this.port = port;

				this.objOS = new ObjectOutputStream(socket.getOutputStream());
				this.objIS = new ObjectInputStream(socket.getInputStream());
			}

			Message nmessage = new Message('C', this.channelName, "NC");

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

	// Receives a topic a Consumer wants to follow.
	// Sends a SetTopic (ST) message to its Broker so that the Broker keeps track of
	// all Consumer subscriptions.
	public void follow(String name) {
		System.out.println(">Consumer " + this.channelName + " follows " + name);
		topics.add(name);
		try {
			Message message = new Message('C', this.channelName, "ST");
			System.out.println(">Consumer " + this.channelName + " sends message: ST");

			this.objOS.writeObject(message);
			this.objOS.flush();

			this.objOS.writeUTF(name);
			this.objOS.flush();

		} catch (IOException e) {
			System.err.println(">!<Error in Consumer.follow()>!<");
		}
	}

	// Receives a topic for which a Consumer wants to receive videoFiles.
	// Sends to the Broker a RequestPublisherList (RPL) message.
	// The Broker sends back all responsible Broker ID's and their Publishers that
	// have posted videos related to the topic. The consumer connects temporarily to
	// each one of them and requests the videos from them. When done, closes the
	// temporary connection.
	public void requestATopic(String topic) {
		System.out.println(">Consumer " + this.channelName + " requests " + topic);

		try {
			Message message = new Message('C', this.channelName, "RPL"); // Consumer requests a topic from broker
			System.out.println(">Consumer " + this.channelName + " sends message: RPL");

			this.objOS.writeObject(message);
			this.objOS.flush();

			this.objOS.writeUTF(topic);
			this.objOS.flush();

			Thread.sleep(1000);

			HashMap<String, ArrayList<String>> topicInfo = this.getHashMap(); // <brokerId, channelNamesWithTopicIwant>
			System.out.println(">Consumer receives publisher list!");
			Set<String> brokersWithTopic = topicInfo.keySet();

			for (String brokerID : brokersWithTopic) {

				Socket nsocket = new Socket(InetAddress.getByName(brokerID.split(":")[0]),
						Integer.parseInt((brokerID.split(":"))[1]));
				ObjectInputStream in = new ObjectInputStream(nsocket.getInputStream());
				ObjectOutputStream out = new ObjectOutputStream(nsocket.getOutputStream());
				ArrayList<VideoFileChunk> vfcl = request(in, out, topicInfo.get(brokerID), topic);

				in.close();
				out.close();
				nsocket.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

	// Receives the Consumer's streams and the Publishers that have posted videos
	// related to the topic. The Broker sends back the number of videos about to be
	// received. Then receives each video's videoFileChunks.
	public ArrayList<VideoFileChunk> request(ObjectInputStream objIStream, ObjectOutputStream objOStream,
			ArrayList<String> publisherList, String topic) {
		try {
			Message message = new Message('C', this.channelName, "RV");
			objOStream.writeObject(message);
			objOStream.flush();

			objOStream.writeObject(publisherList);
			objOStream.flush();

			objOStream.writeUTF(topic);
			objOStream.flush();

			int numberOfVids = objIStream.readInt();

			if (numberOfVids > 0) {

				ArrayList<VideoFileChunk> receivedChunks = new ArrayList<>();
				int videosReceived = 0;
				VideoFileChunk vfc = null;
				while (videosReceived < numberOfVids) {
					char mC = '1';
					while (mC == '1') {
						vfc = (VideoFileChunk) objIStream.readObject();
						receivedChunks.add(vfc);
						mC = vfc.getMC();
						System.out.println(">Consumer " + this.channelName + " receives video file chunk with orderID "
								+ vfc.getorder());
					}
					videosReceived++;
					
					Collections.sort(receivedChunks, new VideoFileChunkComparator());
					System.out.println(">Consumer: VideoFileChunks sorted.");
					VideoFile.saveVideoFileFromArrayList(receivedChunks, "RequestedVideo" + vfc.getvideoName() + ".mp4");
				}
				return receivedChunks;
			}

		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

	public void disconnect() {
		try {
			objOS.close();
			objIS.close();
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// Might be needed for the Android part of the project.
	public void playData(String input, VideoFile value) {

	}

	// Thread implementation part.
	// The consumer can listen to multiple Brokers in parallel and receive the
	// Broker's messages.
	public void run() {
		System.out.println("Waiting for message");
		while (true) {
			try {
				Message message = (Message) this.objIS.readObject();
				if (message.getFrom() == 'B') {

					if (message.getRequestType().equals("RPL")) {
						this.temp = (HashMap<String, ArrayList<String>>) this.objIS.readObject();
					}

					if (message.getRequestType().equals("SNV")) {
						System.out.println(">Consumer->" + this.channelName + ": New message!");
						ArrayList<VideoFileChunk> temp = new ArrayList<>();
						char mC = '1';
						System.out.println(">Consumer->" + this.channelName + ": Messages received:");
						while (mC == '1') {
							VideoFileChunk vfc = (VideoFileChunk) this.objIS.readObject();
							temp.add(vfc);
							mC = vfc.getMC();
							System.out.println(
									">Consumer: VideoFileChunk from broker received with orderID " + vfc.getorder());
						}
						Collections.sort(temp, new VideoFileChunkComparator());

						System.out.println(">Consumer: VideoFileChunks sorted.");
						VideoFile.saveVideoFileFromArrayList(temp, "ReceivedVideoFromPublisher.mp4");
					}
				}

			} catch (IOException e) {
				// e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// e.printStackTrace();
			}
			// System.out.println("Thread killed! BOOM");
		}
	}

	public HashMap<String, ArrayList<String>> getHashMap() {
		return this.temp;
	}
}