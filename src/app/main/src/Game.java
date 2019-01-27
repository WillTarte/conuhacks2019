
package app.main.src;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.geom.Area;
import java.awt.image.BufferStrategy;
import java.util.HashMap;
import java.util.Random;

import javax.swing.JFrame;

import app.main.entities.Boost;
import app.main.entities.Car;
import app.main.entities.Entity;
import app.main.entities.EntityManager;
import app.main.entities.Obstacle;
import app.main.network.Client;
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
	
	private Random rd = new Random();
	
	private int boostCount = 0;
	
	private int count = 0;
	
	private boolean toReset = false;
	private String [] obsID;
	
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
	Client client;
	
	
	
	@Override
	public void run() {
		
		Input input = new Input(this);
		this.addMouseListener(input);
		this.addKeyListener(input);
		
		init();
		
		int fps = 0, ticks = 0;
		long lastTick = System.nanoTime();
		long lastNet = System.nanoTime();
		long lastTime = System.nanoTime();
		while(running) {
			
			// EVERY TICK
			long currentTick = System.nanoTime();
			if(currentTick - lastTick >= 1000000000 / TICKS_PER_SEC) {
				tick(ticks);
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
	
	private void tick(int currentTick) {
		
		if(toReset) {
			reset();
			toReset = false;
		}
		
		HashMap<String, Entity> map = em.getEntityMap();
		
		if(currentTick == 0) {
			count++;
			boostCount++;
		}
		
		if(count == 3) {
			Car ennemy = new Car(100, (rd.nextDouble()-0.5)*32/9, (rd.nextDouble()-0.5)*2, Maths.generateFromAngle((float)Math.PI / 4, 30.0f, 60.0f),new Color(139,0,0));
			em.register(ennemy.getId(), ennemy);
			count = 0;
		}
		
		if (boostCount == 6) {
			Boost boost1 = new Boost((rd.nextDouble()-0.5)*32/9, (rd.nextDouble()-0.5)*2, Maths.generateHeart(10.0f));
			em.register(boost1.getId(), boost1);
			boostCount = 0;
		}
		
	
		
		
		
		Car playerCar = em.getPlayer();
		Polygon player = em.getPlayer().getShape();
		Vector pScreenCoords = Maths.convert2screen(em.getPlayer().getPos());
		player.translate((int)pScreenCoords.getX(), (int)pScreenCoords.getY());
		

		if (playerCar.getPos().getX() < (-16.0/9.0)) 
			{
				playerCar.setPos((new Vector((-16.0/9), playerCar.getPos().getY())));
			}
		else if (playerCar.getPos().getX() > (16.0/9.0)){
			
				playerCar.setPos((new Vector((16.0/9), playerCar.getPos().getY())));
			}
		if (playerCar.getPos().getY() > (1))
			{
				playerCar.setPos(new Vector(playerCar.getPos().getX(), 1));
			}
		else if (playerCar.getPos().getY() < (-1))
			{
				playerCar.setPos(new Vector(playerCar.getPos().getX(), -1));
			}
		
		for(String id:map.keySet()) {
			Entity ent = map.get(id);
			
			
			if(playerCar.getId() == id)
				continue;
				
			Polygon obstacle = ent.getShape();
			Vector screenCoords = Maths.convert2screen(ent.getPos());
			obstacle.translate((int)screenCoords.getX(), (int)screenCoords.getY());
			Area a = new Area(obstacle);
			a.intersect(new Area(player));
			
			//car obstacle collision
			if(ent.getType() == 0) {
				if (!a.isEmpty()) {
					if(em.getPlayer().getVelocity()>0) 
					{
						playerCar.setPos(Vector.add(playerCar.getPos(), Vector.scale(-playerCar.getVelocity() * Car.getSpeed()*2, new Vector(Math.sin(playerCar.getRotation()+Math.PI/2), Math.cos(playerCar.getRotation()+Math.PI/2)))));
						playerCar.setPos(playerCar.getLastpos());
						playerCar.setRotation(playerCar.getLastangle());
					}
					else if (em.getPlayer().getVelocity()<0)
					{
						playerCar.setPos(Vector.add(playerCar.getPos(), Vector.scale(-playerCar.getVelocity() * Car.getSpeed()*2 , new Vector(Math.sin(playerCar.getRotation()+Math.PI/2), Math.cos(playerCar.getRotation()+Math.PI/2)))));
						playerCar.setPos(playerCar.getLastpos());
						playerCar.setRotation(playerCar.getLastangle());
					}
					
					else if (em.getPlayer().getVelocity() == 0)
					{
						playerCar.setPos(Vector.add(playerCar.getPos(), Vector.scale(-playerCar.getVelocity() * Car.getSpeed()*2 , new Vector(Math.sin(playerCar.getRotation()+Math.PI/2), Math.cos(playerCar.getRotation()+Math.PI/2)))));
						playerCar.setPos(playerCar.getLastpos());
						playerCar.setRotation(playerCar.getLastangle());
					}
			}
		
				
			}
			else if(ent.getType() == 2) {
				
				
				if(!a.isEmpty()) {

					playerCar.heal(20);

					em.remove(id);
					break;
				}
			}
		
			else if(ent.getType() == 1) {
				
				if(ent.getId().equals(playerCar.getId()))
					break;
				
				if(!a.isEmpty()) {
					playerCar.damage(30);
					
					em.remove(id);
					break;
				}
				
				//PATHFINDING BOYS
				Vector diff = Vector.sub(playerCar.getPos(), ent.getPos());
				double angle = diff.getX()<0?-diff.angle()+Math.PI:-diff.angle();				
				((Car)ent).setRotation((float)angle);
				((Car)ent).setVelocity(1f);
				
				
				
				for(int i=0;i<obsID.length;i++)
				{
					
					Area area = new Area(obstacle);
					
					Entity wall = map.get(obsID[i]);
					Polygon wallShape = wall.getShape();
					Vector sc = Maths.convert2screen(wall.getPos());
					wallShape.translate((int)sc.getX(), (int)sc.getY());
					
					area.intersect(new Area(wallShape));
					if (!area.isEmpty())
						ent.setPos(new Vector((rd.nextDouble()-0.5)*32/9, (rd.nextDouble()-0.5)*2));
					
				}
				
				
			}
				
		}
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
		Graphics2D g2d = (Graphics2D) g;

	    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, frame.getWidth(), frame.getHeight());
		
		// RENDER CODE GOES HERE
		this.em.render(g);
		
		g.setColor(Color.LIGHT_GRAY);
		g.fillRect(20, frame.getContentPane().getHeight() - 80, frame.getContentPane().getWidth()/3, 50);
		
		g.setColor(Color.GREEN);
		g.fillRect(20, frame.getContentPane().getHeight() - 80, frame.getContentPane().getWidth()* em.getPlayer().getHP()/300, 50);
		
		g.setColor(Color.BLACK);
		g.drawRect(20, frame.getContentPane().getHeight() - 80, frame.getContentPane().getWidth()/3, 50);
		

		g.dispose();
		bs.show();
		
	}
	
	private void init() {
		//load levels here
		genObstacles();
		Car car = new Car(100, 0, 0, Maths.generateFromAngle((float)Math.PI / 4, 30.0f, 60.0f),new Color(0,0,139));
		
		boolean flag = false;
		int count = 0;
		while(!flag) {
			for(int i=0;i<obsID.length;i++){
				
				Polygon carShape = car.getShape();
				Vector scCar = Maths.convert2screen(car.getPos());
				carShape.translate((int)scCar.getX(), (int)scCar.getY());
				Area area = new Area(car.getShape());
				
				Entity wall = em.getEntityMap().get(obsID[i]);
				Polygon wallShape = wall.getShape();
				Vector sc = Maths.convert2screen(wall.getPos());
				wallShape.translate((int)sc.getX(), (int)sc.getY());
				
				area.intersect(new Area(wallShape));
				if (!area.isEmpty()){
					count++;
					
				}
	
			}
			
			if(count > 0) {
				car.setPos(new Vector((rd.nextDouble()-0.5)*32/9, (rd.nextDouble()-0.5)*2));
				count = 0;
				flag = false;
			}else
				flag = true;
		}
		//Obstacle box = new Obstacle(0.5, 0.5, Maths.generateFromAngle((float)Math.PI / 4, 60.0f, 30.0f));
		//Obstacle box2 = new Obstacle(-0.5, -0.5, Maths.generateFromAngle((float)Math.PI/4, 60.0f, 30.0f));
		//Obstacle box3 = new Obstacle(0.5, -0.5, Maths.generateFromAngle((float)Math.PI/4, 60.0f, 30.0f));
		Boost boost1 = new Boost((rd.nextDouble()-0.5)*32/9, (rd.nextDouble()-0.5)*2, Maths.generateHeart(10.0f));
		Boost boost2 = new Boost((rd.nextDouble()-0.5)*32/9, (rd.nextDouble()-0.5)*2, Maths.generateHeart(10.0f));
		
		em.setPlayer(car);
		//em.register(box.getId(), box);
		//em.register(box2.getId(), box2);
		//em.register(box3.getId(), box3);
		em.register(boost1.getId(), boost1);
		em.register(boost2.getId(), boost2);
	

	}
	
	public Car getPlayer() {
		return this.em.getPlayer();
	}
	
	public void shouldReset() {
		this.toReset = true;
	}
	
	private void reset() {
		this.em.getEntityMap().clear();
		init();
	}
	
	private void genObstacles() {
		int num = rd.nextInt(7);
		obsID = new String[num];
		for(int i=0;i<num;i++)
		{
			Obstacle box = new Obstacle((rd.nextDouble()-0.5)*32/9, (rd.nextDouble()-0.5)*2, Maths.generateFromAngle((float)Math.PI*2*rd.nextFloat(), rd.nextFloat()*120+30, rd.nextFloat()*120+30));
			em.register(box.getId(), box);
			obsID[i] = box.getId();
		}
		
		
	}
}
