package com.doom.actions;

import com.badlogic.gdx.math.Interpolation;
import com.doom.Sector;
import com.doom.SectorAction;
import com.doom.SideDef;

/** elevators, transitions floor height between the values of it's two neighboring sectors
 * 
 * more than 2 sectors? have fun
 *  */
public class LiftAction extends SectorAction {
	final float naturalFloor;
	float otherFloor;
	
	float time;
	final float duration;
	
	public LiftAction(Sector target, float runtime) {
		super(target);
		
		duration = runtime;
		
		naturalFloor = target.currentFloorHeight;
		for (SideDef side : target.sides) {
			if (side.line.doubleSided) {
				if (side.getOtherSide().sector.floorHeight != naturalFloor) {
					otherFloor = side.getOtherSide().sector.currentFloorHeight;
					break;
				}
			}
		}
	}
	
	@Override
	public boolean update(float td) {
		time += td;
		if (time > duration) {
			getSector().setFloorHeight(otherFloor);
			return true;
		} else {
			getSector().setFloorHeight((int)Interpolation.linear.apply(naturalFloor,otherFloor,time/duration));
		}
		return false;
	}
	
	
}
