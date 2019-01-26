package app.main.src;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferStrategy;

import javax.swing.JFrame;

public class Game extends Canvas implements Runnable{

	// FUCKING ECLIPSE GETS TRIGGERED IF YOU DONT HAVE THIS
	private static final long serialVersionUID = 1L;
	
	
	private static final int TICKS_PER_SEC = 60;
	
	private static JFrame frame;
	private static Thread gameThread;
	private static boolean running = false;
	
	public static void main(String[] args) {
		
		frame = Display.create(1280, 720, "This is a game");
		
		Game g = new Game();
		frame.add(g);
		
		g.startGame();

	}

	public synchronized void startGame() {
		if(running) return;
		
		running = true;
		
		gameThread = new Thread(this);
		gameThread.start();
		
	}
	
	private synchronized void stopGame() {
		if(!running) return;
		
		running = false;
		
		try {
			gameThread.join();
		}catch(Exception e) {
			System.exit(-1);
		}
	}
	
	
	@Override
	public void run() {
		
		int fps = 0, ticks = 0;
		long lastTick = System.nanoTime();
		long lastTime = System.nanoTime();
		while(running) {
			
			// EVERY TICK
			long currentTick = System.nanoTime();
			if(currentTick - lastTick >= 1000000000 / TICKS_PER_SEC) {
				tick();
				ticks++;
				lastTick = currentTick;
			} 
			
			// EVERY SECOND
			long currentTime = System.nanoTime();
			if(currentTime - lastTime >= 1000000000) {
				System.out.println("FPS : " + fps + ", TICKS : " + ticks);
				fps = ticks = 0;
				lastTime = currentTime;
			} 
			
			
			render();
			fps++;
			
		}
		stopGame();
	}
	
	public void tick() {
		
		// GAME LOGIC GOES HERE
		
	}
	
	public void render() {
		BufferStrategy bs = getBufferStrategy();
		if(bs == null) {
			createBufferStrategy(2);
			bs = getBufferStrategy();
		}
		Graphics g = bs.getDrawGraphics();
		
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, frame.getWidth(), frame.getHeight());
		
		// RENDER CODE GOES HERE
		
		g.dispose();
		bs.show();
		
	}
}