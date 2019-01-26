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

	/**
	 * @return the num_entities
	 */
	public static int getNum_entities() {
		return num_entities;
	}

	/**
	 * @param num_entities the num_entities to set
	 */
	public static void setNum_entities(int num_entities) {
		Entity.num_entities = num_entities;
	}

	/**
	 * @return the pos
	 */
	public Vector getPos() {
		return pos;
	}

	/**
	 * @param pos the pos to set
	 */
	public void setPos(Vector pos) {
		this.pos = pos;
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @return the velocity
	 */
	public Vector getVelocity() {
		return velocity;
	}

	/**
	 * @param velocity the velocity to set
	 */
	public void setVelocity(Vector velocity) {
		this.velocity = velocity;
	}

	/**
	 * @return the rect
	 */
	public Shape getRect() {
		return rect;
	}

	/**
	 * @param rect the rect to set
	 */
	public void setRect(Shape rect) {
		this.rect = rect;
	}
}
