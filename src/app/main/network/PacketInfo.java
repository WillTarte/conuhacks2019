package app.main.network;

import java.net.InetAddress;

public class PacketInfo{
		
	public InetAddress source;
	public int port;
	public Packet packet;
	
	public PacketInfo(InetAddress source, int port, Packet packet) {
		this.source = source;
		this.port = port;
		this.packet = packet;
	}
		
}
