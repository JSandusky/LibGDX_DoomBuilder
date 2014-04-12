package com.doom.ext;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Plane;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.doom.Sector;
import com.doom.SideDef;

/** basic decal, it's implementation is instant on/off decals, use case is bullet holes */
public class Decal {
	static Sprite dumbSprite;
	static Plane projectionPlane;
	
	protected transient TextureRegion region;
	protected int sideIndex = -1;
	protected int sectorIndex = -1;
	protected transient SideDef side;
	protected transient Sector sector;
	protected float[] vertices;
	
	float lifeTime = Float.MIN_VALUE; //MIN_VALUE == live forever
	
	public Decal(TextureRegion region, Vector3 position, SideDef side) {
		this.region = region;
		this.side = side;
		sideIndex = side.index;
		
		plane1.set(side.line.a.x, 0, side.line.a.y);
		plane2.set(side.line.b.x, 0, side.line.b.y);
		plane3.set(side.line.b.x, 10, side.line.b.y);
		projectionPlane.set(plane1, plane2, plane3);
		
		
	}
	
	static Vector3 plane1, plane2, plane3; //for building clipping planes
	public Decal(TextureRegion region, Vector2 position, float rotAxis, Sector sector, boolean floor) {
		this.region = region;
		this.sector = sector;
		sectorIndex = sector.index;
		
		if (floor) {
			
		} else {
			
		}
	}
	
	private Decal() {
		
	}
}
