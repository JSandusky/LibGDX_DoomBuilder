package com.doom.cache;

import java.util.Comparator;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Frustum;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.Array;
import com.doom.Sector;
import com.doom.SideDef;

//TODO all cached/temp-helper values from sectors are to go here
//while this will create class count clutter, it will reduce class clutter
public class SectorCache {
	public static Color[] lightLevels;
	
	static {
		lightLevels = new Color[256];
		for (int i = 0; i <= 255; ++i) {
			lightLevels[i] = new Color(i/255f,i/255f,i/255f,1f);
		}
	}
	
	public Array<SideDef> sectorSortedSideDefs = new Array<SideDef>();
	
	public static Vector3 tmpVec = new Vector3();
	public static Ray tmpRay = new Ray(new Vector3(0,0,0),Vector3.Y);
	public static Array<Sector> _eventStack_ = new Array<Sector>();
	
	public static BoundingBox tmp = new BoundingBox();
	public boolean[] renderPassingSides;
	public Frustum localFrustum = new Frustum(); //parent in render call will set this
	
	public static Comparator<SideDef> sideSorter = new Comparator<SideDef>() {
		@Override
		public int compare(SideDef lhs, SideDef rhs) {
			final boolean lhsOther = lhs.getOtherSide() != null;
			final boolean rhsOther = rhs.getOtherSide() != null;
			if (!lhsOther && !rhsOther) {
				return 0;
			} else if (lhsOther && !rhsOther) {
				return -1;
			} else if (rhsOther && !lhsOther) {
				return 1;
			}
			
			final int lhsIndex = lhs.getOtherSide().sector.index;
			final int rhsIndex = rhs.getOtherSide().sector.index;
			if (lhsIndex == rhsIndex) {
				return 0;
			} else if (lhsIndex < rhsIndex) {
				return -1;
			} else {
				return 1;
			}
		}
	};
}
