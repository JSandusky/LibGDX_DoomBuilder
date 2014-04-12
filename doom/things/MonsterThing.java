package com.doom.things;

import com.badlogic.gdx.utils.XmlReader.Element;
import com.doom.Thing;

@SuppressWarnings("serial")
public class MonsterThing extends Thing {
	float health;
	float armor;
	
	
	
	public float getHealth() {return health;}
	public float getArmor() {return armor;}
	public void setHealth(float val) {health = val;}
	public void setArmor(float val) {armor = val;}
	
	public boolean isDead() {return health <= 0;}
	
	public MonsterThing(Element elem) {
		super(elem);
		// TODO Auto-generated constructor stub
	}

}
