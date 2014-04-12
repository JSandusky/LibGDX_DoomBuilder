package com.doom.actions;

import com.doom.DoomMap;
import com.doom.Sector;
import com.doom.SectorAction;
import com.doom.SectorActionBuilder;
import com.doom.actions.lights.LightBlinkAction;
import com.doom.actions.stairs.StairAction;

public class ClassicSectorActions {
	public static void fill(DoomMap map) {
		map.registerSectorActionBuilder(65, new SectorActionBuilder() {
			@Override
			public SectorAction createAction(Sector forSector) {
				return new LightBlinkAction(forSector,1,1);
			}
		});
		
		map.registerSectorActionBuilder(26, new SectorActionBuilder() {
			@Override
			public SectorAction createAction(Sector forSector) {
				return new StairAction(forSector,0);
			}		
		});
		
		map.registerSectorActionBuilder(27, new SectorActionBuilder() {
			@Override
			public SectorAction createAction(Sector forSector) {
				return new StairAction(forSector,0);
			}		
		});
		
		
	}
}
