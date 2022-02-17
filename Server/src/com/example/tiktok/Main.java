package com.example.tiktok;

import java.util.Scanner;

public class Main {

	public static void main(String[] args) {

		Scanner in = new Scanner(System.in);
		System.out.println("Insert Machine's IP address: ");
		String ipAddr = in.nextLine();
		System.out.println("Insert Broker port number: ");
		int portNo = in.nextInt();
		

		try {

			Broker broker = new Broker(ipAddr, portNo);
			broker.openServer();
		} catch (ClassNotFoundException e) {
			System.err.println("ServerSide - Error in Main");
		}
	}
}