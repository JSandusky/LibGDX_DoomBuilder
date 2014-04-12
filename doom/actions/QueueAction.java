package com.doom.actions;

import com.doom.Sector;
import com.doom.SectorAction;

public class QueueAction extends SectorAction {
	float time;
	SectorAction action;
	
	public QueueAction(Sector target, SectorAction act, float time) {
		super(target);
		action = act;
		this.time = time;
	}
	
	@Override
	public boolean update(float td) {
		time -= td;
		if (time <= 0) {
			getSector().actions.add(action);
			return true; //caller will remove us
		}
		return false;
	}

	
}
