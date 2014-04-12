package com.doom.util;

import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Plane;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.doom.ActionCodes;
import com.doom.LineDef;
import com.doom.Sector;
import com.doom.SideDef;

public final class Slopes {
	
	public static LineDef findSlopeLine(Sector sector, boolean forCeiling) {
		final int argIndex = forCeiling ? 1 : 0;
		for (SideDef side : sector.sides) {
			if (side.line.actionCode == ActionCodes.PLANE_ALIGN && side.line.ActionArgs != null && side.line.ActionArgs.length > 1) {
				final int arg = side.line.ActionArgs[argIndex];
				if (side.isFront && arg == 1) { //Plane_Align front side
					return side.line;
				} else if (!side.isFront && arg == 2) { //Plane_Align back side
					return side.line;
				}
			}
		}
		return null;
	}
	
	public static Plane constructPlane(Sector sector, LineDef line, boolean ceiling) {
		final float sectorHeight = ceiling ? sector.currentCeilingHeight : sector.currentFloorHeight;
		final Sector otherSector = line.front.sector != sector ? line.front.sector : line.back.sector;
		if (otherSector == null)
			return null;
		final float otherSectorHeight = ceiling ? otherSector.currentCeilingHeight : otherSector.currentFloorHeight;
		
		final Vector2 midPoint = line.a.cpy().lerp(line.b, 0.5f);
		Vector2 farthestPoint = midPoint.cpy();
		float farthestDist = 0f;
		for (SideDef side : sector.sides) {
			if (side.line != line) {
				float dst = Intersector.distanceLinePoint(line.a, line.b, side.line.a);
				if (dst > farthestDist) {
					farthestDist = dst;
					farthestPoint.set(side.line.a);
				}
				dst = Intersector.distanceLinePoint(line.a, line.b, side.line.b);
				if (dst > farthestDist) {
					farthestDist = dst;
					farthestPoint.set(side.line.b);
				}
			}
		}
		
		final Vector3 mid3d = new Vector3(line.a.x,otherSectorHeight,line.a.y);
		final Vector3 rightEdge = new Vector3(line.b.x,otherSectorHeight,line.b.y);
		final Vector3 far3d = new Vector3(farthestPoint.x, sectorHeight, farthestPoint.y);
		
		return new Plane(mid3d,far3d,rightEdge);
	}
}
