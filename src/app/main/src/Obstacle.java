package app.main.src;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;

import app.main.entities.Entity;
import app.main.utils.Maths;
import app.main.utils.Vector;

public class Obstacle extends Entity{
	
	public Obstacle(double x, double y, String id, Polygon rect) {
		super(0, x, y, id, rect);
	}
	
	public Obstacle(double x, double y, Polygon rect) {
		super(0, x, y, rect);
	}
	
	public void render(Graphics g) {
		Graphics2D g2d = (Graphics2D)g;
		
		g2d.setColor(Color.GRAY);
		Polygon toRender = new Polygon(this.rect.xpoints, this.rect.ypoints, this.rect.npoints);
		Vector screenCoords = Maths.convert2screen(this.pos);
		toRender.translate((int)screenCoords.getX(), (int)screenCoords.getY());
		g2d.fill(toRender);
	}

	@Override
	public void update() {
		// TODO Auto-generated method stub
		
	}
}
