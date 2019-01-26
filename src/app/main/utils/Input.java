package app.main.utils;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import app.main.entities.Car;

public class Input implements KeyListener, MouseListener{
	
	private Car player;
	
	public Input(Car player) {
		this.player = player;
	}
	
	@Override
	public void keyPressed(KeyEvent arg0) {
		//left arrow: 37
		//up arrow: 38
		//right arrow: 39
		//down arrow 40
		int code = arg0.getKeyCode();
		System.out.println(code);
		if(code == 38) {
			player.setVelocity(new Vector(Math.sin(player.getRotation()+Math.PI/2), Math.cos(player.getRotation()+Math.PI/2)));
		}
		else if(code == 40){
			player.setVelocity(new Vector(-Math.sin(player.getRotation()+Math.PI/2), -Math.cos(player.getRotation()+Math.PI/2)));
		}
		else if(code == 39) {
			player.setRotationVelocity(0.03f);
		}
		else if(code == 37) {
			player.setRotationVelocity(-0.03f);
		}
		
		
	}

	@Override
	public void keyReleased(KeyEvent arg0) {
		//left arrow: 37
		//up arrow: 38
		//right arrow: 39
		//down arrow 40
		int code = arg0.getKeyCode();
		System.out.println(code);
		if(code == 38) {
			player.setVelocity(new Vector());
		}
		else if(code == 40){
			player.setVelocity(new Vector());
			
		}
		else if(code == 39) {
			player.setRotationVelocity(0);
		}
		else if(code == 37) {
			player.setRotationVelocity(0);
		}
		
	}

	@Override
	public void keyTyped(KeyEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseClicked(MouseEvent arg0) {
		//Dont use this
		
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent arg0) {
		System.out.println(arg0.getX() + ", " + arg0.getY());
		
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	
	

}
