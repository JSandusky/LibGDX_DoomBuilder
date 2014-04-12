package com.doom.actions;

import com.badlogic.gdx.math.Interpolation;
import com.doom.Sector;

/** as opposed to the Ceiling crushing, this causes the floor to crush */
public class FloorCrusherAction extends ToggleAction {
	float damage;
	float slowFactor = 1f;
	float time;
	float cycleDuration;
	boolean rising = false;
	
	float lerpStart;

	public FloorCrusherAction(Sector target, float halfCycleTime, float dmg, float slowDownForCrushed) {
		super(target);
		
		slowFactor = slowDownForCrushed;
		damage = dmg;
		time = 0;
		cycleDuration = halfCycleTime;
	}
	
	@Override
	public void deactivate() {
		super.deactivate();
		lerpStart = getSector().currentFloorHeight;
	}

	@Override
	public boolean updateActive(float td) {
		time += td;
		if (rising) {
			getSector().setFloorHeight((int)Interpolation.linear.apply(getSector().floorHeight, getSector().ceilingHeight-8, time/cycleDuration));
			if (time >= cycleDuration) {
				rising = false;
				time = 0;
			}
		} else {
			getSector().setFloorHeight((int)Interpolation.linear.apply(getSector().ceilingHeight-8, getSector().floorHeight, time/cycleDuration));
			if (time >= cycleDuration) {
				rising = true;
				time = 0;
			}
		}
		return false;
	}

	@Override
	public boolean updateDeactivate(float td) {
		getSector().setFloorHeight((int)Interpolation.linear.apply(lerpStart, getSector().floorHeight,time/cycleDuration));
		return getSector().floorHeight == getSector().currentFloorHeight;
	}

	@Override
	public boolean updateActivate(float td) {
		return true; //just go straight into being active
	}
}
