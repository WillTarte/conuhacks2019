package app.main.entities;

import java.awt.Shape;

public class Entity {
	
	private double x, y;
	
	private String id;
	
	private Vector velocity;
	
	private Shape collision_rectangle;
	
	
	public Entity(double x, double y, String id, Vector velocity, Shape collision_rectangle) {
		this.x = x;
		this.y = y;
		this.id = id;
		this.velocity = velocity;
		this.collision_rectangle = collision_rectangle;
	}
}
