package com.doom.nav;

import java.io.Serializable;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.doom.DoomMap;
import com.doom.Sector;

//TODO add inputs for stepheight and object height to build path graphs for different things

/** Generates precalculated passability for all sectors of a map
 * the idea is that you precalculate navigation for different classes of things 
 * navigation cannot currently check slopes correctly
 * 
 * It does this by brute force
 * */
public class NavBuilder implements Serializable {
	private static final long serialVersionUID = 1L;
	
	transient float[] dists;
	int[][] navInfo;
	int dims;
	public void build(DoomMap map) {
		final int tableDim = map.getSectors().size;
		navInfo = new int[tableDim][tableDim];
		Array<Sector> holder = new Array<Sector>();
		
		for (int i = 0; i < map.getSectors().size; ++i) {
			for (int j = 0; j < map.getSectors().size; ++j) {
				if (j == i)
					continue;
				
			}
		}
	}
	
	transient Vector2 destCentroid = new Vector2();
	transient Array<Sector> closed = new Array<Sector>();
	transient Array<Sector> open = new Array<Sector>();
	transient Array<Sector> holder = new Array<Sector>();
	transient ObjectMap<Sector,Sector> cameFrom = new ObjectMap<Sector,Sector>();
	
	/** grab the precalculated next sector from the table */
	public Sector getPathPart(Sector start, Sector dest, DoomMap map) {
		if (navInfo[start.index][dest.index] != -1) {
			final int nextIndex = navInfo[start.index][dest.index];
			return map.getSectors().get(nextIndex);
		} return null;
	}
	
	/** grab the full path from the table */
	public void getPath(DoomMap map, Sector start, Sector dest, Array<Sector> holder) {
		Sector next = start;
		do {
			next = getPathPart(next,dest, map);
			if (next != null)
				holder.add(next);
		} while (next != dest && next != null);
	}
	
	Sector getNearest() {
		float lowScore = Float.MAX_VALUE;
		Sector lowSector = null;
		for (int i = 0; i < open.size; ++i) {
			Sector cur = open.get(i);
			float dist = cur.getCentroid().dst2(destCentroid);
			if (dist < lowScore) {
				lowScore = dist;
				lowSector = cur;
			}
		}
		return lowSector;
	}
	
	/** You can use this if you want to find a completely original path */
	public Array<Sector> findPath(Sector from, Sector to) {
		closed.clear();
		open.clear();
		holder.clear();
		cameFrom.clear();
		
		Array<Sector> ret = new Array<Sector>();
		
		//from.getNeighbors(holder, closed);
		//open.addAll(holder);
		open.add(from);
		holder.clear();
		destCentroid.set(to.getCentroid());
		
		while (open.size > 0) {
			Sector current = getNearest();
			if (current == to) {
				reconstructPath(ret,current);
				return ret; //??
			}
			open.removeValue(current, true);
			closed.add(current);
			holder.clear();
			current.getNeighbors(holder, closed);
			for (Sector s : holder) {
				//add sectors that we can move to
				if (!open.contains(s, true)) {// && to.canEnter(from, 15, 65)) {
					cameFrom.put(s, current);
					open.add(s);
				}
			}
			holder.clear();
		}
		
		return ret;
	}
	
	void reconstructPath(Array<Sector> holdingArray, Sector current) {
		if (cameFrom.containsKey(current)) {
			Sector nxt = cameFrom.get(current);
			reconstructPath(holdingArray,nxt);
			holdingArray.insert(0, current); //tracing backwards
		} else {
			holdingArray.add(current); //destination
		}
	}
	
	public void buildPaths(DoomMap map) {
		final int sectorCt = dims = map.getSectors().size;
		dists = new float[sectorCt];
		for (int i = 0; i < dists.length; ++i)
			dists[i] = Float.MAX_VALUE;
		navInfo = new int[sectorCt][sectorCt];
		for (int i = 0; i < sectorCt; ++i) {
			for (int j = 0; j < sectorCt; ++j) {
				navInfo[i][j] = -1;
			}
		}
		
		for (int s = 0; s < sectorCt; ++s) {
			final Sector start = map.getSectors().get(s);
			for (int t = 0; t < sectorCt; ++t) {
				if (t == s)
					continue;
				final Sector dest = map.getSectors().get(t);
				Array<Sector> path = findPath(start, dest);
				if (path.size > 0) {
					if (path.get(0).index == start.index)
						navInfo[s][t] = dest.index;
					else if (path.size > 1)
						navInfo[s][t] = path.get(path.size-2).index;
					else
						navInfo[s][t] = dest.index;
					//for (int part = 0; part < path.size - 1; ++part) {
						//navInfo[path.get(part).index][dest.index] = path.get(part+1).index;
					//}
				}
			}
		}
	}
	
	/** construct nav-builder from another nav-builder */
	public NavBuilder(NavBuilder other) {
		dims = other.dims;
		navInfo = new int[dims][dims];
		for (int i = 0; i < dims; ++i) {
			for (int j = 0; j < dims; ++j) {
				navInfo[i][j] = other.navInfo[i][j];
			}
		}
	}
	
	private NavBuilder() {
		//for serialization
	}
	
	public NavBuilder(DoomMap map) {
		buildPaths(map);
		/* Originally I gave it a shot using the event flood-filler, that was sub-par
		 * 
		 * NavSectorEvent event = new NavSectorEvent(null);
		final int sectorCt = map.getSectors().size;
		
		navInfo = new int[sectorCt][sectorCt];
		
		for (int i = 0; i < map.getSectors().size; ++i) {
			Sector start = map.getSectors().get(i);
			for (int j = 0; j < map.getSectors().size; ++j) {
				if (j == i)
					continue;
				Sector dest = map.getSectors().get(j);
				event.target = dest;
				start.sendEvent(event);
				
				Array<Sector> path = event.getPath();
				if (path.size > 0) {
					for (int k = 0; k < path.size - 1; ++k) {
						navInfo[path.get(k).index][dest.index] = path.get(k).index;
					}
					navInfo[path.get(path.size-1).index][dest.index] = dest.index;
				}
				event.sectorStack.clear();
			}
		}*/
	}
}
