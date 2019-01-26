package app.main.network;

import java.net.DatagramSocket;
import java.net.SocketException;
import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.function.BiConsumer;

import app.main.network.Server.SClient;

public class WorkerThread extends Receiver{
	
	public volatile ArrayList<SClient> clients = new ArrayList<SClient>();
	
	private BiConsumer<PacketInfo, WorkerThread> callbackPointer;
	
	public DatagramSocket socket;
	
	public WorkerThread(int port, PrivateKey key, BiConsumer<PacketInfo, WorkerThread> callbackPointer) {
		super(port, key);
		withCallback(this::callback);
		start();
		this.callbackPointer = callbackPointer;
		try {this.socket = new DatagramSocket();} catch (SocketException e) {}
	}
	
	public void serve(SClient client) {
		clients.add(client);
	}
	
	public void disconnect(SClient client) {
		clients.remove(client);
	}
	
	public void callback(PacketInfo packet) {
		if(callbackPointer != null)
			callbackPointer.accept(packet, this);
	}
	
}
