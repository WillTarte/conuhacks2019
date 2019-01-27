package app.main.network;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class TestClient {
	
	public static void main(String[] args) throws UnknownHostException, Exception {
		
		Client client = new Client(InetAddress.getByName("localhost"), 42353, "APE");
		client.start();
		
	}
	
}
