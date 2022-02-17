package com.example.tiktok;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;

public class ActionsForServer extends Thread {

	Socket socket;
	ObjectInputStream objectIS;
	ObjectOutputStream objectOS;

	private static String brokerID; // brokerID = ipAddress + port
	private static ArrayList<String> brokersInfo; // Stores all broker ID'S (ipAddress + port)
	private static ArrayList<String> brokersServerPartInfo;
	
	// Map a Consumer/Publisher object to its streams.
	private static HashMap<Consumer, ActionsForServer> consumerHashList = new HashMap<>();
	private static HashMap<Publisher, ActionsForServer> publisherHashList = new HashMap<>();

	private static HashFunctionMD5 hash = new HashFunctionMD5();
	private static BrokerInfo brokerInfo = new BrokerInfo();

	// Stores all temporary connections from consumers (AND PUBS?) connected to a
	// Broker who's not responsible for them, based on their channelName.
	private static HashMap<String, ActionsForServer> tempHashList = new HashMap<>();

	public ActionsForServer(Socket socket) {
		try {
			this.socket = socket;
			this.objectOS = new ObjectOutputStream(socket.getOutputStream());
			this.objectIS = new ObjectInputStream(socket.getInputStream());
			System.out.println("New thread");
		} catch (IOException e) {
			System.err.println("ServerSide - Error in ActionsForServer constructor.");
		}
	}

	public Publisher getPublisher(String channelName) {
		for (Publisher pub : ActionsForServer.publisherHashList.keySet()) {
			if (pub.getChannelName().equals(channelName)) {
				return pub;
			}
		}
		return null;
	}

	public Consumer getConsumer(String channelName) {
		for (Consumer con : ActionsForServer.consumerHashList.keySet()) {
			if (con.getChannelName().equals(channelName)) {
				return con;
			}
		}
		return null;
	}

