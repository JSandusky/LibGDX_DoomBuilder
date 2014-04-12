package com.doom.actions;

import com.badlogic.gdx.math.Vector2;
import com.doom.Sector;
import com.doom.SectorAction;

public class WindAction extends SectorAction {
	Vector2 dir;
	float force;
	public WindAction(Sector target, Vector2 windDir, float windForce) {
		super(target);
		dir = new Vector2(windDir);
		force = windForce;
	}
	@Override
	public boolean update(float td) {
		return false;
	}

}
