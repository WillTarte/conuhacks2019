package app.main.utils;

import java.awt.Polygon;

import app.main.src.Game;

public class Maths {
	
	public static Vector convert2screen(Vector vector) {
		
		double x = (Game.frame.getContentPane().getWidth() / 2) * vector.getX()/(16.0/9.0)  + (Game.frame.getContentPane().getWidth() / 2);
		double y = -(Game.frame.getContentPane().getHeight() / 2) * vector.getY() + (Game.frame.getContentPane().getHeight() / 2);
		
		return new Vector(x,y);
	}
	
	public static Polygon generateFromAngle(float angle, float scaleX, float scaleY) {
		Vector dir = new Vector(Math.cos(angle), Math.sin(angle));
		Vector perpDir = new Vector(Math.cos(angle - Math.PI / 2), Math.sin(angle - Math.PI / 2));
		
		Vector topRight = Vector.add(Vector.add(new Vector(), Vector.scale(scaleY, dir)), Vector.scale(scaleX, perpDir));
		Vector topLeft = Vector.sub(Vector.add(new Vector(), Vector.scale(scaleY, dir)), Vector.scale(scaleX, perpDir));
	
		Vector bottomRight = Vector.add(Vector.sub(new Vector(), Vector.scale(scaleY, dir)), Vector.scale(scaleX, perpDir));
		Vector bottomLeft = Vector.sub(Vector.sub(new Vector(), Vector.scale(scaleY, dir)), Vector.scale(scaleX, perpDir));
	
		return new Polygon(
				new int[] {(int)topLeft.getX(), (int)topRight.getX(), (int)bottomRight.getX(), (int)bottomLeft.getX()},
				new int[] {(int)topLeft.getY(), (int)topRight.getY(), (int)bottomRight.getY(), (int)bottomLeft.getY()}, 4);
	}
	
	public static Polygon generateHeart(float scale) {
		return new Polygon(
				new int[] {(int)(1.6f * scale), (int)(1.0f * scale), (int)(0.4f * scale), 0, (int)(-0.4f * scale), (int)(-1.0f * scale),(int)(-1.6f * scale), (int)(-2.0f * scale),  (int)(-1.5f * scale), 0, (int)(1.5f * scale), (int)(2.0f * scale)},
				new int[] {(int)(-0.8f * scale), (int)(-1.0f * scale), (int)(-0.8f * scale), 0, (int)(-0.8f * scale), (int)(-1.0f * scale), (int)(-0.8f * scale),  0,  (int)(1.1f * scale), (int)(3.0f * scale), (int)(1.1f * scale), 0}, 12);
	}
	
	
}
