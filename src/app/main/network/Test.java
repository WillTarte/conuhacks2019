package app.main.network;

public class Test {
	
	public static void main(String[] args) throws Exception {
		
		Server server = new Server()
				.withPortRange(PortRange.create().from(42353).to(42360))
				.withThreads(4)
				.init();
		
		long lastTick = System.nanoTime();
		long lastTime = System.nanoTime();
		while(true) {
				
			long currentTime = System.nanoTime();
			if(currentTime - lastTime >= 1000000000) {
				
				System.out.println(server.currentClients());
				
				lastTime = currentTime;
			}
			
			long currentTick = System.nanoTime();
			if(currentTick - lastTick >= 1000000000 / 60) {
				
				server.tickClients();
				
				lastTick = currentTick;
			}
			
		}

		/*
		@SuppressWarnings("resource")
		DatagramSocket socket = new DatagramSocket();
		
		
		// Connect.
		Packet packet = new Packet(Packet.CONNECT, server.keys.getPublic().getEncoded());
		
		byte[] data = packet.serialize();
		DatagramPacket dp = new DatagramPacket(data, data.length, InetAddress.getByName("localhost"), 34341);
		socket.send(dp);
		
		
		Thread.sleep(1000);
		
		// Login.
		packet = new Packet(Packet.LOGIN, "nigger".getBytes());
		
		data = RSA.encrypt(server.keys.getPublic(), packet.serialize());
		dp = new DatagramPacket(data, data.length, InetAddress.getByName("localhost"), 34341);
		socket.send(dp);
		/*
		long lastTime = System.nanoTime();
		boolean ape = true;
		Thread.sleep(1000);
		while(true) {
			long diff = System.nanoTime() - lastTime;
			if(diff > 1000000000) {
				
				Packet packet = new Packet(Packet.CONNECT, server.keys.getPublic().getEncoded());
				
				byte[] data = packet.serialize();
				DatagramPacket dp = new DatagramPacket(data, data.length, InetAddress.getByName("localhost"), 34341);
				socket.send(dp);
				
				lastTime = System.nanoTime();
				ape = !ape;
			}
		}
		*/
		//Client client = new Client(InetAddress.getByName("localhost"), Server.SERVER_PORT, "nigger");
		//client.start();
	}
	
}
