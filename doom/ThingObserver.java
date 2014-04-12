package com.doom;

import com.badlogic.gdx.utils.Array;

public abstract class ThingObserver {
	public abstract void onAdded(Thing thing);
	public abstract void onRemoved(Thing thing);
	
	public static abstract class Selective extends ThingObserver {
		Array<Thing> contents = new Array<Thing>();
		public abstract boolean select(Thing thing);
		
		public void onAdded(Thing thing) {
			if (select(thing))
				contents.add(thing);
		}
		public void onRemoved(Thing thing) {
			if (select(thing))
				contents.removeValue(thing,true);
		}
	}
}