	// Thread implementation part.
	// The Broker receives Messages from Publishers, Consumers or other Brokers, in
	// parallel, and serves their requests.
	public void run() {
		while (true) {
			try {
				Message message = (Message) objectIS.readObject();

				if (message.getFrom() == 'P') { // Messages from Publishers
					if (message.getRequestType().equals("NP")) {
						System.out.println(">Server: New Publisher recognized");
						Publisher newPublisher = new Publisher(message.getChannelName());
						ActionsForServer.publisherHashList.put(newPublisher, this);
						brokerInfo.addPublisher(brokerID, newPublisher);
						notifyBrokersAboutChanges();

					}

					// GBI -> get broker info
					if (message.getRequestType().equals("GBI")) {
						objectOS.writeObject(ActionsForServer.brokersInfo);
						objectOS.flush();
						System.out.println(">Server: Sending brokersInfo");

						hash.hashByteintoInt(brokersInfo);
						String chosenBroker = hash.chooseBroker(hash.hashFunction(message.getChannelName()));

						if (!chosenBroker.equals(brokerID)) {
							objectIS.close();
							objectOS.close();
							socket.close();
						}
					}

					// RSL -> request subscribers list
					if (message.getRequestType().equals("RSL")) {
						String mChannelName = message.getChannelName();
						System.out.println(">Server: Publisher " + mChannelName + " requests subscribers list");

						ArrayList<String> associatedhashTags = (ArrayList<String>) objectIS.readObject();
						brokerInfo.addHashTag(brokerID, mChannelName, associatedhashTags);
						notifyBrokersAboutChanges();

						associatedhashTags.add(mChannelName);
						System.out.println(">Server: AssociatedhashTags received!");
						for (String str : associatedhashTags) {
							System.out.println(str);
						}

						Message nmessage = new Message('B', null, "RSL");

						objectOS.writeObject(nmessage);
						objectOS.flush();

						HashMap<String, ArrayList<String>> subscribers = brokerInfo
								.getInfoAboutSubscribers(associatedhashTags);

						objectOS.writeObject(subscribers);
						objectOS.flush();
						System.out.println(">Server: Requested subscribers list sent!");
					}

					// NV -> new video
					if (message.getRequestType().equals("NV")) {

						String mChannelName = message.getChannelName();
						System.out.println(">Server: New Video detected from Publisher: " + mChannelName);

						ArrayList<String> associatedhashTags = (ArrayList<String>) objectIS.readObject();

						associatedhashTags.add(mChannelName);
						System.out.println(">Server: AssociatedhashTags received!");
						for (String str : associatedhashTags) {
							System.out.println(str);
						}

						ArrayList<VideoFileChunk> vfcl = new ArrayList<>();
						char mC = '1';
						while (mC == '1') {
							VideoFileChunk vfc = (VideoFileChunk) objectIS.readObject();
							vfcl.add(vfc);
							mC = vfc.getMC();
							System.out.println(">Server" + vfc.getorder());
						}

						ArrayList<String> channelNamesOfBroker = brokerInfo.getInfoAboutSubscribers(associatedhashTags)
								.get(brokerID);

						for (String consumerChannelName : channelNamesOfBroker) {

							Thread videoBroadcasting = new VideoBroadcasting(
									ActionsForServer.consumerHashList.get(getConsumer(consumerChannelName)), vfcl);
							videoBroadcasting.start();
						}
					}

					// SRVFC -> send requested video file chunks
					if (message.getRequestType().equals("SRVFC")) {

						String consumer = this.objectIS.readUTF();

						ActionsForServer actionForServer = ActionsForServer.tempHashList.get(consumer);
						int videosSent = 0;

						int rint = this.objectIS.readInt();
						int numberOfVids = rint;
						System.out.println(">Server: Number of videos is " + rint);

						ArrayList<VideoFileChunk> temp = new ArrayList<>();
						if (numberOfVids > 0) {

							while (videosSent < numberOfVids) {
								char mC = '1';
								while (mC == '1') {
									VideoFileChunk vfc = (VideoFileChunk) objectIS.readObject();
									temp.add(vfc);
									mC = vfc.getMC();
									System.out.println(
											">Server: Video file chunk received with orderID " + vfc.getorder());
								}
								videosSent++;
							}
						}

						actionForServer.objectOS.writeInt(numberOfVids);
						actionForServer.objectOS.flush();

						for (VideoFileChunk vd : temp) {
							actionForServer.objectOS.writeObject(vd);
							actionForServer.objectOS.flush();
							System.out.println(">Server: Message from broker sent to consumer with name: "
									+ vd.getvideoName() + " and orderID: " + vd.getorder());
						}
						actionForServer.disconnect();
						ActionsForServer.tempHashList.remove(consumer);
					}

				} else if (message.getFrom() == 'C') { // Messages from Consumers
					if (message.getRequestType().equals("NC")) {
						System.out.println(">Server: New Consumer recognized");
						Consumer newConsumer = new Consumer(message.getChannelName());
						ActionsForServer.consumerHashList.put(newConsumer, this);
						brokerInfo.addConsumer(brokerID, newConsumer);
						notifyBrokersAboutChanges();
					}

					// ST -> set topic
					if (message.getRequestType().equals("ST")) {
						System.out.println(">Server: Setting topic to consumer: " + message.getChannelName());
						String topicOfConsumer = objectIS.readUTF();
						brokerInfo.addInterests(brokerID, message.getChannelName(), topicOfConsumer);
						notifyBrokersAboutChanges();
					}

					// RPL -> request publisher list
					if (message.getRequestType().equals("RPL")) {

						String topic = objectIS.readUTF();

						Message nmessage = new Message('B', null, "RPL");
						objectOS.writeObject(nmessage);
						objectOS.flush();
						System.out
								.println(">Server: Consumer " + message.getChannelName() + " requests Publisher List!");

						HashMap<String, ArrayList<String>> info = brokerInfo.getInfoAboutTopic(topic);

						objectOS.writeObject(info);
						objectOS.flush();
					}

					// RV -> request video
					if (message.getRequestType().equals("RV")) {
						String consumerChannelName = message.getChannelName();
						ArrayList<String> publisherList = (ArrayList<String>) this.objectIS.readObject();
						String topic = this.objectIS.readUTF();

						ActionsForServer.tempHashList.put(consumerChannelName, this);

						ArrayList<VideoFileChunk> temp = new ArrayList<>();
						
						boolean flag = true;

						for (String pub : publisherList) {					
							// Filtering based on the channelNames
							if (!pub.equals(consumerChannelName)) {
								ActionsForServer actionsForServer = publisherHashList.get(getPublisher(pub));
								actionsForServer.objectOS.writeObject(new Message('B', null, "RT"));
								actionsForServer.objectOS.flush();
								System.out.println(">Server: Message sent to Publisher " + pub);

								actionsForServer.objectOS.writeUTF(topic);
								actionsForServer.objectOS.flush();
								System.out.println(">Server: Topic sent to Publisher " + pub);

								actionsForServer.objectOS.writeUTF(message.getChannelName());
								actionsForServer.objectOS.flush();
								flag = false;
							}
						}
						if (flag) {
							objectOS.writeInt(0);
							objectOS.flush();
						}
						// TODO
						// else notify consumer that publisher has no videos.
					}

					// GBI -> get broker info
					if (message.getRequestType().equals("GBI")) {
						objectOS.writeObject(ActionsForServer.brokersInfo);
						objectOS.flush();
						System.out.println(">Server: Sending brokersInfo");

						hash.hashByteintoInt(brokersInfo);
						String chosenBroker = hash.chooseBroker(hash.hashFunction(message.getChannelName()));

						if (!chosenBroker.equals(brokerID)) {
							objectIS.close();
							objectOS.close();
							socket.close();
						}
					}
				} else { // Messages form Brokers
					// UB -> update Brokers
					if (message.getRequestType().equals("UB")) {
						HashMap<String, ArrayList<Publisher>> brokerTopics = (HashMap<String, ArrayList<Publisher>>) objectIS
								.readObject();
						brokerInfo.updateBrokerTopics(brokerTopics);
						System.out.println(">Server: Received broker topics from other broker");

						HashMap<String, ArrayList<Consumer>> subscribers = (HashMap<String, ArrayList<Consumer>>) objectIS
								.readObject();
						brokerInfo.updateSubscribers(subscribers);
					}
				}

			} catch (IOException e) {

			} catch (ClassNotFoundException e) {

			}
		}
	}

