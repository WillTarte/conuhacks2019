package app.main.network;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.security.KeyPair;
import java.security.PublicKey;

public class Client extends Thread{
	
	private static final int TICKS_PER_SECOND = 15;
	private static final int TIMEOUT = 5000;
	
	private InetAddress host;
	private int port;
	
	private PublicKey serverKey;
	
	private DatagramSocket socket;
	public KeyPair keys;
	private Receiver receiver;
	
	private String username;
	
	private long lastPacket;
	private int ping = 0;
	
	private volatile boolean running = false;
	
	private volatile boolean connected = false;
	private volatile boolean loggedIn = false;
	
	
	public Client(InetAddress host, int port, String username) throws Exception {
		this.host = host;
		this.port = port;
		this.username = username;
		this.keys = RSA.generateKeyPair(Server.KEY_SIZE);
		this.receiver = new Receiver(this.keys.getPrivate()).withCallback(this::processPacket);
		this.socket = receiver.getSocket();
		this.receiver.start();
		
	}
	
	@Override
	public void start() {
		if(running) return;
		this.running = true;
		super.start();
	}
	
	public void stopServer() {
		if(!running) 
			return;
		running = false;
		try {
			join();
		} catch (InterruptedException e) {}
	}
	
	public void run() {
		
		long lastTime = System.nanoTime();
		long lastTick = System.nanoTime();
		while(running) {
			
			long currentTick = System.nanoTime();
			if(currentTick - lastTick >= 1000000000 / TICKS_PER_SECOND) {
		
				if(connected && loggedIn) {
					sendPacket(Packet.INVALID, null, false);
					if(System.currentTimeMillis() - lastPacket >= TIMEOUT) {
						connected = false;
						loggedIn = false;
						System.out.println("Connection to " + host.toString() + " timed out.");
					}
				}
				
				
				lastTick = currentTick;
			}
			
			long currentTime = System.nanoTime();
			if(currentTime - lastTime >= 1000000000) {

				if(!connected)
					connect();
				else if(!loggedIn)
					login();
				else {
					sendPacket(Packet.PING, null, false);
					
					System.out.println("Connected to " + host + ":" + port + " (" + ping + " ms)");
				}
				
				lastTime = currentTime;
			}
			
		}
		
	}

	public void processPacket(PacketInfo packetInfo) {
		if(packetInfo == null) 
			return;
		switch(packetInfo.packet.getType()) {
		
			case Packet.ACCEPT_CONNECTION:
				processConnection(packetInfo);
				break;
				
			case Packet.INVALID:
				processInvalid(packetInfo);
				break;
				
			case Packet.PING:
				processPing(packetInfo);
				break;
				
			case Packet.PORT_REDIRECT:
				processRedirect(packetInfo);
				break;
				
		}
		
	}
	
	private boolean sendPacket(int type, byte[] data, boolean encrypted) {
		
		if(serverKey == null)
			encrypted = false;
		
		byte[] toSend = encrypted 	? RSA.encrypt(serverKey, new Packet(type, data).serialize()) 
									: new Packet(type, data).serialize();
		
		DatagramPacket dp = new DatagramPacket(toSend, toSend.length, host, port);
		
		try {
			socket.send(dp);
			return true;
		} catch (IOException e) {
			return false;
		}
		
	}
	
	private void processConnection(PacketInfo packetInfo) {
		
		if(packetInfo.packet.getType() != Packet.ACCEPT_CONNECTION)
			return;
		
		serverKey = RSA.toPublicKey(packetInfo.packet.getContent());
		
		if(serverKey != null)
			connected = true;
		else {
			connected = false;
			loggedIn = false;
		}
		
		resetTimer();
		
	}
	
	private void processInvalid(PacketInfo packetInfo) {
		
		if(packetInfo.packet.getType() != Packet.INVALID)
			return;
		
		if(!loggedIn)
			loggedIn = true;
		
		resetTimer();
		
	}
	
	private void processPing(PacketInfo packetInfo) {
		
		if(packetInfo.packet.getContentLength() < 9)
			return;
	
		updateLatency(packetInfo);
		
		ByteBuffer content = ByteBuffer.wrap(packetInfo.packet.getContent());
		content.rewind();
		
		byte counter = content.get(8);
		if(counter <= 2)
			sendPacket(Packet.PING, ByteBuffer.allocate(Long.BYTES).putLong(packetInfo.packet.getTimestamp()).array(), false);
		
		resetTimer();
		
		
	}
	
	private void processRedirect(PacketInfo packetInfo) {
		if(packetInfo.packet.getContentLength() < 4)
			return;
	
		ByteBuffer content = ByteBuffer.wrap(packetInfo.packet.getContent());
		content.rewind();
		
		int newPort = content.getInt();
		
		this.port = newPort;

	}
	
	private void connect() {
		
		// Send unencrypted connect packet to server with our public key.
		sendPacket(Packet.CONNECT, keys.getPublic().getEncoded(), false);
		
	}
	
	private void login() {
		
		// Send encrypted login packet to server with our username.
		sendPacket(Packet.LOGIN, username.getBytes(), true);
		
	}
	
	private void updateLatency(PacketInfo packetInfo) {
		if(packetInfo.packet.getType() != Packet.PING)
			return;
		try {
			ping = (int)(System.currentTimeMillis() - 
					((ByteBuffer)ByteBuffer.wrap(packetInfo.packet.getContent()).rewind()).getLong()) / 2;
		}catch(Exception e) {}
		
	}
	
	private void resetTimer() {
		lastPacket = System.currentTimeMillis();
	}
	
}
