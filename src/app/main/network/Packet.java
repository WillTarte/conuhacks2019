package app.main.network;

import java.nio.ByteBuffer;

public class Packet {
	
	public static final int INVALID = 0;
	public static final int PING = 1;
	public static final int ERROR = 2;
	public static final int CONNECT = 3;
	public static final int LOGIN = 4;
	public static final int DISCONNECT = 5;
	public static final int PORT_REDIRECT = 6;
	public static final int ACCEPT_CONNECTION = 7;
	public static final int GAME_UPDATE = 8;
	
	public static final int BUFFER = 525;
	
	private static final int HEADER_LENGTH = 16;
	
	private int type;
	private long timestamp;
	private int length;
	
	private byte[] content;
	
	public Packet(int type, long timestamp, byte[] content) {
		this.type = type;
		this.timestamp = timestamp;
		this.length = content != null ? content.length : 0;
		this.content = content;
	}
	
	public Packet(int type, byte[] content) {
		this.type = type;
		this.timestamp = System.currentTimeMillis();
		this.length = content != null ? content.length : 0;
		this.content = content;
	}
	
	public int getType() {
		return type;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public int getContentLength() {
		return length;
	}
	
	public byte[] getContent() {
		return content;
	}
	
	public byte[] serialize() {
		
		byte[] packet = new byte[length + HEADER_LENGTH];
		
		byte[] header = (byte[]) ByteBuffer.allocate(HEADER_LENGTH)
				.putInt(type)
				.putLong(timestamp)
				.putInt(length)
				.array();
		
		Util.copy(header, packet, 0);
		Util.copy(content, packet, HEADER_LENGTH);
		
		return packet;
		
	}

	public static Packet fromData(byte[] data) {
		ByteBuffer buffer = ByteBuffer.wrap(data);
		buffer.rewind();
		
		int type = buffer.getInt();
		long timestamp = buffer.getLong();
		int length = buffer.getInt();
		byte[] content = new byte[length];
		
		
		for(int i = 0; i < content.length; i++)
			content[i] = buffer.get();
		
		return new Packet(type, timestamp, content);
	}
	
	
}
