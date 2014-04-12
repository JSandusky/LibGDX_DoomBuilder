package com.doom.util;

import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.doom.Sector;
import com.doom.SideDef;

public final class Clipping {
	public static void clip2d(Sector sector, Vector2 in) {
		if (!sector.getPolygon().contains(in.x, in.y)) {
			float closest = Float.MAX_VALUE;
			SideDef closestSide = null;
			for (SideDef side : sector.sides) {
				final float dist = Intersector.distanceLinePoint(side.line.a, side.line.b, in);
				if (dist < closest) {
					closest = dist;
					closestSide = side;
				}
			}
			if (closestSide != null) {
				if (closestSide.line.doubleSided) {
					if (!closestSide.getOtherSide().sector.canEnter(sector, 32, 64)) {
						Intersector.nearestSegmentPoint(closestSide.line.a, closestSide.line.b, in, in);
						return;
					}
				} else if (closestSide.line.impassable){
					Intersector.nearestSegmentPoint(closestSide.line.a, closestSide.line.b, in, in);
					return;
				}
			} //err? is an else possible?
		}
	}
	
	public static void clip3d(Sector sector, Vector3 in) {
		Vector2 flatPos = new Vector2(in.x, in.z);
		clip2d(sector,flatPos);
		if (flatPos.x != in.x || flatPos.y != in.z) { //changed sector??
			//????
		}
		in.y = Math.min(in.y, sector.getCeilingHeight(flatPos));
		in.y = Math.max(in.y, sector.getFloorHeight(flatPos));
		in.x = flatPos.x;
		in.z = flatPos.y;
	}
	
	public static float clipVertical(Sector sector, float in) {
		in = Math.min(sector.currentCeilingHeight, in);
		in = Math.max(sector.currentFloorHeight, in);
		return in;
	}
	
	static Vector2 tmpVec2 = new Vector2();
	public static float clipVertical(Sector sector, Vector3 in) {
		tmpVec2.set(in.x,in.z);
		float ret = Math.max(in.y, sector.getFloorHeight(tmpVec2));
		ret = Math.min(in.y, sector.getCeilingHeight(tmpVec2));
		return ret;
	}
}
