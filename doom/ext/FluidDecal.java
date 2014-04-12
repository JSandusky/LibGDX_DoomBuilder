package com.doom.ext;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.doom.Sector;

/** Intent for this class is a decal that on floors spreads out and and walls stretches downward 
 * neither in extremes, just starts at something like 30% of it's size and grows, use case is blood */
public class FluidDecal extends Decal {

	public FluidDecal(TextureRegion region, Vector2 position, float rot, Sector sector, boolean floor) {
		super(region, position, rot, sector, floor);
		// TODO Auto-generated constructor stub
	}

}
