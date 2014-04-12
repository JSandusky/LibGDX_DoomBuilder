package com.doom.actions;

import com.badlogic.gdx.math.Interpolation;
import com.doom.Sector;

/** a murderous collapsing ceiling*/
public class CrusherAction extends ToggleAction {
	float damage;
	float slowFactor = 1f;
	float time;
	float cycleDownDuration;
	float cycleUpDuration;
	boolean rising = false;
	
	float lerpStart;

	public CrusherAction(Sector target, float halfCycleTime, float dmg, float slowDownForCrushed) {
		this(target,halfCycleTime,halfCycleTime,dmg,slowDownForCrushed);
	}
	
	public CrusherAction(Sector target, float downCycleTime, float upCycleTime, float dmg, float slowDownForCrushed) {
		super(target);
		
		slowFactor = slowDownForCrushed;
		damage = dmg;
		time = 0;
		cycleDownDuration = downCycleTime;
		cycleUpDuration = upCycleTime;
	}
	
	
	
	@Override
	public void deactivate() {
		super.deactivate();
		lerpStart = getSector().ceilingHeight;
	}

	@Override
	public boolean updateActive(float td) {
		time += td;
		if (rising) {
			getSector().setCeilingHeight((int)Interpolation.linear.apply(getSector().floorHeight+8, getSector().ceilingHeight,time/cycleUpDuration));
			if (time >= cycleUpDuration) {
				rising = false;
				time = 0;
			}
		} else {
			getSector().setCeilingHeight((int)Interpolation.linear.apply(getSector().ceilingHeight, getSector().floorHeight+8, time/cycleDownDuration));
			if (time >= cycleDownDuration) {
				rising = true;
				time = 0;
			}
		}
		return false;
	}

	@Override
	public boolean updateDeactivate(float td) {
		getSector().setCeilingHeight((int)Interpolation.linear.apply(lerpStart, getSector().ceilingHeight,time/cycleUpDuration));
		return getSector().ceilingHeight == getSector().currentCeilingHeight;
	}

	@Override
	public boolean updateActivate(float td) {
		return true; //just go straight into being active
	}

}
