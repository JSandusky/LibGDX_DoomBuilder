package com.doom.actions;

import com.badlogic.gdx.math.Interpolation;
import com.doom.Sector;
import com.doom.SectorAction;

/** adjust the height of a sector using whatever interpolation you want */
public class AlterHeightAction extends SectorAction {
	float duration;
	float time;
	float height;
	float startHeight;
	boolean floor;
	Interpolation interpol;
	
	public AlterHeightAction(Sector target, float time, float height, boolean isFloor, Interpolation interpolate) {
		super(target);
		this.height = height;
		floor = isFloor;
		duration = time;
		interpol = interpolate;
		startHeight = floor ? target.currentFloorHeight : target.currentCeilingHeight;
	}

	@Override
	public boolean update(float td) {
		time += td;
		if (time <= duration) {
			final float oldHeight = floor ? getSector().currentFloorHeight : getSector().currentCeilingHeight;
			int newHeight = (int)interpol.apply(startHeight,height,time/duration);
			if (oldHeight != newHeight) {
				if (floor) {
					getSector().currentFloorHeight = newHeight;
				} else {
					getSector().currentCeilingHeight = newHeight;
				}
				getSector().rebuildGeometry();
			}
			return false;
		}
		return true;
	}
}
