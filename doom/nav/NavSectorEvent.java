package com.doom.nav;

import java.util.Stack;

import com.badlogic.gdx.utils.Array;
import com.doom.Sector;
import com.doom.SectorEvent;
import com.doom.SideDef;

/** Was once used for building navigation info */
@Deprecated
public class NavSectorEvent extends SectorEvent {
	Sector target;
	public NavSectorEvent(Sector target) {
		this.target = target;
	}
	Stack<Sector> sectorStack = new Stack<Sector>();
	
	@Override
	public boolean propagateThrough(SideDef side) {
		return false;
	}

	@Override
	public boolean doEvent(Sector sector) {
		sectorStack.add(sector);
		if (sector == target) {
			return true;
		}
		return false;
	}
	
	public Array<Sector> getPath() {
		Array<Sector> ret = new Array<Sector>();
		while (!sectorStack.empty())
			ret.insert(0, sectorStack.pop());
		return ret;
	}

	@Override
	public void popped() {
		sectorStack.pop();
	}
}
