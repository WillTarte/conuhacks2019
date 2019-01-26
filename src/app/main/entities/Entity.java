package app.main.entities;

import java.awt.Graphics;
import java.awt.Shape;

import app.main.utils.Vector;

public abstract class Entity {
	
	static int num_entities = 0;
	
	protected Vector pos;
	
	private String id;
	
	protected Vector velocity;
	
	protected Shape rect;
	
	
	public Entity(double x, double y, String id, Vector velocity, Shape rect) {
		this.pos = new Vector(x, y);
		this.id = id;
		this.velocity = velocity;
		this.rect = rect;
		Entity.num_entities++;
	}
	
	abstract public void update();
	abstract public void render(Graphics g);
}
