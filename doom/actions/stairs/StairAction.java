package com.doom.actions.stairs;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.utils.Array;
import com.doom.Sector;
import com.doom.SectorAction;
import com.doom.SideDef;
import com.doom.actions.ToggleAction;

public class StairAction extends ToggleAction {
	float cycleTime; //reference
	float time; //current
	float delay; //current
	float delayTime; //reference
	
	float targetHeight;
	Array<Sector> neighbors = new Array<Sector>();
	float tallestNonstairNeighbor;
	
	public StairAction(Sector target, float cycle) {
		super(target);
		cycleTime = cycle;
		
		//???final int neighborCt = getNeighboringStairCount();
		
		for (SideDef side : target.sides) {
			if (side.line.doubleSided) {
				boolean wasStair = false;
				for (SectorAction act : side.getOtherSide().sector.actions) {
					if (act instanceof StairAction) {
						neighbors.add(side.getOtherSide().sector);
						wasStair = true;
					}
				}
				if (!wasStair)
					tallestNonstairNeighbor = Math.max(tallestNonstairNeighbor, side.getOtherSide().sector.floorHeight);
			}
		}
		
		
	}
	
	static StairAction getStairAction(Sector sector) {
		for (SectorAction act : sector.actions) { 
			if (act instanceof StairAction)
				return (StairAction)act;
		} return null;
	}
	
	int getNeighboringStairCount() {
		return neighbors.size;
	}

	@Override
	public void activate() {
		super.activate();
		delay = delayTime;
		time = 0;
	}
	
	@Override
	public void deactivate() {
		time = 0;
		delay = delayTime;
		super.deactivate();
	}
	
	//while active the stairs go up
	@Override
	public boolean updateActive(float td) {
		if (delay > 0) {
			delay -= td;
			return false;
		}
		time += td;
		if (time < cycleTime) {
			getSector().setFloorHeight((int)Interpolation.linear.apply(getSector().floorHeight,targetHeight,time/cycleTime));
		} else {
			getSector().setFloorHeight(targetHeight);
			return true;
		}
		return false;
	}

	//while inactive the stairs go down
	@Override
	public boolean updateDeactivate(float td) {
		if (delay > 0) {
			delay -= td;
			return false;
		}
		time += td;
		if (time < cycleTime) {
			getSector().setFloorHeight((int)Interpolation.linear.apply(targetHeight,getSector().floorHeight,time/cycleTime));
		} else {
			getSector().setFloorHeight(getSector().floorHeight);
			return true;
		}
		return false;
	}

	@Override
	public boolean updateActivate(float td) {
		return false; //do nothing
	}
	
	/** build a list of stairs starting at a particular point */
	static Array<StairAction> listStairs(Sector start) {
		Array<StairAction> stack = new Array<StairAction>();
		StairAction act = StairAction.getStairAction(start);
		if (act != null) {
			Sector current = start;
			stack.add(act);
			
			do {
				for (SideDef side : current.sides) {
					if (side.line.doubleSided) {
						StairAction nxt = StairAction.getStairAction(side.getOtherSide().sector);
						if (nxt != null && !stack.contains(nxt, true)) {
							stack.add(nxt);
							current = nxt.getSector();
							break;
						}
					}
				}
			} while (stack.get(stack.size-1).getNeighboringStairCount() > 1);
		}
		return stack;
	}
	
	static float getHighestNeighbor(Array<StairAction> act) {
		float ret = 0;
		for (StairAction a : act) {
			ret = Math.max(ret, a.tallestNonstairNeighbor);
		}
		return ret;
	}
	
	/** construct a sequence of stairs from the target point, they will all reach their tops at the same time */
	public static void buildUpStairs(Sector start, float cycleTime) {
		Array<StairAction> actions = listStairs(start);
		if (actions.size > 1) {
			final float topHeight = getHighestNeighbor(actions);
			for (int i = 0; i < actions.size; ++i) {
				StairAction cur = actions.get(i);
				cur.cycleTime = cycleTime;
				cur.targetHeight = (int)(topHeight/((i+1)/((float)actions.size)));
				cur.delay = 0;
			}
			for (StairAction act : actions)
				act.activate();
		}
	}
	
	/** tear down a sequence of stairs from the target point, they will all reach their bottoms at the same time */
	public static void buildDownStairs(Sector start, float cycleTime) {
		Array<StairAction> actions = listStairs(start);
		if (actions.size > 1) {
			final float topHeight = getHighestNeighbor(actions);
			for (int i = 0; i < actions.size; ++i) {
				StairAction cur = actions.get(i);
				cur.cycleTime = cycleTime;
				cur.targetHeight = (int)(topHeight/((i+1)/((float)actions.size)));
				cur.delay = 0;
			}
			for (StairAction act : actions)
				act.deactivate();
		}
	}
	
	/** construct a sequence of stairs that have a delay which will incrementally increase for each stair */
	public static void buildUpStairs_Sequence(Sector start, float cycleTime, float increment) {
		float curInc = 0;
		Array<StairAction> actions = listStairs(start);
		if (actions.size > 1) {
			final float topHeight = getHighestNeighbor(actions);
			for (int i = 0; i < actions.size; ++i) {
				StairAction cur = actions.get(i);
				cur.cycleTime = cycleTime;
				cur.targetHeight = (int)(topHeight/((i+1)/((float)actions.size)));
				cur.delay = curInc;
				curInc += increment;
			}
			for (StairAction act : actions)
				act.activate();
		}
	}
	
	/** tear down a sequence of stairs that have a delay which will incrementally increase for each stair */
	public static void buildDownStairs_Sequence(Sector start, float cycleTime, float increment) {
		float curInc = 0;
		Array<StairAction> actions = listStairs(start);
		if (actions.size > 1) {
			final float topHeight = getHighestNeighbor(actions);
			for (int i = 0; i < actions.size; ++i) {
				StairAction cur = actions.get(i);
				cur.cycleTime = cycleTime;
				cur.targetHeight = (int)(topHeight/((i+1)/((float)actions.size)));
				cur.delay = curInc;
				curInc += increment;
			}
			for (StairAction act : actions)
				act.deactivate();
		}
	}
}
