package app.main.network;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.security.PrivateKey;
import java.util.Arrays;
import java.util.function.Consumer;

public class Receiver extends Thread{
	
	private static final int BUFFER_LENGTH = 1024;
	private static final int TIMEOUT = 0;
	
	private int port;
	private PrivateKey key;
	
	private DatagramSocket socket;
	private Consumer<PacketInfo> callback;
	
	private volatile boolean running = false;
	
	public Receiver(PrivateKey key) {
		this.key = key;
		try {this.socket = new DatagramSocket();
			 this.port = socket.getLocalPort();
			 this.socket.setSoTimeout(TIMEOUT);} 
		catch (SocketException e) {this.socket = null;e.printStackTrace();}
	}
	
	public Receiver(int port, PrivateKey key) {
		this.port = port;
		this.key = key;
		try {this.socket = new DatagramSocket(this.port);
			 this.socket.setSoTimeout(TIMEOUT);} 
		catch (SocketException e) {this.socket = null;}
	}
	
	public Receiver withCallback(Consumer<PacketInfo> callback) {
		this.callback = callback;
		return this;
	}
	
	@Override
	public void start() {
		if(running) 
			return;
		this.running = true;
		
		super.start();
	}
	
	public void halt() {
		this.running = false;
	}
	
	public void run() {
		
		while(running){
			
			try {
				
				// Receive the next packet.
				DatagramPacket dp = new DatagramPacket(new byte[BUFFER_LENGTH], BUFFER_LENGTH);
				socket.receive(dp);
				
				// Extract the packet data.
				byte[] data = extractData(dp);
				
				// Packet object.
				Packet packet = null;
				
				try {
					
					// Decrypt the packet.
					byte[] decrypted = RSA.decrypt(key, data);
					packet = Packet.fromData(decrypted);
				
				}catch(Exception e) {
					
					// Simply read the packet if it isn't encrypted.
					packet = Packet.fromData(data);
					
				}
				
				// Add the packet to the queue.
				if(packet != null && callback != null);
					callback.accept(new PacketInfo(dp.getAddress(), dp.getPort(), packet));
					//queue.append(dp.getAddress(), dp.getPort(), packet);
				
			} catch (Exception e) {
				
				// For debug purposes.
				//e.printStackTrace();
				
			}
			
		}
		
	}
	
	private byte[] extractData(DatagramPacket packet) {
		return Arrays.copyOf(packet.getData(), packet.getLength());
	}
	
	public DatagramSocket getSocket() {
		return socket;
	}
	
	public int getPort() {
		return port;
	}
}
