package com.doom;

/** SectorEvents get flood-filled through the game map, use them for triggering sound activations and such */
public abstract class SectorEvent {
	public abstract boolean propagateThrough(SideDef side);
	/** return true for the event to be swallowed */
	public abstract boolean doEvent(Sector sector);
	/** popped is called whenever the event tree cycles back up */
	public void popped() { }
}
