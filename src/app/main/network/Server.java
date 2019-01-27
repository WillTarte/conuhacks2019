package app.main.network;

import java.awt.Color;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Collections;

import app.main.entities.Car;
import app.main.entities.Entity;
import app.main.entities.EntityManager;
import app.main.utils.Maths;
import app.main.utils.Vector;

public class Server extends Thread{
	
	public static final int KEY_SIZE = 4200;
	private static final int MAX_CLIENTS = 20;
	
	private static final int TICKS_PER_SECOND = 20;
	
	private static final int TIMEOUT = 5000;
	
	private EntityManager em = new EntityManager();
	
	/**
	 * Client struct for the server to keep track of.
	 */
	static class SClient{
		protected String username = null;
		protected InetAddress ip;
		protected int port, ping = 0;
		protected long lastPacket;
		protected PublicKey key;
		
		protected SClient(InetAddress ip, int port, PublicKey key, long lastPacket) {
			this.ip = ip;
			this.port = port;
			this.key = key;
			this.lastPacket = lastPacket;
		}
		
		public void updateLatency(Packet p) {
			if(p.getType() != Packet.PING)
				return;
			try {
				this.ping = (int)(System.currentTimeMillis() - ((ByteBuffer)ByteBuffer.wrap(p.getContent()).rewind()).getLong()) / 2;
			}catch(Exception e) {}
		}
		
		public void resetTimer() {
			this.lastPacket = System.currentTimeMillis();
		}
		
		public String toString() {
			return "[" + ip.toString().substring(1) + ":" + port + "] " + username + " (" + ping + "ms)";
		}
	}
	
	
	public Server() throws SocketException{
		this.keys = RSA.generateKeyPair(KEY_SIZE);
	}
	
	public Server withThreads(int numThreads) {
		if(numThreads < 1)
			throw new RuntimeException("Negative number of threads specified.");
		this.workers = new WorkerThread[numThreads];
		return this;
	}
	
	public Server withPortRange(PortRange range) {
		this.range = range;
		return this;
	}
	
	public Server init() {
		int begin = 0;
		if(range != null)
			begin = range.getFrom();
		
		
		for(int i = 0; i < workers.length; i++) {
			System.out.println(begin);
			workers[i] = new WorkerThread(begin == 0 ? begin : begin++, this.keys.getPrivate(), this::processPacket);
			
			workers[i].start();
		}
		return this;
	}
	
	public KeyPair keys;
	
	private PortRange range;
	private WorkerThread[] workers;
	private volatile SClient[] clients = new SClient[MAX_CLIENTS];
	private int numClientsConnected = 0;
	
	private volatile boolean running = false;
	
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
		
