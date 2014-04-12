package com.doom;

public enum UniType {
	Integer,
	Float,
	String,
	Boolean,
	LinedefType, //int
	SectorEffect, //int
	Texture, //string
	Flat, //string
	AngleDegrees, //int
	AngleRadians, //float
	Color, //string RRGGBB missing AA
	EnumOption, //???probably an int?
	EnumBits, //???probably an int?
	SectorTag, //int
	ThingTag, //int
	LinedefTag, //int
	EnumStrings, //string
	AngleDegreesFloat, //float
	ThingType, //int
	ThingClass; //int, don't think this one works in tools?
}
