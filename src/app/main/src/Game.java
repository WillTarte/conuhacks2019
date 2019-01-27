
package app.main.src;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Polygon;
import java.awt.geom.Area;
import java.awt.image.BufferStrategy;
import java.util.HashMap;

import javax.swing.JFrame;

import app.main.entities.Boost;
import app.main.entities.Car;
import app.main.entities.Entity;
import app.main.entities.EntityManager;
import app.main.utils.Input;
import app.main.utils.Maths;
import app.main.utils.Vector;

public class Game extends Canvas implements Runnable{
	
	// FUCKING ECLIPSE GETS TRIGGERED IF YOU DONT HAVE THIS
	private static final long serialVersionUID = 1L;
	
	
	private static final int TICKS_PER_SEC = 60;
	private static final int NET_PER_SEC = 25;
	
	public static JFrame frame;
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
	
	
	EntityManager em = new EntityManager();
	
	
	
	@Override
	public void run() {
		init();
		
		
		
		int fps = 0, ticks = 0;
		long lastTick = System.nanoTime();
		long lastNet = System.nanoTime();
		long lastTime = System.nanoTime();
		while(running) {
			
			// EVERY TICK
			long currentTick = System.nanoTime();
			if(currentTick - lastTick >= 1000000000 / TICKS_PER_SEC) {
				tick();
				ticks++;
				lastTick = currentTick;
			} 
			
			// EVERY NETWORK TICK
			long currentNet = System.nanoTime();
			if(currentNet - lastNet >= 1000000000 / NET_PER_SEC) {
				lastNet = currentNet;
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
	
	private void tick() {
		
		HashMap<String, Entity> map = em.getEntityMap();
		
		Car playerCar = em.getPlayer();
		Polygon player = em.getPlayer().getShape();
		Vector pScreenCoords = Maths.convert2screen(em.getPlayer().getPos());
		player.translate((int)pScreenCoords.getX(), (int)pScreenCoords.getY());
		
		for(String id:map.keySet())
			//car obstacle collision
			if(map.get(id).getType() == 0) {
				Polygon obstacle = map.get(id).getShape();
				Vector screenCoords = Maths.convert2screen(map.get(id).getPos());
				obstacle.translate((int)screenCoords.getX(), (int)screenCoords.getY());
				Area a = new Area(obstacle);
				
				
				
				a.intersect(new Area(player));
				if (!a.isEmpty()) {
					if(em.getPlayer().getVelocity()>0) 
					{
						playerCar.setPos(Vector.add(playerCar.getPos(), Vector.scale(-playerCar.getVelocity() * Car.getSpeed()*2, new Vector(Math.sin(playerCar.getRotation()+Math.PI/2), Math.cos(playerCar.getRotation()+Math.PI/2)))));
						playerCar.setPos(playerCar.getLastpos());
						//playerCar.setRotation(playerCar.getLastangle());
					}
					else if (em.getPlayer().getVelocity()<0)
					{
						playerCar.setPos(Vector.add(playerCar.getPos(), Vector.scale(-playerCar.getVelocity() * Car.getSpeed()*2 , new Vector(Math.sin(playerCar.getRotation()+Math.PI/2), Math.cos(playerCar.getRotation()+Math.PI/2)))));
						playerCar.setPos(playerCar.getLastpos());
						//playerCar.setRotation(playerCar.getLastangle());
					}
					
					else if (em.getPlayer().getVelocity() == 0)
					{
						playerCar.setPos(Vector.add(playerCar.getPos(), Vector.scale(-playerCar.getVelocity() * Car.getSpeed()*2 , new Vector(Math.sin(playerCar.getRotation()+Math.PI/2), Math.cos(playerCar.getRotation()+Math.PI/2)))));
						//playerCar.setRotation(playerCar.getLastangle());
					}
			}
		
				
			}
			/*else if(map.get(id).getType() == 2) {
				em.remove(id);
				continue;
			}*/
		
		em.update();
		// GAME LOGIC GOES HERE
		
	}
	
	private void render() {
		BufferStrategy bs = getBufferStrategy();
		if(bs == null) {
			createBufferStrategy(2);
			bs = getBufferStrategy();
		}
		Graphics g = bs.getDrawGraphics();
		
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, frame.getWidth(), frame.getHeight());
		
		// RENDER CODE GOES HERE
		this.em.render(g);
		
		g.dispose();
		bs.show();
		
	}
	
	private void init() {
		//load levels here
		
		Car car = new Car(2000, 100, 0, 0, "abc", Maths.generateFromAngle((float)Math.PI / 4, 30.0f, 60.0f));
		Obstacle box = new Obstacle(0.5, 0.5, Maths.generateFromAngle((float)Math.PI / 4, 60.0f, 30.0f));
		Obstacle box2 = new Obstacle(-0.5, -0.5, Maths.generateFromAngle((float)Math.PI/4, 60.0f, 30.0f));
		Obstacle box3 = new Obstacle(0.5, -0.5, Maths.generateFromAngle((float)Math.PI/4, 60.0f, 30.0f));
		Boost boost1 = new Boost(0.2, 0.8, Maths.generateFromAngle((float)Math.PI/4, 20.0f, 20.0f));
		
		Input input = new Input(car);
		this.addMouseListener(input);
		this.addKeyListener(input);
		
		em.setPlayer(car);
		em.register(box.getId(), box);
		em.register(box2.getId(), box2);
		em.register(box3.getId(), box3);

		System.out.println(box.getId());
		System.out.println(box2.getId());

		em.register(boost1.getId(), boost1);

		
	}
}
