package app.main.utils;

import app.main.src.Game;

public class Maths {
	
	public static Vector convert2screen(Vector vector) {
		
		double x = (Game.frame.getContentPane().getWidth() /2) * vector.getX() + (Game.frame.getContentPane().getWidth()/2);
		double y = (Game.frame.getContentPane().getHeight() /2) * vector.getY() + (Game.frame.getContentPane().getHeight()/2);
		
		return new Vector(x,y);
	}
	
	
}
