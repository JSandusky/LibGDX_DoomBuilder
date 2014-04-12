package com.doom;

import java.io.Serializable;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.XmlReader;

@SuppressWarnings("serial")
public class Thing implements Serializable, PostDeserialize {
	public int index;
	public Vector2 position;
	public float z;
	public int tag;
	public int type;
	public int action;
	public float facing;
	public int doomFacing;
	public ObjectMap<String,Boolean> flags = new ObjectMap<String,Boolean>();
	public ObjectMap<String,UniField> fields = new ObjectMap<String,UniField>();
	
	//rebuild/postSerialize will deal with this
	public transient Sector parentSector;
	
	public Thing(XmlReader.Element elem) {
		index = elem.getInt("idx");
		tag = elem.getInt("tag",0);
		type = elem.getInt("type",0);
		action = elem.getInt("action",0);
		
		XmlReader.Element pos = elem.getChildByName("pos");
		position = new Vector2(pos.getFloat("x",0f), pos.getFloat("y",0f));
		facing = pos.getFloat("angle-float");
		doomFacing = pos.getInt("angle-int");
		z = pos.getFloat("z",0f);
		
		XmlReader.Element flags = elem.getChildByName("flags");
		DoomMap.readFlags(flags, this.flags);
		
		XmlReader.Element fields = elem.getChildByName("fields");
		DoomMap.readFields(fields, this.fields);
	}
	
	public boolean inFOV(Vector2 pt, float fovRange) {
		return false;
	}
	
	@SuppressWarnings("unused")
	private Thing() {
		//serializable
	}
	
	public void initSector(Array<Sector> sectors) {
		for (Sector s : sectors) {
			if (s.contains(position)) {
				parentSector = s;
				return;
			}
		}
	}
	
	public boolean updatePosition() {
		Sector s = checkSectorChange();
		if (s != null) {
			parentSector = s;
			return true;
		}
		return false;
	}
	
	public Sector checkSectorChange() {
		if (!parentSector.contains(position)) {
			for (SideDef ld : parentSector.sides) {
				if (ld.getOtherSide() != null) {
					if (ld.getOtherSide().sector.contains(position)) {
						return ld.getOtherSide().sector;
					}
				}
			}
			return null; //??couldn't find a new parent sector
		} else {
			return parentSector;
		}
	}
	
	Thing(int id) {
		index = id;
	}

	@Override
	public void postDeserialize(DoomMap map, ResourceResolver res) {
		initSector(map.getSectors());
	}
}
