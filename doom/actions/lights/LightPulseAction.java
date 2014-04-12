package com.doom.actions.lights;

import com.badlogic.gdx.math.Interpolation;
import com.doom.Sector;
import com.doom.SectorAction;

/** interpolates between values */
public class LightPulseAction extends SectorAction {
	int normalLight;
	int blinkLight = 0;
	float phaseOn;
	float phaseOff;
	boolean on;
	float time;
	Interpolation interpol;
	
	public LightPulseAction(Sector target, int blinkValue, float cycleTime, Interpolation interpolate) {
		super(target);
		blinkLight = blinkValue;
		on = true;
		phaseOn = phaseOff = cycleTime;
		interpol = interpolate;
		normalLight = target.lighting;
	}
	
	public LightPulseAction(Sector target, int blinkValue, float cycleOnTime, float cycleOffTime, Interpolation interpolate) {
		super(target);
		blinkLight = blinkValue;
		on = true;
		phaseOn = cycleOnTime;
		phaseOff = cycleOffTime;
		interpol = interpolate;
		normalLight = target.lighting;
	}

	@Override
	public boolean update(float td) {
		time += td;
		if (on) {
			if (time > phaseOn) {
				on = !on;
				time = 0;
			}
		} else {
			if (time > phaseOff) {
				on = !on;
				time = 0;
			}
		}
		final int lastValue = getSector().lighting;
		int newValue = 0;
		if (on)
			newValue = (int)interpol.apply(blinkLight,normalLight,time/phaseOn);
		else
			newValue = (int)interpol.apply(normalLight,blinkLight,time/phaseOff);
		if (lastValue != newValue) //very slow phases may require no change
			getSector().rebuildGeometry();
		return false;
	}
}
