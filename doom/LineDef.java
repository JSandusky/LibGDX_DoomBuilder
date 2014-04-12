package com.doom;

import java.io.Serializable;

import com.badlogic.gdx.math.Frustum;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.XmlReader;

/**via: http://doom.wikia.com/wiki/Linedef
 * Linedefs are what make up the 'shape' (for lack of a better word) of a map. Every linedef is between two vertices and contains one or two sidedefs (which contain wall texture data). There are two major purposes of linedefs. The first is to divide the map into sectors, and the second is to trigger action specials.
 * 
 * Any area of a map directly behind a one-sided linedef is void space (which cannot be occupied except by using the idclip cheat code). A two-sided linedef is needed (to separate the two sectors) any time there is a change in (1) the height or texture of the floor or ceiling, (2) the light level, or (3) the sector tag or type.
*/
@SuppressWarnings("serial")
public class LineDef implements IDoomDrawable, Serializable, PostDeserialize {
	public int index;
	public Vector2 a, b;
	public boolean facing;
	public float floorZOffset = 0; //for doing slopes and cool shit
	public float ceilingZOffset = 0; //for doing slopes and cool shit
	public int[] ActionArgs; //may be null
	
	int frontIndex = -1;
	int backIndex = -1;
	
	//rebuild/postSerialize will set these
	public transient SideDef front;
	public transient SideDef back;
	
	public ObjectMap<String,UniField> fields = new ObjectMap<String,UniField>();
	public ObjectMap<String,Boolean> flags = new ObjectMap<String,Boolean>();
	public boolean impassable, blockMonster, doubleSided, upperUnpegged, lowerUnpegged, secret, blockSound, hidden, shown;
	public int actionCode;
	public int tag;
	
	public LineDef(XmlReader.Element elem, Vector2 offset, Vector2 scale, float vScale) {
		index = elem.getInt("idx");
		actionCode = elem.getInt("action");
		tag = elem.getInt("tag");
		
		String[] actionArgs = elem.getAttribute("action-args","").split(",");
		if (actionArgs != null) {
			ActionArgs = new int[actionArgs.length];
			for (int i = 0; i < actionArgs.length; ++i) {
				if (actionArgs[i].length() > 0)
					ActionArgs[i] = Integer.parseInt(actionArgs[i]);
				else
					ActionArgs[i] = 0;
			}
		}
		
		int vertCt = 0;
		for (int i = 0; i < elem.getChildCount(); ++i) {
			XmlReader.Element e = elem.getChild(i);
			if (e.getName().equalsIgnoreCase("vertex")) {
				if (vertCt == 0) {
					a = new Vector2(e.getFloat("x"),e.getFloat("y"));
					a.scl(scale);
					a.add(offset);
				} else {
					b = new Vector2(e.getFloat("x"),e.getFloat("y"));
					b.add(offset);
					b.scl(scale);
				}
				++vertCt;
			}
		}
		
		a.y *= -1;
		b.y *= -1;
		
		XmlReader.Element flags = elem.getChildByName("flags");
		DoomMap.readFlags(flags, this.flags);
		
		XmlReader.Element fields = elem.getChildByName("fields");
		DoomMap.readFields(fields, this.fields);
	}
	
	LineDef(int id) {
		index = id;
	}
	
	@SuppressWarnings("unused")
	private LineDef() {
		//serialization
	}
	
	public boolean isFlagged(String flag) {
		return flags.get(flag,false);
	}
	
	public void setFlag(String flag, boolean val) {
		flags.put(flag, val);
	}
	
	public Vector2 getMidpoint() {
		return a.cpy().lerp(b,0.5f);
	}
	
	public void calculateBounds(BoundingBox out) {
		out.min.x = Math.min(a.x, b.x);
		out.min.z = Math.min(a.y, b.y);
		out.min.y = Math.min(front.sector.floorHeight, back.sector.floorHeight);
		
		out.max.x = Math.max(a.x, b.x);
		out.max.z = Math.max(a.y, b.y);
		out.max.y = Math.max(front.sector.ceilingHeight, back.sector.ceilingHeight);
		out.set(out.min,out.max);
	}

	@Override
	public void draw(DoomMapRenderer batch, Frustum frustum, Vector2 relTo, int depth) {
		//?? will anything ever be done here?
	}
	
	static Vector2 tmp = new Vector2();
	public boolean intersects(Vector2 start, Vector2 end) {
		if (Intersector.intersectLines(a, b, start, end, tmp))
			return true;
		return false;
	}
	public boolean intersects(Vector2 start, Vector2 end, Vector2 holder) {
		if (Intersector.intersectLines(a, b, start, end, holder))
			return true;
		return false;
	}
	
	public void rebuild(IntMap<SideDef> sides) {
		if (front != null) {
			front = sides.get(front.index);
			frontIndex = front.index;
			front.line = this;
		}
		if (back != null) {
			back = sides.get(back.index);
			backIndex = back.index;
			back.line = this;
		}
	}
	
	public void postDeserialize(DoomMap map, ResourceResolver reso) {
		if (frontIndex != -1) {
			front = map.sides.get(frontIndex);
			/*for (SideDef side : map.sides.values()) {
				if (side.index == frontIndex) {
					front = side;
					break;
				}
			}*/
		}
		if (backIndex != -1) {
			back = map.sides.get(backIndex);
			/*for (SideDef side : map.sides.values()) {
				if (side.index == backIndex) {
					back = side;
					break;
				}
			}*/
		}
	}
	
	float sideOfLine(Vector2 p) {
		Vector2 v1 = a;
		Vector2 v2 = b;
		
		return (p.y - v1.y) * (v2.x - v1.x) - (p.x - v1.x) * (v2.y - v1.y);
	}
	
	public boolean frontSide(Vector2 p) {
		return sideOfLine(p) > 0;
	}
	
	public boolean backSide(Vector2 p) {
		return sideOfLine(p) < 0;
	}
	
	public static LineDef getRightMost(Array<LineDef> lines) {
		Vector2 cur = new Vector2(Float.MIN_VALUE, 0);
		LineDef curBest = null;
		for (LineDef l : lines) {
			if (l.a.x > cur.x) {
				cur.x = l.a.x;
				curBest = l;
			} 
			if (l.b.x > cur.x) {
				cur.x = l.b.x;
				curBest = l;
			}
		}
		return curBest; //???
	}
	
	public static LineDef getNext(LineDef current, Array<LineDef> lines) {
		for (LineDef l : lines) {
			if (l.a.equals(current.b))
				return l;
		}
		return null;
	}
	
	public boolean canCross(Thing thing, int stepHeight, int objectHeight) {
		if (!doubleSided)
			return false;
		Sector from = null, to = null;
		if (frontSide(thing.position)) {
			from = front.sector;
			to = back.sector;
		} else {
			from = back.sector;
			to = front.sector;
		}
		if (to.canEnter(from, stepHeight, objectHeight))
			return true;
		return false;
	}
}
