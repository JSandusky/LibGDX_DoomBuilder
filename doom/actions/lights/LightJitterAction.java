package com.doom.actions.lights;

import com.badlogic.gdx.math.MathUtils;
import com.doom.Sector;
import com.doom.SectorAction;

/** light value is completely random (within specific ranges) */
public class LightJitterAction extends SectorAction {
	float minRand, maxRand;
	float cycle;
	float time;
	int minValue = 0;
	int maxValue = 255;
	
	public LightJitterAction(Sector target, float minTime, float maxTime) {
		super(target);
		minRand = minTime;
		maxRand = maxTime;
	}
	
	public LightJitterAction(Sector target, float minTime, float maxTime, int minVal, int maxVal) {
		super(target);
		minRand = minTime;
		maxRand = maxTime;
		minValue = minVal;
		maxValue = maxVal;
	}

	@Override
	public boolean update(float td) {
		time += td;
		if (time > cycle) {
			time = 0;
			getSector().lighting = MathUtils.random(minValue, maxValue);
			getSector().rebuildGeometry();
			cycle = MathUtils.random(minRand,maxRand);
		}
		return false;
	}

}
