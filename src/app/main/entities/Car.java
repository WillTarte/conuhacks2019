package app.main.entities;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;

import app.main.utils.Vector;

public class Car extends Entity{
	
	private int hitpoints;
	private int armor;
	private Vector forward;
	
	
	public Car(int hp, int armor, double x, double y, String id, Vector velocity, Shape rect) {
		super(x, y, id, velocity, rect);
		this.hitpoints = hp;
		this.armor = armor;
		this.forward = new Vector(0,-1);
	}

	public void update() {
		//every tick call this method
		//take the calling car's velocity and add it to its position
		
		return;
	}
	
	public void render(Graphics g) {
		//Implement switching from OUR coordinates to the frame's coordinates
		Graphics2D g2d = (Graphics2D)g;
		g2d.setColor(Color.BLACK);
		g2d.draw(this.rect);
	}
	
}
