package com.doom;

import com.badlogic.gdx.math.Frustum;
import com.badlogic.gdx.math.Vector2;

public interface IDoomDrawable {
	public void draw(DoomMapRenderer batch, Frustum frustum, Vector2 relTo, int depth);
}
