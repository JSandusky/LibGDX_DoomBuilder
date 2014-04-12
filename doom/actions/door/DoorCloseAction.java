package com.doom.actions.door;

import com.badlogic.gdx.math.Interpolation;
import com.doom.Sector;
import com.doom.SectorAction;

/** opens a door */
public class DoorCloseAction extends SectorAction {
	float time;
	float duration;
	float targetHeight;
	String keyCode = "";
	
	public DoorCloseAction(Sector target, float duration) {
		super(target);
		this.duration = duration;
		targetHeight = getSector().currentFloorHeight;
	}

	@Override
	public boolean update(float td) {
		time += td;
		if (time <= duration) {
			getSector().setCeilingHeight((int)Interpolation.linear.apply(getSector().currentCeilingHeight, targetHeight,time/duration));
			getSector().rebuildGeometry();
			return false;
		}
		getSector().setCeilingHeight(targetHeight);
		getSector().rebuildGeometry();
		return true;
	}
}
