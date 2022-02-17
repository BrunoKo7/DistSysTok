package com.example.tiktok;

import java.io.IOException;

public class AppNode {

    private Consumer consumer;
    private Publisher publisher;
    private ChannelName channelName;
    private static AppNode appNode = null;

    private AppNode(String name){
        this.channelName = new ChannelName(name);
        this.consumer = new Consumer(this.channelName.getChannelName());
        this.publisher = new Publisher(this.channelName);
    }

    public static AppNode getAppNode(String name){
        if (appNode == null){
            appNode = new AppNode(name);
        }
        return appNode;
    }

    public static AppNode getAppNode(){
        return appNode;
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
