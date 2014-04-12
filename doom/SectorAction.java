package com.doom;

/** Something that operates on a specific sector 
 * used for recreating the classic actions such as crushers/lifts/doors
 * 
 * refer to actions package for examples
 * */
//TODO: these are going to become serializable - yeap, that'll be fun
public abstract class SectorAction {
	Sector workingOn;
	
	public SectorAction(Sector target) {
		workingOn = target;
	}
	
	public Sector getSector() {return workingOn;}
	
	public abstract boolean update(float td); //return true to remove
}
