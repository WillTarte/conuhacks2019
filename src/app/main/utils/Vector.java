package app.main.utils;

public class Vector {
	
	private double x;
	
	private double y;
	
	public Vector() {
		this.x = 0;
		this.y = 0;
	}
	
	public Vector(double x, double y) {
		this.x = x;
		this.y = y;
	}
	
	public double getX() {
		return x;
	}
	public void setX(double x) {
		this.x = x;
	}
	public double getY() {
		return y;
	}
	public void setY(double y) {
		this.y = y;
	}
	
	public double length() {
		double x = this.x;
		double y = this.y;
		double length = Math.sqrt(x*x+y*y);
		
		return length;
	}
	
	public Vector unitVec() {
		double length = this.length();
		double scaled_x = this.x / length;
		double scaled_y = this.x / length;
		return new Vector(scaled_x, scaled_y);
	}
	
	public double angle() {
		double x = this.x;
		double y = this.y;
		double angle = Math.atan(y/x)*(Math.PI/180);
		return angle;
	}
	
	public static Vector scale(double scale, Vector vec) {
		return new Vector(scale * vec.x, scale * vec.y);
	}
	
	public static Vector add(Vector v1, Vector v2) {
		return new Vector(v1.x + v2.x, v1.y + v2.y);
	}
	
	public static Vector sub(Vector v1, Vector v2) {
		return new Vector(v1.x - v2.x, v1.y - v2.y);
	}
	
	public static double dot(Vector v1, Vector v2) {
		return v1.x * v2.x + v1.y * v2.y;
	}
	
	@Override
	public String toString() {
		return x + ", " + y;
	}
}
