package com.doom.actions;

import com.doom.Sector;
import com.doom.SectorAction;

/** toggle actions are turned on/off by actions */
public abstract class ToggleAction extends SectorAction {
	boolean active;
	boolean deactivating;
	boolean activating;
	
	public ToggleAction(Sector target) {
		super(target);
	}

	@Override
	public boolean update(float td) {
		if (active) {
			updateActive(td);
		} else if (deactivating) {
			if (updateDeactivate(td)) {
				active = deactivating = false;
			}
		} else if (activating) {
			if (updateActivate(td)) {
				active = true;
				activating = false;
			}
		}
		return false;
	}
	
	public void activate() {
		activating = true;
		active = false;
	}
	
	public void deactivate() {
		deactivating = true;
		activating = false;
		active = false;
	}
	
	public void toggle() {
		if (active)
			deactivate();
		else
			activate();
	}

	public abstract boolean updateActive(float td);
	public abstract boolean updateDeactivate(float td);
	public abstract boolean updateActivate(float td);
}