		long lastTick = System.nanoTime();
		long lastTime = System.nanoTime();
		while(running) {
				
			long currentTime = System.nanoTime();
			if(currentTime - lastTime >= 1000000000) {
				
				System.out.println(currentClients());
				
				lastTime = currentTime;
			}
			
			long currentTick = System.nanoTime();
			if(currentTick - lastTick >= 1000000000 / TICKS_PER_SECOND) {
				
				tickClients();
				
				lastTick = currentTick;
			}
			
		}
		
	}
	
	private WorkerThread getLeastChargedThread() {
		ArrayList<Integer> sizes = new ArrayList<Integer>();
		for(int i = 0; i < workers.length; i++)
			sizes.add(workers[i].clients.size());
		
		return workers[sizes.indexOf(Collections.min(sizes))];
	}
	
	public String currentClients() {
		String msg = "[" + Thread.activeCount() + " Threads]\nCurrently connected clients :\n ";
		for(WorkerThread t : workers)
			msg += t.clients.size() + " ; ";
		msg +="\n";
		for(int i = 0; i < numClientsConnected; i++)
			if(clients[i].username != null)
				msg += "\t" + clients[i] + "\n";
		
		return msg + "\n";
	}
	
	public void tickClients() {
		for(int i = 0; i < numClientsConnected; i++)
			if(System.currentTimeMillis() - clients[i].lastPacket >= TIMEOUT) {
				System.out.println(clients[i] + " timed out.");
				disconnect(i, "Connection timed out.", workers[0].socket);
			}
	}
	
	public void processPacket(PacketInfo packetInfo, WorkerThread worker) {
		//System.out.println(packetInfo.packet.getType());
		switch(packetInfo.packet.getType()) {
			case Packet.CONNECT:
				WorkerThread takesOver = getLeastChargedThread();
				if(takesOver != worker)
					sendPacket(packetInfo, Packet.PORT_REDIRECT, ByteBuffer.allocate(4).putInt(takesOver.getPort()).array(), worker.socket);
				else
					processConnectPacket(packetInfo, worker);
				break;
			case Packet.LOGIN:
				processLoginPacket(packetInfo, worker.socket);
				break;
			case Packet.PING:
				processPingPacket(packetInfo, worker.socket);
				break;
			case Packet.INVALID:
				processInvalidPacket(packetInfo, worker.socket);
				break;
			case Packet.GAME_UPDATE:
				processGameUpdate(packetInfo, worker.socket);
				break;
			default:
				SClient client = getClient(packetInfo.source, packetInfo.port);
				if(client != null)
					sendPacket(client, Packet.ERROR, "Invalid packet.".getBytes(), workers[0].socket);
				else
					sendPacket(packetInfo, Packet.ERROR, "Invalid packet.".getBytes(), workers[0].socket);
		}
	}
	
	private void disconnect(int clientIndex, String reason, DatagramSocket socket) {
		
		sendPacket(clients[clientIndex], Packet.DISCONNECT, reason.getBytes(), socket);
		
		for(int i = 0; i < workers.length; i++)
			workers[i].disconnect(clients[clientIndex]);
		
		clients[clientIndex] = null;
		numClientsConnected--;
		
		for(int i = clientIndex; i < MAX_CLIENTS - 1; i++)
			clients[i] = clients[i + 1];
		
		clients[MAX_CLIENTS - 1] = null;
			
	}
	
	private void processConnectPacket(PacketInfo packetInfo, WorkerThread worker) {
		
		System.out.println("Processing connect packet.");
		
		if(packetInfo.packet.getType() != Packet.CONNECT)
			return;
		
		if(numClientsConnected >= MAX_CLIENTS) {
			// Send unencrypted error packet.
			sendPacket(packetInfo, Packet.ERROR, "Server is full.".getBytes(), worker.socket);
			return;
		}
		
		PublicKey key = RSA.toPublicKey(packetInfo.packet.getContent());
		if(key != null) {
			
			// Check if client is already connected.
			if(getClient(packetInfo.source, packetInfo.port) != null) {
				sendPacket(packetInfo, Packet.ERROR, "Already connected.".getBytes(), worker.socket);
				return;
			}
			
			// Register the new client.
			SClient client = new SClient(packetInfo.source, packetInfo.port, key, System.currentTimeMillis());
			clients[numClientsConnected++] = client;
			worker.serve(client);
			
			// Reset timeout timer.
			client.resetTimer();
			
			// Send unencrypted packet containing our server's public key.
			sendPacket(packetInfo, Packet.ACCEPT_CONNECTION, keys.getPublic().getEncoded(), worker.socket);
			
			System.out.println(client.ip.toString() + " just connected. (" + client.ping + " ms)");
			
		}else {
			// Send unencrypted error packet.
			sendPacket(packetInfo, Packet.ERROR, "Invalid RSA key.".getBytes(), worker.socket);
			return;
		}
		
	}
	
	private void processLoginPacket(PacketInfo packetInfo, DatagramSocket socket) {
		
		if(packetInfo.packet.getType() != Packet.LOGIN)
			return;
		
		SClient client = getClient(packetInfo.source, packetInfo.port);
		if(client == null) {
			sendPacket(packetInfo, Packet.ERROR, "You must connect before logging in.".getBytes(), socket);
			return;
		}
		
		if(client.username != null) {
			sendPacket(packetInfo, Packet.ERROR, "Already logged in.".getBytes(), socket);
			return;
		}	
		
		// Set the client's username.
		client.username = new String(packetInfo.packet.getContent());
		
		// Reset timeout timer.
		client.resetTimer();
		
		// Send invalid packet to confirm.
		sendPacket(client, Packet.INVALID, null, socket);
		
		System.out.println(client.username + " just logged in. (" + client.ping + " ms)");
		System.out.println(client);
	}
	
	private void processInvalidPacket(PacketInfo packetInfo, DatagramSocket socket) {
		
		if(packetInfo.packet.getType() != Packet.INVALID)
			return;
		
		SClient client = getClient(packetInfo.source, packetInfo.port);
		if(client == null) 
			return;
		
		// Reset timeout timer.
		client.resetTimer();
		
		// Send invalid packet to confirm.
		sendPacket(packetInfo, Packet.INVALID, null, socket);
		
	}
	
	private void processPingPacket(PacketInfo packetInfo, DatagramSocket socket) {
		
		if(packetInfo.packet.getType() != Packet.PING)
			return;
		
		SClient client = getClient(packetInfo.source, packetInfo.port);
		if(client == null) 
			return;
		
		byte counter = 2;
		if(packetInfo.packet.getContentLength() > 0)
			counter++;
		
		// Update client's ping.
		client.updateLatency(packetInfo.packet);
		
		// Send ping packet back to client.
		sendPacket(packetInfo, Packet.PING, ByteBuffer.allocate(Long.BYTES + Byte.BYTES)
				.putLong(packetInfo.packet.getTimestamp())
				.put(counter)
				.array(), socket);
		
		// Reset timeout timer.
		client.resetTimer();

	}
	
	private void processGameUpdate(PacketInfo packetInfo, DatagramSocket socket) {
		
		if(packetInfo.packet.getType() != Packet.GAME_UPDATE)
			return;
		
		SClient client = getClient(packetInfo.source, packetInfo.port);
		if(client == null) 
			return;
		
		// Reset timeout timer.
		client.resetTimer();
		
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
 					em.register(id, new Car(100, x, y, id, Maths.generateFromAngle((float)angle, 30, 60),new Color(0,0,255)));
 				}
				
			}
		}
		
		msg = "";
		for(String eID : em.getEntityMap().keySet()) {
			Entity player = em.getEntityMap().get(eID);
			msg += "e|id=" + eID + ",x=" + Float.toString((float)player.getPos().getX()) + 
					",y=" + Float.toString((float)player.getPos().getY()) + 
					",ang=" + Float.toString((float)((Car)player).getRotation()) + ";";
		}
		sendPacket(packetInfo, Packet.GAME_UPDATE, msg.getBytes(), socket);
	}
	
	public static boolean sendPacket(SClient recipient, int type, byte[] data, DatagramSocket socket) {
		byte[] encrypted = RSA.encrypt(recipient.key, new Packet(type, data).serialize());
		DatagramPacket dp = new DatagramPacket(encrypted, encrypted.length, recipient.ip, recipient.port);
		try {
			socket.send(dp);
			return true;
		} catch (IOException e) {
			return false;
		}
	}
	public static boolean sendPacket(PacketInfo recipient, int type, byte[] data, DatagramSocket socket) {
		byte[] packetData = new Packet(type, data).serialize();
		DatagramPacket dp = new DatagramPacket(packetData, packetData.length, recipient.source, recipient.port);
		try {
			socket.send(dp);
			return true;
		} catch (IOException e) {
			return false;
		}
	}

	private SClient getClient(InetAddress ip, int port) {
		for(SClient client : clients)
			if(client != null && client.ip.equals(ip) && client.port == port)
				return client;
		return null;
	}

}
