package app.main.network;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.security.KeyPair;
import java.security.PublicKey;

import app.main.entities.Car;
import app.main.entities.EntityManager;
import app.main.utils.Maths;
import app.main.utils.Vector;

public class Client {
	
	public static final int TIMEOUT = 5000;
	
	public InetAddress host;
	public int port;
	
	private PublicKey serverKey;
	
	private DatagramSocket socket;
	public KeyPair keys;
	private Receiver receiver;
	
	private String username;
	
	public long lastPacket;
	public int ping = 0;
	
	public volatile boolean connected = false;
	public volatile boolean loggedIn = false;
	
	private EntityManager em;
	
	public Client(InetAddress host, int port, String username, EntityManager em) throws Exception {
		this.host = host;
		this.port = port;
		this.username = username;
		this.keys = RSA.generateKeyPair(Server.KEY_SIZE);
		this.receiver = new Receiver(this.keys.getPrivate()).withCallback(this::processPacket);
		this.socket = receiver.getSocket();
		this.em = em;
		this.receiver.start();
		
	}
	
	public boolean connected() {
		return connected && loggedIn;
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
				
			case Packet.GAME_UPDATE:
				processGameUpdate(packetInfo);
				break;
		}
		
	}
	
	public boolean sendPacket(int type, byte[] data, boolean encrypted) {
		
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
	
	private void processGameUpdate(PacketInfo packetInfo) {
		
		if(packetInfo.packet.getType() != Packet.GAME_UPDATE) 
			return;
		
		String msg = new String(packetInfo.packet.getContent());
		String[] cmds = msg.split(";");
		
		for(String command : cmds) {
			
			if(command.startsWith("e|")) {
				
				String[] values = command.substring(2).split(",");
				String id = "";
				double x = 0, y = 0;
				double angle = 0;
				
				
				for(String value : values) {
					switch(value.split("\\=")[0]) {
					case "id" :
						id = value.split("\\=")[1];
						break;
					case "x" :
						x = Double.parseDouble(value.split("\\=")[1]);
						break;
					case "y" :
						y = Double.parseDouble(value.split("\\=")[1]);
						break;
					case "ang" : 
						angle = Double.parseDouble(value.split("\\=")[1]);
						break;
					}
				}
				
				
				if(em.getEntityMap().get(id) != null) {
					Car car = (Car) em.getEntityMap().get(id);
					car.setPos(new Vector(x, y));
					car.setRotation((float)angle);
 				}else{
 					em.register(id, new Car(100, 100, x, y, id, Maths.generateFromAngle((float)angle, 30, 60)));
 				}
				
			}
		}
	}
	
	public void connect() {
		
		// Send unencrypted connect packet to server with our public key.
		sendPacket(Packet.CONNECT, keys.getPublic().getEncoded(), false);
		
	}
	
	public void login() {
		
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
