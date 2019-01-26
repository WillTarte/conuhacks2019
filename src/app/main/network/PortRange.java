package app.main.network;

public class PortRange {
	
	private int fromPort, toPort;
	
	
	protected void checkPorts() {
		if(toPort > fromPort)
			throw new RuntimeException("Invalid port range.");
	}
	
	public static PortRange create() {
		return new PortRange();
	}
	
	public PortRange to(int port) {
		this.toPort = port;
		return this;
	}
	
	public PortRange from(int port) {
		this.fromPort = port;
		return this;
	}
	
	protected int getFrom() {
		return fromPort;
	}
	
	protected int getTo() {
		return toPort;
	}
	
}
