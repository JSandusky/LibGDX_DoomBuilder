package com.doom;

import com.badlogic.gdx.graphics.Color;

/** The Universal Doom Map Format fields */
public class UniField {
	UniType type;
	String data;
	Object rawData;
	
	public UniField(int type, String data) {
		this.data = data;
		this.type = UniType.values()[type];
		switch (this.type) {
		case Integer:
		case LinedefType:
		case SectorEffect:
		case AngleDegrees:
		case SectorTag:
		case LinedefTag:
		case ThingType:
		case ThingClass:
			rawData = getInt();
			break;
		case Float:
		case AngleRadians:
			rawData = getFloat();
			break;
		case Boolean:
			rawData = getBool();
			break;
		case Color:
			rawData = getColor();
			break;
		case String:
		case Texture:
		case Flat:
		case EnumStrings:
			break;
		}
	}
	
	private UniField() {}
	
	public int getTypeCode() {return type.ordinal();}
	public UniType getType() {return type;}
	public String getTypeName() {
		return type.toString();
	}
	
	public String getText() {return data;}
	
	public <T> T get() {
		return (T)rawData;
	}
	
	public float getFloat() {
		return Float.parseFloat(data);
	}
	public int getInt() {
		if (rawData != null)
			return (Integer)rawData;
		return Integer.parseInt(data);
	}
	public Color getColor() {
		if (rawData != null)
			return (Color)rawData;
		return Color.valueOf(data + "FF");
	}
	public boolean getBool() {
		if (rawData != null)
			return (Boolean)rawData;
		return Boolean.parseBoolean(data);
	}
	public LineDef getLine(DoomMap map) {
		return map.lines.get(getInt());
	}
	public SideDef getSide(DoomMap map) {
		return map.sides.get(getInt());
	}
	public Sector getSector(DoomMap map) {
		return map.sectorTags.get(getInt());
	}
}
