package com.doom.actions;

import com.doom.Sector;
import com.doom.SectorAction;

/** pans the surface of a flat */
public class PanFlatAction extends SectorAction {
	float xPanAmount;
	float yPanAmount;
	boolean isFloor;

	public PanFlatAction(Sector target, float xAmount, float yAmount, boolean floor) {
		super(target);
		xPanAmount = xAmount;
		yPanAmount = yAmount;
		isFloor = floor;
	}
	
	@Override
	public boolean update(float td) {
		if (isFloor) {
			getSector().floorTextureOffset.add(xPanAmount * td, yPanAmount * td);
		} else {
			getSector().ceilingTextureOffset.add(xPanAmount * td, yPanAmount * td);
		}
		getSector().rebuildGeometry();
		return false;
	}
	
}
