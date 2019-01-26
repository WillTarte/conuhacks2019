package app.main.utils;

import java.awt.Polygon;

import app.main.src.Game;

public class Maths {
	
	public static Vector convert2screen(Vector vector) {
		
		double x = (Game.frame.getContentPane().getWidth() / 2) * vector.getX() + (Game.frame.getContentPane().getWidth() / 2);
		double y = -(Game.frame.getContentPane().getHeight() / 2) * vector.getY() + (Game.frame.getContentPane().getHeight() / 2);
		
		return new Vector(x,y);
	}
	
	public static Polygon generateFromAngle(float angle, float scale) {
		Vector dir = new Vector(Math.cos(angle), Math.sin(angle));
		Vector perpDir = new Vector(Math.cos(angle - Math.PI / 2), Math.sin(angle - Math.PI / 2));
		
		Vector topRight = Vector.add(Vector.add(new Vector(), Vector.scale(scale, dir)), Vector.scale(scale / 2, perpDir));
		Vector topLeft = Vector.sub(Vector.add(new Vector(), Vector.scale(scale, dir)), Vector.scale(scale / 2, perpDir));
	
		Vector bottomRight = Vector.add(Vector.sub(new Vector(), Vector.scale(scale, dir)), Vector.scale(scale / 2, perpDir));
		Vector bottomLeft = Vector.sub(Vector.sub(new Vector(), Vector.scale(scale, dir)), Vector.scale(scale / 2, perpDir));
	
		return new Polygon(
				new int[] {(int)topLeft.getX(), (int)topRight.getX(), (int)bottomRight.getX(), (int)bottomLeft.getX()},
				new int[] {(int)topLeft.getY(), (int)topRight.getY(), (int)bottomRight.getY(), (int)bottomLeft.getY()}, 4);
	}
	
	
}
