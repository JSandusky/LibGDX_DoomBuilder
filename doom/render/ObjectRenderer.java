package com.doom.render;

import com.doom.DoomMapRenderer;

/** psychotic rendering will get outsourced to rendering objects */
public abstract class ObjectRenderer<T> {
	DoomMapRenderer renderer;
	
	public ObjectRenderer(DoomMapRenderer renderer) {
		this.renderer = renderer;
	}
	public abstract void render(T rendering);
	
	public DoomMapRenderer getRenderer() {
		return renderer;
	}
}
