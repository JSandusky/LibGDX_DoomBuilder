package com.doom.cache;

import com.badlogic.gdx.math.Vector2;

//TODO all temp/caching fields from sidedefs are to go here
//doing so will greating clean-up the complicated parts
//while this will create class count clutter, it will reduce class clutter
public class SideDefCache {
	public static Vector2 tmp = new Vector2();
	public boolean checkForSlopedFloors = false;
	public boolean checkForSlopedCeilings = false;
}
