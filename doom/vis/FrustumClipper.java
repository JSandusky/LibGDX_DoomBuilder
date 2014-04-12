package com.doom.vis;

import com.badlogic.gdx.math.Frustum;
import com.badlogic.gdx.math.Plane;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;

/** Utility routines for clipping a frustum to points or bounding boxes */
public final class FrustumClipper {
	public static final int NEAR = 0;
	public static final int FAR = 1;
	public static final int LEFT = 2;
	public static final int RIGHT = 3;
	public static final int TOP = 4;
	public static final int BOTTOM = 5;
	
	
	public static Frustum cpy(Frustum in) {
		Frustum ret = new Frustum();
		set(ret,in);
		return ret;
	}
	
	public static void set(Frustum in, Frustum src) {
		for (int i = 0; i < in.planes.length; ++i) {
			in.planes[i].set(src.planes[i]);
		}
	}
	
	/* 3,2	  7,6
	 * 0,1    4,5
	 * 
	 * 	Near
	 * 	planes[0].set(planePoints[1], planePoints[0], planePoints[2]);
	 * Far
		planes[1].set(planePoints[4], planePoints[5], planePoints[7]);
		Left
		planes[2].set(planePoints[0], planePoints[4], planePoints[3]);
		Right
		planes[3].set(planePoints[5], planePoints[1], planePoints[6]);
		Top
		planes[4].set(planePoints[2], planePoints[3], planePoints[6]);
		Bottom
		planes[5].set(planePoints[4], planePoints[0], planePoints[1]);
	 */
	
	public static void clipLeft(Frustum in, Vector3 point) {
		if (point != null)
			in.planes[LEFT].set(in.planePoints[0],in.planePoints[4],point);
	}
	
	public static void clipRight(Frustum in, Vector3 point) {
		if (point != null)
			in.planes[RIGHT].set(in.planePoints[5],in.planePoints[1], point);
	}
	
	public static void clipTop(Frustum in, Vector3 point) {
		if (point != null)
			in.planes[TOP].set(in.planePoints[2],in.planePoints[3], point);
	}
	
	public static void clipBottom(Frustum in, Vector3 point) {
		if (point != null)
			in.planes[BOTTOM].set(in.planePoints[4],in.planePoints[0],point);
	}
	
	public static void clipBox(Frustum in, BoundingBox box) {
		Vector3[] corners = box.getCorners();
		int closest = -1;
		float dist = Float.MAX_VALUE;
		
		clipLeft(in, getClosest(in.planes[LEFT],corners));
		clipRight(in, getClosest(in.planes[RIGHT],corners));
		clipBottom(in, getClosest(in.planes[BOTTOM],corners));
		clipTop(in, getClosest(in.planes[TOP],corners));
	}
	
	static Vector3 getClosest(Plane plane, Vector3[] pts) {
		float dist = Float.MAX_VALUE;
		Vector3 best = null;
		for (Vector3 pt : pts) {
			float d = plane.distance(pt);
			if (d < dist) {
				dist = d;
				best = pt;
			}
		}
		if (dist < 0)
			return null;
		return best;
	}
}
