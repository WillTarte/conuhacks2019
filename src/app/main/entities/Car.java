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
	
	private float scale;
	private float theta;
	
	public Car(int hp, int armor, double x, double y, String id, Vector velocity, Polygon rect) {
		super(x, y, id, velocity, rect);
		this.hitpoints = hp;
		this.armor = armor;
		this.scale = 30.0f;
		this.theta = 0.0f;
		this.rect = Maths.generateFromAngle(theta, scale);
	}

	public void update() {
		this.rect = Maths.generateFromAngle(theta, scale);
	}
	
	public void render(Graphics g) {
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
}