	// Each Broker connects temporarily with every other Broker and informs them
	// about new Consumer or Publisher connections. Also about new topic
	// subscriptions and hashtag additions.
	public static void notifyBrokersAboutChanges() {
		Socket nSocket = null;
		ObjectOutputStream nobjOS = null;
		ObjectInputStream nobjIS = null;

		for (String ipPort : brokersServerPartInfo) {
			if (!ipPort.equals(brokerID)) {
				try {
					nSocket = new Socket(InetAddress.getByName(ipPort.split(":")[0]),
							Integer.parseInt(ipPort.split(":")[1]));
					nobjOS = new ObjectOutputStream(nSocket.getOutputStream());
					nobjIS = new ObjectInputStream(nSocket.getInputStream());

					Message message = new Message('B', null, "UB"); // update brokers = UB
					nobjOS.writeObject(message);
					nobjOS.flush();

					nobjOS.writeObject(brokerInfo.getBrokerTopics());
					nobjOS.flush();

					nobjOS.writeObject(brokerInfo.getSubscribers());
					nobjOS.flush();

					nobjOS.close();
					nobjIS.close();
					nSocket.close();

				} catch (NumberFormatException e) {
					e.printStackTrace();
				} catch (UnknownHostException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static void setBrokersInfo(ArrayList<String> info) {
		ActionsForServer.brokersInfo = info;
	}
	
	public static void setBrokersServerPartInfo(ArrayList<String> info) {
		ActionsForServer.brokersServerPartInfo = info;
	}

	public static void setIpaddress(String brokerID) {
		ActionsForServer.brokerID = brokerID;
	}

	public static void setHashFunction() {
		ActionsForServer.hash.hashByteintoInt(brokersInfo);
	}

	public void disconnect() {

		try {
			this.socket.close();
			this.objectIS.close();
			this.objectOS.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
