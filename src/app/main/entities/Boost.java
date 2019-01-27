package app.main.entities;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;

import app.main.utils.Maths;
import app.main.utils.Vector;

public class Boost extends Entity{

	public Boost(int type, double x, double y, Polygon rect) {
		super(type, x, y, rect);
		// TODO Auto-generated constructor stub
	}
	
	public Boost(double x, double y, Polygon rect) {
		super(2, x, y, rect);
	}

	@Override
	public void update() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void render(Graphics g) {
		Graphics2D g2d = (Graphics2D)g;
		
		g2d.setColor(Color.GREEN);
		Polygon toRender = new Polygon(this.rect.xpoints, this.rect.ypoints, this.rect.npoints);
		Vector screenCoords = Maths.convert2screen(this.pos);
		toRender.translate((int)screenCoords.getX(), (int)screenCoords.getY());
		g2d.fill(toRender);
		
	}
	
	
	
	

}
