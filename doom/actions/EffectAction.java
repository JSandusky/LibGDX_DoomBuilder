package com.doom.actions;

import com.doom.Sector;
import com.doom.SectorAction;
import com.doom.Thing;

/** An effect action is one that is intended to run on the contents of a sector */
public abstract class EffectAction extends SectorAction {

	public EffectAction(Sector target) {
		super(target);
	}

	public abstract void applyEffect(Thing thing);
}
