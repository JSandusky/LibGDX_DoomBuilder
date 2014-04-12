package com.doom.actions.door;

import com.badlogic.gdx.math.Interpolation;
import com.doom.Sector;
import com.doom.actions.ToggleAction;

public class DoorToggleAction extends ToggleAction {
	float stayOpenFor = 30f;
	float time = 0;
	float cycleTime;
	int topHeight;
	String keyCode = "";
	
	public DoorToggleAction(Sector target, float duration, float stayOpenFor, int openHeight) {
		super(target);
		cycleTime = duration;
		this.stayOpenFor = stayOpenFor;
		topHeight = openHeight;
	}

	@Override
	public boolean updateActive(float td) {
		if (stayOpenFor != -1) {
			time += td;
			if (time > stayOpenFor)
				deactivate();
		}
		return false;
	}

	@Override
	public boolean updateDeactivate(float td) {
		time += td;
		getSector().setCeilingHeight((int)Interpolation.linear.apply(topHeight,getSector().floorHeight,time/cycleTime));
		getSector().rebuildGeometry();
		if (time > cycleTime) {
			time = 0;
			return true;
		}
		return false;
	}
	
	@Override
	public void toggle() {
		super.toggle();
		time = 0;
		//jump time to 'close enough' for the purposes of closing while still opening or opening while still closing
		if (getSector().currentCeilingHeight != topHeight)
			time += (topHeight-getSector().currentCeilingHeight)/cycleTime;
	}

	@Override
	public boolean updateActivate(float td) {
		time += td;
		getSector().setCeilingHeight((int)Interpolation.linear.apply(getSector().floorHeight,topHeight,time/cycleTime));
		getSector().rebuildGeometry();
		if (time > cycleTime) {
			time = 0;
			return true;
		}
		return false;
	}
}
