package app.main.entities;

import java.awt.Graphics;
import java.awt.Polygon;

import app.main.utils.Vector;

public abstract class Entity {
	
	static int num_entities = 0;
	
	protected Vector pos;
	
	private String id;
	
	protected Polygon rect;
	
	//0-obstacles
	//1-player
	//2-pickups
	private int type;
	
	
	
	public Entity(int type, double x, double y, String id, Polygon rect) {
		this.pos = new Vector(x, y);
		this.id = id;
		this.rect = rect;
		Entity.num_entities++;
	}
	
	public Entity(int type, double x, double y, Polygon rect) {
		
		this.pos = new Vector(x, y);
		this.id = java.util.UUID.randomUUID().toString().split("-")[0];
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
	
	public int getType() {
		return this.type;
	}
	
	public Polygon getShape() {
		
		return new Polygon(this.rect.xpoints, this.rect.ypoints, this.rect.npoints);
	}

}
