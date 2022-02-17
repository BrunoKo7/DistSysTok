package com.example.tiktok;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class Broker{

    private ArrayList<Consumer> consumerList;
	private ArrayList<Publisher> publisherList;
	private String ipAddress;
	private int port;
	private ArrayList<String> brokersInfo = new ArrayList<>();
	private ArrayList<String> brokersServerPartInfo = new ArrayList<>();
	private HashFunctionMD5 hash = new HashFunctionMD5();
    
    public Broker(int port){
		this.port = port;
        this.consumerList = new ArrayList<>();
        this.publisherList = new ArrayList<>();
        //this.brokersInfo.add("10.0.2.2:4321");
        //this.brokersInfo.add("10.0.2.2:4322");
        //this.brokersInfo.add("10.0.2.2:4323");
        //this.ipAddress = "10.0.2.2:".concat("" + port);
        this.brokersInfo.add("192.168.1.9:4321");
        this.brokersInfo.add("192.168.1.9:4322");
        this.brokersInfo.add("192.168.1.9:4323");
        this.ipAddress = "192.168.1.9:".concat("" + port);
        
        this.brokersServerPartInfo.add("127.0.0.1:4321");
        this.brokersServerPartInfo.add("127.0.0.1:4322");
        this.brokersServerPartInfo.add("127.0.0.1:4323");
        
        ActionsForServer.setBrokersInfo(brokersInfo);
        ActionsForServer.setBrokersServerPartInfo(brokersServerPartInfo);
        
        ActionsForServer.setIpaddress(ipAddress);
    }
    
    public Broker(String ip, int port ){
		this.port = port;
        this.consumerList = new ArrayList<>();
        this.publisherList = new ArrayList<>();

        this.ipAddress = "192.168.1.9:".concat("" + port);
        this.brokersInfo.add(ip.concat(":" + 4321));
        this.brokersInfo.add(ip.concat(":" + 4322));
        this.brokersInfo.add(ip.concat(":" + 4323));
        
        this.brokersServerPartInfo.add("127.0.0.1:4321");
        this.brokersServerPartInfo.add("127.0.0.1:4322");
        this.brokersServerPartInfo.add("127.0.0.1:4323");
        
        ActionsForServer.setBrokersInfo(brokersInfo);
        ActionsForServer.setBrokersServerPartInfo(brokersServerPartInfo);
        
        ActionsForServer.setIpaddress(ipAddress);
    }

    public void openServer() throws ClassNotFoundException{
        ServerSocket serverSocket = null;
        Socket connectionSocket = null;
        
        try {
			serverSocket = new ServerSocket(port);
			System.out.println("Server: started on port " + port);
			while (true) {
				connectionSocket = serverSocket.accept();
				System.out.println("Server: New connection detected");
				Thread actionForServer = new ActionsForServer(connectionSocket);
				actionForServer.start();
			}
		} catch (IOException ioException) {
			ioException.printStackTrace();
		} finally {
			try {
				serverSocket.close();
			} catch (IOException ioException) {
				ioException.printStackTrace();
			}
		}
	}

    public void calculateKeys() {
        // TODO Auto-generated method stub
        
    }

    public Publisher acceptConnection(Publisher publisher) {
        // TODO Auto-generated method stub
        return null;
    }

    public Consumer acceptConnection(Consumer consumer) {
        // TODO Auto-generated method stub
        return null;
    }

    public void notifyPublisher(String message) {
        // TODO Auto-generated method stub
        
    }

    public void notifyBrokersOnChanges() {
        // TODO Auto-generated method stub
        
    }

    public void pull(String key) {
        // TODO Auto-generated method stub
        
    }

    public void filterConsumers(String criterion) {
        // TODO Auto-generated method stub
        
    }
}