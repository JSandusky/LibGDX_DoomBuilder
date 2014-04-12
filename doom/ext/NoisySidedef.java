package com.doom.ext;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import com.badlogic.gdx.utils.XmlReader.Element;
import com.doom.ResourceResolver;
import com.doom.SideDef;

/** a sidedef that is made of a few parts that get varying offsets along the cross normal
 * only intended use is to generate random offsets (seeded by world position) to create caves
 * */
public class NoisySidedef extends SideDef {

	public NoisySidedef(Element elem, ResourceResolver reso) {
		super(elem, reso);
		throw new NotImplementedException(); //seriously, it's not done yet
	}

}
