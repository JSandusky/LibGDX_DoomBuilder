package com.doom.actions;

import com.doom.Sector;
import com.doom.SectorAction;

/** execute 2 actions in sequence */
public class SequenceAction extends SectorAction {
	SectorAction first,second;
	boolean firstDone;
	public SequenceAction(Sector target, SectorAction first, SectorAction second) {
		super(target);
		this.first = first;
		this.second = second;
	}
	
	@Override
	public boolean update(float td) {
		if (!firstDone) {
			if (first.update(td))
				firstDone = true;
		} else {
			return second.update(td);
		}
		return false;
	}

}
