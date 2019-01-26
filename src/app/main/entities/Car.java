package app.main.entities;

import java.awt.Shape;

import app.main.utils.Vector;

public class Car extends Entity{
	
	private int hitpoints;
	private int armor;
	
	public Car(int hp, int armor, double x, double y, String id, Vector velocity, Shape rect) {
		super(x, y, id, velocity, rect);
		this.hitpoints = hp;
		this.armor = armor;
	}
	
	
}
