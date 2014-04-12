package com.doom;

import com.badlogic.gdx.graphics.Texture;

public abstract class ResourceResolver {
	public abstract Texture getTexture(String name, TextureRole role);
}
