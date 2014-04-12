package com.doom.handles;

import java.io.Serializable;

import com.doom.DoomMap;
import com.doom.Sector;

/** reconstructible means to get a sector, for SectorActions mostly */
public class SectorHandle implements Serializable, IHandle {
	private static final long serialVersionUID = 1L;
	
	transient Sector sector;
	int index;
	
	public SectorHandle() {}
	public SectorHandle(int index) {}
	public SectorHandle(Sector sec) {
		sector = sec;
	}
	
	public Sector get() {return sector;}
	public int getIndex() {return index;}
	
	@Override
	public void rebuild(DoomMap map) {
		for (Sector s : map.getSectors()) {
			if (s.index == index) {
				sector = s;
				return;
			}
		}
	}
}
