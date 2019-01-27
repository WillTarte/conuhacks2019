
package app.main.src;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Polygon;
import java.awt.geom.Area;
import java.awt.image.BufferStrategy;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Random;

import javax.swing.JFrame;

import app.main.entities.Boost;
import app.main.entities.Car;
import app.main.entities.Entity;
import app.main.entities.EntityManager;
import app.main.network.Client;
import app.main.network.Packet;
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
	
	private int count = 0;
	
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

				if(client.connected()) {
					String msg = "e|id=" + em.getPlayer().getId() + 
							",x=" + Float.toString((float)em.getPlayer().getPos().getX()) + 
							",y=" + Float.toString((float)em.getPlayer().getPos().getY()) + 
							",ang=" + Float.toString((float)em.getPlayer().getRotation()) + ";";
					client.sendPacket(Packet.GAME_UPDATE, msg.getBytes(), false);
					if(System.currentTimeMillis() - client.lastPacket >= Client.TIMEOUT) {
						client.connected = false;
						client.loggedIn = false;
						System.out.println("Connection to " + client.host.toString() + " timed out.");
					}
				}
				
				lastNet = currentNet;
			}
			
			// EVERY SECOND
			long currentTime = System.nanoTime();
			if(currentTime - lastTime >= 1000000000) {
				
				if(!client.connected)
					client.connect();
				else if(!client.loggedIn)
					client.login();
				else {
					client.sendPacket(Packet.PING, null, false);
					
					System.out.println("Connected to " + client.host + ":" + client.port + " (" + client.ping + " ms)");
				}
				
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
		
		HashMap<String, Entity> map = em.getEntityMap();
		
		if(currentTick == 0)
			count++;
		
		if(count == 3) {
			Car ennemy = new Car(100, 100, (rd.nextDouble()-0.5)*32/9, (rd.nextDouble()-0.5)*2, Maths.generateFromAngle((float)Math.PI / 4, 30.0f, 60.0f));
			
			em.register(ennemy.getId(), ennemy);
			count = 0;
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
		
		Car car = new Car(100, 100, 0, 0, Maths.generateFromAngle((float)Math.PI / 4, 30.0f, 60.0f));
		Obstacle box = new Obstacle(0.5, 0.5, Maths.generateFromAngle((float)Math.PI / 4, 60.0f, 30.0f));
		Obstacle box2 = new Obstacle(-0.5, -0.5, Maths.generateFromAngle((float)Math.PI/4, 60.0f, 30.0f));
		Obstacle box3 = new Obstacle(0.5, -0.5, Maths.generateFromAngle((float)Math.PI/4, 60.0f, 30.0f));
		Boost boost1 = new Boost(0.2, 0.8, Maths.generateFromAngle((float)Math.PI/4, 20.0f, 20.0f));
		Boost boost2 = new Boost(0.7, 0.2, Maths.generateFromAngle((float)Math.PI/4, 20.0f, 20.0f));
		
		
		Input input = new Input(car);
		this.addMouseListener(input);
		this.addKeyListener(input);
		
		em.setPlayer(car);
		em.register(box.getId(), box);
		em.register(box2.getId(), box2);
		em.register(box3.getId(), box3);
		em.register(boost1.getId(), boost1);
		em.register(boost2.getId(), boost2);

		
		
		try {
			client = new Client(InetAddress.getByName("172.30.181.242"), 42353, "player", em);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

	}
}
