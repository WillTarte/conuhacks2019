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
					String msg = "e|id=" + em.getPlayer().getId() + 
							",x=" + em.getPlayer().getPos().getX() + 
							",y=" + em.getPlayer().getPos().getY() + 
							",ang=" + em.getPlayer().getRotation() + ";";
					sendPacket(Packet.GAME_UPDATE, msg.getBytes(), true);
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
	
	private void processGameUpdate(PacketInfo packetInfo) {
		
		if(packetInfo.packet.getType() != Packet.GAME_UPDATE) 
			return;
		
		String msg = new String(packetInfo.packet.getContent());
		String[] cmds = msg.split(";");
		
		for(String command :cmds)
			if(command.startsWith("e|")) {
				String[] values = command.split("|")[1].split(",");
				String id = "";
				double x = 0, y = 0;
				double angle = 0;
				
				for(String value : values) {
					switch(value.split("=")[0]) {
					case "id" :
						id = value.split("=")[1];
						break;
					case "x" :
						x = Double.parseDouble(value.split("=")[1]);
						break;
					case "y" :
						y = Double.parseDouble(value.split("=")[1]);
						break;
					case "ang" : 
						angle = Double.parseDouble(value.split("=")[1]);
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
