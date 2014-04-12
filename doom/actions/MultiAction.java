package com.doom.actions;

import com.doom.Sector;
import com.doom.SectorAction;

/** execute 2 actions at the same time, the only reason to ever do this rather than just adding the actions to the sector is if you want both to go away with removing the action */
public class MultiAction extends SectorAction {
	SectorAction a, b;
	boolean aDone, bDone;
	public MultiAction(Sector target, SectorAction a, SectorAction b) {
		super(target);
		this.a = a;
		this.b = b;
	}

	@Override
	public boolean update(float td) {
		if (!aDone) {
			if (a.update(td))
				aDone = true;
		}
		if (!bDone) {
			if (b.update(td))
				bDone = true;
		}
		return aDone && bDone;
	}

}
