package com.doom.ext;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.doom.Sector;

/** this decal will use a specialized shader and a 'depth' map to progressively 'worsen', use case is craters */
public class DepressionDecal extends Decal {

	public DepressionDecal(TextureRegion region, Vector2 position,float rotAxis, Sector sector, boolean floor) {
		super(region, position, rotAxis, sector, floor);
		// TODO Auto-generated constructor stub
	}

}
