package com.doom.actions;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.doom.Sector;

public class VaryingWindAction extends WindAction {
	float minForce, maxForce;
	float intervalMin, intervalMax;
	float time;
	
	//Fixed interval
	public VaryingWindAction(Sector target, Vector2 windDir, float minWindForce, float maxWindForce, float interval) {
		this(target,windDir,minWindForce,maxWindForce,interval,interval);
	}
	
	//Varying interval
	public VaryingWindAction(Sector target, Vector2 windDir, float minWindForce, float maxWindForce, float minInterval, float maxInterval) {
		super(target, windDir, minWindForce);
		this.intervalMin = minInterval;
		this.intervalMax = maxInterval;
		minForce = minWindForce;
		maxForce = maxWindForce;
		time = intervalMin;
	}

	@Override
	public boolean update(float td) {
		time -= td;
		if (time <= 0) {
			if (intervalMin != intervalMax)
				time = MathUtils.random(intervalMin,intervalMax);
			else
				time = intervalMin;
			this.force = MathUtils.random(minForce,maxForce);
		}
		return super.update(td);
	}
}
