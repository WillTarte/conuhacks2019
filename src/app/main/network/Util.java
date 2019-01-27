package app.main.network;

public class Util {
	
	public static void copy(byte[] from, byte[] to, int index) {
		
		if(from == null || to == null)
			return;
		
		if(from.length + index > to.length)
			return;
		
		for(int i = 0; i < from.length; i++)
			to[i + index] = from[i];
	}
	
}
