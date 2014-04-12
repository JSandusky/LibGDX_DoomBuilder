package com.doom;

import com.badlogic.gdx.utils.XmlReader;

public abstract class ThingBuilder {
	public abstract Thing createThing(int id, int tag, XmlReader.Element elem);
}
