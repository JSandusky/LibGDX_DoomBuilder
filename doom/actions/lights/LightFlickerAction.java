package com.doom.actions.lights;

import com.badlogic.gdx.math.MathUtils;
import com.doom.Sector;
import com.doom.SectorAction;

/** like LightBlinkAction - except with random intervals */
public class LightFlickerAction extends SectorAction {
	int normalLight;
	int blinkLight = 0;
	float phase;
	boolean on;
	float time;
	
	float minRand, maxRand;
	
	public LightFlickerAction(Sector target, int blinkValue, float minRand, float maxRand) {
		super(target);
		blinkLight = blinkValue;
		this.minRand = minRand;
		this.maxRand = maxRand;
		on = true;
		normalLight = target.lighting;
	}

	@Override
	public boolean update(float td) {
		time += td;
		if (time > phase) {
			phase = MathUtils.random(minRand, maxRand);
			if (on) {
				on = false;
				getSector().lighting = blinkLight;
			} else {
				on = true;
				getSector().lighting = normalLight;
			}
			getSector().rebuildGeometry();
			time = 0;
		}
		return false;
	}

}
