package com.doom.things;

import com.badlogic.gdx.utils.XmlReader.Element;
import com.doom.Thing;

@SuppressWarnings("serial")
public class ProjectileThing extends Thing {
	int damageType;
	float damage;
	float distanceTraveled;
	
	public ProjectileThing(Element elem, int dmgType, float dmgValue) {
		super(elem);
		damageType = dmgType;
		damage = dmgValue;
	}

}
