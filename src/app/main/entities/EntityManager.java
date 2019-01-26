package app.main.entities;

import java.util.HashMap;

import javax.swing.text.html.parser.Entity;

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
	
	
}
