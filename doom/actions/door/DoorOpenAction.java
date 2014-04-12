package com.doom.actions.door;

import com.badlogic.gdx.math.Interpolation;
import com.doom.Sector;
import com.doom.SectorAction;

/** opens a door */
public class DoorOpenAction extends SectorAction {
	float time;
	float duration;
	int targetHeight;
	
	public DoorOpenAction(Sector target, float duration, int openHeight) {
		super(target);
		this.duration = duration;
		targetHeight = openHeight;
	}

	@Override
	public boolean update(float td) {
		time += td;
		if (time <= duration) {
			getSector().setCeilingHeight((int)Interpolation.linear.apply(getSector().currentFloorHeight, targetHeight,time/duration));
			getSector().rebuildGeometry();
			return false;
		}
		getSector().setCeilingHeight(targetHeight);
		getSector().rebuildGeometry();
		return true;
	}
}
