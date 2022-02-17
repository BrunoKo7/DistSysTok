package com.example.tiktok;

import android.os.AsyncTask;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.StrictMode;
import android.util.Log;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class Publisher implements Runnable {

    private ChannelName channelName;
    private Socket socket;
    private ObjectOutputStream objOS;
    private ObjectInputStream objIS;
    private ArrayList<String> brokersInfo;
    private HashFunctionMD5 hash = new HashFunctionMD5();
    private HashMap<String, ArrayList<String>> temp; // Subscriber list received from Broker.

    private String ipAddress;
    private int port;

    public Thread thread;

    public Publisher(ChannelName channelName) {
        this.channelName = channelName;

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

        StrictMode.setThreadPolicy(policy);
    }

    // Thread implementation part.
    // The Publisher can listen to multiple Brokers in parallel and receive the
    // Broker's messages.
    public void run() {
        Log.d("DEBUG:","Waiting for message");
        while (true) {

            try {
                Message message = (Message) this.objIS.readObject();
                if (message.getFrom() == 'B') {

                    if (message.getRequestType().equals("RT")) {
                        Log.d("DEBUG:",">Publisher gets request type RT");
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

    public ChannelName getChannelName(){
        return this.channelName;
    }

    public void connect(String ipAddress, int port) {
        try {
            this.socket = new Socket(InetAddress.getByName(ipAddress), port);

            this.objOS = new ObjectOutputStream(socket.getOutputStream());
            this.objIS = new ObjectInputStream(socket.getInputStream());

            Message message = new Message('P', this.channelName.getChannelName(), "GBI");

            this.objOS.writeObject(message);
            this.objOS.flush();

            brokersInfo = (ArrayList<String>) this.objIS.readObject();

            for (String str : brokersInfo) {
                Log.d("DEBUG:", str);
            }

            hash.hashByteintoInt(brokersInfo);

            String chosenBroker = hash.chooseBroker(hash.hashFunction(this.channelName.getChannelName()));
            Log.d("DEBUG:", chosenBroker);

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
            Log.d("DEBUG:", "Connected Publisher");

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

    public void start() {
        this.thread = new Thread(this);
        this.thread.start();
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

            Log.d("DEBUG:",">Publisher " + this.channelName.getChannelName() + " sends message: RSL");

            this.objOS.writeObject(message);
            this.objOS.flush();

            this.objOS.writeObject(videoFile.getAssociatedHashTags());
            this.objOS.flush();

            Thread.sleep(1000);

            HashMap<String, ArrayList<String>> subscribersInfo = this.getHashMap();
            Log.d("DEBUG:",">Publisher " + this.channelName.getChannelName() + "  receives subscribers list!");

            Set<String> brokerWithSubscribers = subscribersInfo.keySet();

            for (String brokerID : brokerWithSubscribers) {
                Log.d("DEBUG:","BrokerID " + brokerID);
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

    public HashMap<String, ArrayList<String>> getHashMap() {
        return this.temp;
    }
}
