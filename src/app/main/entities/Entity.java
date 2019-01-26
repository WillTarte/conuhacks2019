package app.main.entities;

import java.awt.Shape;

public class Entity {
	
	static int num_entities = 0;
	
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
		Entity.num_entities++;
	}
}
