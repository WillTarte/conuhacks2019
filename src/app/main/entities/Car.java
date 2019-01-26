package app.main.entities;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;

import app.main.utils.Maths;
import app.main.utils.Vector;

public class Car extends Entity{
	
	private int hitpoints;
	private int armor;
	private Vector forward;
	
	
	private float scale;
	private float theta;
	private float rotationVelocity;
	
	private final static float speed = 0.005f;
	
	public Car(int hp, int armor, double x, double y, String id, Vector velocity, Polygon rect) {
		super(x, y, id, velocity, rect);
		this.hitpoints = hp;
		this.armor = armor;
		this.scale = 30.0f;
		this.theta = 0.0f;
		this.rect = Maths.generateFromAngle(theta, scale);
		this.rotationVelocity = 0;

	}

	public void update() {
		this.setRotation(this.theta + this.rotationVelocity);
		this.pos = Vector.add(this.pos, Vector.scale(speed, velocity));
		this.rect = Maths.generateFromAngle(theta, scale);
	}
	
	public void render(Graphics g) {
		//Implement switching from OUR coordinates to the frame's coordinates
		Graphics2D g2d = (Graphics2D)g;
		
		g2d.setColor(Color.BLACK);
		Polygon toRender = new Polygon(this.rect.xpoints, this.rect.ypoints, this.rect.npoints);
		Vector screenCoords = Maths.convert2screen(this.pos);
		toRender.translate((int)screenCoords.getX(), (int)screenCoords.getY());
		g2d.draw(toRender);
	}
	
	public void setRotation(float angle) {
		this.theta = angle;
	}
	
	public float getRotation() {
		return theta;
	}

	/**
	 * @param rotationVelocity the rotationVelocity to set
	 */
	public void setRotationVelocity(float rotationVelocity) {
		this.rotationVelocity = rotationVelocity;
	}
}
