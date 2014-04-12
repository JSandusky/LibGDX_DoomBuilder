package com.doom.vis;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

/** Utilities for facing and FOV tests */
public final class Facing {
	//indexes into dir table
	public static final int EAST = 0;
	public static final int SOUTH = 3;
	public static final int SOUTH_EAST = 1;
	public static final int NORTH_EAST = 45;
	public static final int NORTH = 7;
	public static final int NORTH_WEST = 6;
	public static final int WEST = 5;
	public static final int SOUTH_WEST = 4;
	
	public static final Vector2[] DIR = {
		new Vector2(1,0).nor(),//east
		new Vector2(1,-1).nor(),//se
		new Vector2(0,-1).nor(),//s
		new Vector2(-1,-1).nor(),//sw
		new Vector2(-1,0).nor(),//w
		new Vector2(-1,1).nor(),//nw
		new Vector2(0,1).nor(),//n
		new Vector2(1,1).nor()//ne
	};
	
	//Precalculated field of view values to use for dot-prod compares
	public static final float NormalFOV = (float)Math.cos(Math.PI/4); //45 degree half for 90
	public static final float WideFOV = (float)Math.cos(Math.PI/3);
	public static final float SuperWideFOV = (float)Math.cos(Math.PI/2);
	public static final float ExtremeFOV = (float)Math.cos(Math.PI/1.5);
	public static final float GodFOV = (float)Math.cos(Math.PI);
	public static final float NarrowFOV = (float)Math.cos(Math.PI/5);
	public static final float SniperFOV = (float)Math.cos(Math.PI/6);
	
	public static float convertFOV(String name) {
		if (name.equalsIgnoreCase("normal"))
			return NormalFOV;
		else if (name.equalsIgnoreCase("wide"))
			return WideFOV;
		else if (name.equalsIgnoreCase("superwide"))
			return SuperWideFOV;
		else if (name.equalsIgnoreCase("extreme"))
			return ExtremeFOV;
		else if (name.equalsIgnoreCase("god"))
			return GodFOV;
		else if (name.equalsIgnoreCase("narrow"))
			return NarrowFOV;
		else if (name.equalsIgnoreCase("sniper"))
			return SniperFOV;
		return NormalFOV;
	}
	
	static final Vector2 rotVec = new Vector2(1,0).nor(); //it's at 0 degrees
	public static Vector2 getFacingVector(float angle) {
		return rotVec.set(1,0).nor().rotate(angle);
	}
	
	/** Does both a FOV view check and distance check */
	public static boolean inFOV(Vector2 targetPt, Vector2 viewPt, float viewAngleDeg, float coneWidth, float dist) {
		if (targetPt.dst(viewPt) > dist)
			return false;
		return inFOV(targetPt,viewPt,viewAngleDeg,coneWidth);
	}
	static final Vector2 cacheDir = new Vector2();
	
	/** Test if a target point is within the specified cone angle of our view */
	public static boolean inFOV(Vector2 targetPt, Vector2 viewPt, float viewAngleDeg, float coneWidth) {
		cacheDir.set(targetPt).sub(viewPt).nor();
		final float ang = getFacingVector(viewAngleDeg).dot(cacheDir);
		return ang > coneWidth;
	}
	
	public static float canonizeRadian(float angle) {
		if (angle > MathUtils.PI) {
			do {
				angle -= MathUtils.PI2;
			} while (angle > MathUtils.PI);
		} else if (angle < -MathUtils.PI) {
			do {
				angle += MathUtils.PI2;
			} while (angle < MathUtils.PI);
		}
		return angle;
	}
	
	static float clipDegrees(float in) {
		while (in > 360)
			in -= 360;
		while (in < 0)
			in += 360;
		return in;
	}
}
