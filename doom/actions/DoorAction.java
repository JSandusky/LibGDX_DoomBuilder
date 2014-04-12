package com.doom.actions;

import com.badlogic.gdx.math.Interpolation;
import com.doom.Sector;
import com.doom.SectorAction;
import com.doom.SideDef;

/** this is just an open/close door */
public class DoorAction extends SectorAction {
	float stayOpenFor;
	float openTimer;
	float openHeight;
	float openDuration = 1.5f;
	boolean opened;
	boolean closing;
	float openingTimer = 0;
	
	public DoorAction(Sector target) {
		super(target);
		
		for (SideDef sd : target.sides) {
			if (sd.line.doubleSided) {
				openHeight = sd.getOtherSide().sector.ceilingHeight;
				break;
			}
		}
	}

	@Override
	public boolean update(float td) {
		if (!opened) {
			openingTimer += td;
			getSector().setCeilingHeight((int)Interpolation.linear.apply(getSector().floorHeight, openHeight, openingTimer / openDuration));
			if (openingTimer > openDuration) {
				opened = true;
				openingTimer = 0;
			}
		} else if (closing) {
			openingTimer += td;
			getSector().setCeilingHeight((int)Interpolation.linear.apply(openHeight,getSector().floorHeight, openingTimer / openDuration));
			if (openingTimer > openDuration) {
				closing = false;
				return true;
			}
		} else {
			if (stayOpenFor != -1 && openTimer >= stayOpenFor) {
				opened = false;
				closing = true;
			}
		}
		return false;
	}

}
