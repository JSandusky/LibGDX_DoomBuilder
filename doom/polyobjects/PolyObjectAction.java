package com.doom.polyobjects;

import com.doom.Sector;
import com.doom.SectorAction;

public abstract class PolyObjectAction extends SectorAction {
	PolyObject polyObj;
	public PolyObjectAction(Sector target) {
		super(target);
		polyObj = (PolyObject)target;
	}

	public PolyObject getPoly() {
		return polyObj;
	}
}
