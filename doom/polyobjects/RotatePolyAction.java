package com.doom.polyobjects;

import com.doom.Sector;

public class RotatePolyAction extends PolyObjectAction {

	public RotatePolyAction(Sector target) {
		super(target);
	}

	@Override
	public boolean update(float td) {
		return false;
	}

}
