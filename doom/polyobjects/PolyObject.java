package com.doom.polyobjects;

import com.badlogic.gdx.math.Frustum;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader.Element;
import com.doom.DoomMapRenderer;
import com.doom.ResourceResolver;
import com.doom.Sector;
import com.doom.SideDef;

/** PolyObjects are special types of sectors which are intended to moved around dynamically
 * they are contagious, in that a sector marked as a PolyObject part will be the root 
 * and all connected sectors will also be PolyObjects */
//TODO: this is going to be fun dealing with children
//TODO: I think sectors are going to need to keep their polygons for this to work, that way the child polygons can have an origin that's relative to the parent polyobject
/** In Hexen polyobjects were limited to being singular complete sectors, I'd like to avoid that limitation */
public class PolyObject extends Sector {
	private static final long serialVersionUID = 1L;
	transient Array<Sector> children;

	public PolyObject(Element elem, ResourceResolver resolver, Vector2 offset,
			Vector2 scale, float vScale) {
		super(elem, resolver, offset, scale, vScale);
	}
	
	public void addChild(Sector sector) {
		children.add(sector);
	}

	public PolyObject(int idx) {
		super(idx);
	}
	
	public void setPosition(float x, float y) {
		
	}
	
	public void rotate(float degrees) {
		
	}
	
	protected PolyObject() {
		//serialization
	}
	
	public PolyObject(Sector source) {
		//TODO construct poly object from existing sector
	}
	
	@Override
	public void draw(DoomMapRenderer batch, Frustum frustum, Vector2 relTo, int depth) {
		if (depth > 128) //double classic VisPlane crash
			return;
		if (geometryDirty)
			rebuildGeometry_();
		if (!drawSurfaces(batch))
			return;
		//for polyobjects we draw backfaces
		for (int i = 0; i < sides.length; ++i) {
			final SideDef sd = sides[i]; 
			if (!sd.line.frontSide(relTo))
				sd.draw(batch, relTo);
		}
	}
}
