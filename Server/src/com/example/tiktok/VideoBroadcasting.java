package com.example.tiktok;

import java.util.ArrayList;
import java.io.*;

public class VideoBroadcasting extends Thread {

	private ActionsForServer actionsForServer;
	private ArrayList<VideoFileChunk> vfcl;

	VideoBroadcasting(ActionsForServer actionsForServer, ArrayList<VideoFileChunk> vfcl) {

		this.actionsForServer = actionsForServer;
		this.vfcl = vfcl;

	}

	// Thread implementation part.
	// The Broker can send videos to multiple Consumers in parallel.
	public void run() {
		try {
			Message nmessage = new Message('B', null, "SNV");
			System.out.println("Message is sending to consumer");
			this.actionsForServer.objectOS.writeObject(nmessage);
			this.actionsForServer.objectOS.flush();
			for (VideoFileChunk vd : this.vfcl) {
				this.actionsForServer.objectOS.writeObject(vd);
				this.actionsForServer.objectOS.flush();
				System.out.println(">Server: Message from broker sent to consumer with orderID " + vd.getorder());
			}
		} catch (IOException io) {
			System.err.println("Error in VideoBroadcasting");
		}
	}
}