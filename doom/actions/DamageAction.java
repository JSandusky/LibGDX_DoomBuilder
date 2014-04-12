package com.doom.actions;

import com.doom.Sector;
import com.doom.SectorAction;

/** a harmful surface such as lava or acid */
public class DamageAction extends SectorAction {
	int damageType;
	float dmgAmount;
	boolean effectMonsters; //players only?
	
	float time;
	
	public DamageAction(Sector target, float dmgAmount, boolean hurtMonsters, int typeCode) {
		super(target);
		this.dmgAmount = dmgAmount;
		effectMonsters = hurtMonsters;
		damageType = typeCode;
	}

	//??never removed
	@Override
	public boolean update(float td) {
		time += td;
		if (time > 0.5f) {
			//??do damage loop
			time = 0;
		}
		return false;
	}

}
