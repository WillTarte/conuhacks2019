package app.main.entities;

import java.awt.Graphics;
import java.util.HashMap;

import app.main.entities.Entity;

public class EntityManager {
	
	private HashMap<String, Entity> entityMap = new HashMap<String, Entity>();
	
	public void register(String id, Entity ent) {
		entityMap.put(id, ent);
	}
	
	public void remove(Entity ent) {
		for(String eID : entityMap.keySet())
			if(entityMap.get(eID) == ent)
				entityMap.remove(eID);
	}
	
	public void render(Graphics g) {
		for(String eID : entityMap.keySet())
			entityMap.get(eID).render(g);
	}
	
	public void update() {
		for(String eID : entityMap.keySet())
			entityMap.get(eID).update();
	}
	
	
}
