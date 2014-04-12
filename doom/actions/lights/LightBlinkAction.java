package com.doom.actions.lights;

import com.doom.Sector;
import com.doom.SectorAction;

public class LightBlinkAction extends SectorAction {
	int normalLight;
	int blinkLight = 0;
	float timeOn;
	float timeOff;
	boolean on;
	float time;
	
	public LightBlinkAction(Sector target, float timeOn, float timeOff) {
		super(target);
		this.timeOn = timeOn;
		this.timeOff = timeOff;
		normalLight = target.lighting;//target.lighting;
		blinkLight = 0;
		on = true;
	}
	
	@Override
	public boolean update(float td) {
		time += td;
		if (on) {
			if (time > timeOn) {
				on = false;
				time = 0;
				getSector().lighting = blinkLight;
				getSector().rebuildGeometry();
			}
		} else {
			if (time > timeOff) {
				on = true;
				time = 0;
				getSector().lighting = normalLight;
				getSector().rebuildGeometry();
			}
		}
		return false;
	}
}
