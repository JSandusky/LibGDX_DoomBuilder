package com.doom;

import java.io.Serializable;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.XmlReader;
import com.doom.util.Slopes;

//Texture behavior
/** I don't think these are exact as they're implemented 
 * with lowerUnpegged set the lowerTexture changes with ceiling height 
 * rather than follow that, I've left the lowerTexture as top-pegged 
 * as lowerUnpegged seems to be for middle textures
 * */
// Uppers
	//Default = Texture starts from bottom and goes up
	//UpperUnpegged = Texture starts from top and goes down
// Lowers
	//Default = Texture starts at top and goes down
	
// Middles
	//Default = Texture starts at top and goes down

/**via: http://doom.wikia.com/wiki/Sidedef
 * 
 * A sidedef contains the wall texture data for each linedef (though sidedefs do not reference linedefs directly, indeed it is the other way around). Each sidedef contains texture data, offsets for the textures and the number of the sector it references (this is how sectors get their 'shape').
 * 
 * @author Jon
 *
 */
@SuppressWarnings("serial")
public class SideDef implements Serializable, PostDeserialize {
	public static final int LOWER = 0;
	public static final int MIDDLE = 1;
	public static final int UPPER = 2;
	
	public int index;
	int lineIndex; //used for serial
	public boolean isFront;
	public int offsetX, offsetY;
	
	public String lowTexName, midTexName, highTexName;
	
	//rebuild/postSerialize will set these
	public transient LineDef line;
	public transient Sector sector;
	public transient Texture middleTex;
	public transient float[] middleVertices;
	public transient Texture lowerTex;
	public transient float[] lowerVertices;
	public transient Texture upperTex;
	public transient float[] upperVertices;
	
	public SideDef(XmlReader.Element elem, ResourceResolver reso) {
		index = elem.getInt("idx");
		line = new LineDef(elem.getInt("line"));
		lineIndex = line.index;
		
		lowTexName = elem.get("low-tex","");
		midTexName = elem.get("middle-tex","");
		highTexName = elem.get("high-tex","");
		
		resolveTextures(reso);
		
		offsetX = elem.getInt("offset-x");
		offsetY = elem.getInt("offset-y");
		isFront = elem.getBoolean("front");
	}
	
	SideDef(int index) {
		this.index = index;
	}
	
	@SuppressWarnings("unused")
	private SideDef() {
		//serialization
	}
	
	public SideDef getOtherSide() {
		if (isFront) {
			return line.back;
		} else {
			return line.front;
		}
	}
	
	public void rebuild(IntMap<LineDef> lines) {
		line = lines.get(line.index);
		if (this.isFront)
			line.front = this;
		else
			line.back = this;
		lineIndex = line.index;
	}
	
	public void postDeserialize(DoomMap map, ResourceResolver reso) {
		for (LineDef line : map.lines.values()) {
			if (line.index == lineIndex) {
				this.line = line;
				if (this.isFront)
					line.front = this;
				else
					line.back = this;
				break;
			}
		}
		resolveTextures(reso);
	}
	
	void resolveTextures(ResourceResolver reso) {
		if (lowTexName.length() > 1) {
			lowerTex = reso.getTexture(lowTexName, TextureRole.Wall);
			if (lowerTex == null)
				lowTexName = "";
			else
				lowerTex.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
		}
		if (midTexName.length() > 1) {
			middleTex = reso.getTexture(midTexName, TextureRole.Wall);
			middleTex.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
		}
		if (highTexName.length() > 1) {
			upperTex = reso.getTexture(highTexName, TextureRole.Wall);
			upperTex.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
		}
	}
	
	/** a valid case for overriding would be to create cave walls */
	public void draw(DoomMapRenderer renderer, Vector2 relTo) {
		if (lowerVertices != null) {
			renderer.queue(lowerVertices,lowerTex, lowTexName);
		}
		if (middleVertices != null) {
			renderer.queue(middleVertices,middleTex, midTexName);
		}
		if (upperVertices != null) {
			renderer.queue(upperVertices,upperTex, highTexName);
		}
	}
	
	//these are cached
	transient boolean checkForSlopedFloors = false;
	transient boolean checkForSlopedCeilings = false;
	/** generates the quads for drawing the SideDef parts, a good case for overriding would be to create cave walls with random vertex offsets */
	protected void buildQuads() {
		
		final Color lightColor = Sector.lightLevels[sector.lighting];
		if (getOtherSide() != null) {
			checkForSlopedFloors = (Slopes.findSlopeLine(sector, false) != null || Slopes.findSlopeLine(getOtherSide().sector, false) != null);
			checkForSlopedCeilings = (Slopes.findSlopeLine(sector, true) != null || Slopes.findSlopeLine(getOtherSide().sector, true) != null);
		}
		if (lowerTex != null && getOtherSide() != null) {
			if (getOtherSide().sector.currentFloorHeight != sector.currentFloorHeight || checkForSlopedFloors) {
				if (lowerVertices == null)
					lowerVertices = new float[6*4];
				final float lowest = Math.min(this.getOtherSide().sector.currentFloorHeight, sector.currentFloorHeight);
				
				if (lowest == sector.currentFloorHeight) {
					setVertices(lowerVertices, lowerTex.getWidth(),
							getOtherSide().sector.currentFloorHeight + (int)line.floorZOffset, lowest + (int)line.floorZOffset,
							lightColor,lightColor, false, 0);
				}
			}
		}
		if (upperTex != null) {
			if (getOtherSide().sector.currentCeilingHeight != sector.currentCeilingHeight || checkForSlopedFloors) {
				if (upperVertices == null)
					upperVertices = new float[6*4];
				final float highest = Math.max(this.getOtherSide().sector.currentCeilingHeight, sector.currentCeilingHeight);
				if (highest != sector.currentCeilingHeight) {
					setVertices(upperVertices, upperTex.getWidth(),
							sector.currentCeilingHeight + (int)line.ceilingZOffset, getOtherSide().sector.currentCeilingHeight + (int)line.ceilingZOffset,
							lightColor,lightColor, !isUpperUnpegged(), 2);
				}
			}
		}
		if (middleTex != null) {
			if (middleVertices == null)
				middleVertices = new float[6*4];
			float bottom = sector.currentFloorHeight;
			float top = sector.currentCeilingHeight;
			
			if (getOtherSide() != null) {
				if (sector.currentCeilingHeight != getOtherSide().sector.currentCeilingHeight) {
					top = Math.min(sector.currentCeilingHeight, getOtherSide().sector.currentCeilingHeight);
				}
				
				if (sector.currentCeilingHeight != getOtherSide().sector.currentCeilingHeight) {
					bottom = Math.max(sector.currentFloorHeight, getOtherSide().sector.currentFloorHeight);
				}
			}
			setVertices(middleVertices,middleTex.getWidth(),
					top + (int)line.ceilingZOffset,bottom + (int)line.floorZOffset,
					lightColor,lightColor, isLowerUnpegged(),1);
		}
	}
	
	static Vector2 tmp = new Vector2();
	/** generally wouldn't recommend overriding this, call it and then alter the target values */
	protected void setVertices(float[] target, float texWidth, float top, float bottom, Color leftLightColor, Color rightLightColor, boolean lowerPeg, int purpose) {
		//top left position
		target[0] = line.a.x;
		target[1] = top;
		target[2] = line.a.y;
		
		//Texture coords
		target[3] = 0 + (offsetX/texWidth);
		if (lowerPeg)
			target[4] = 1f - ((top-bottom) * (1/texWidth) + (offsetY * (1f/texWidth)));
		else //upper peg
			target[4] = 0 + (offsetY/texWidth);
		//color
		target[5] = leftLightColor != null ? leftLightColor.toFloatBits() : Color.WHITE.toFloatBits(); //color
		
		//top right position
		target[6] = line.b.x;
		target[7] = top;
		target[8] = line.b.y;
		//texture coords
		target[9] = 0 + (line.a.dst(line.b)*((1f)/texWidth)) + (offsetX * (1f/texWidth));
		if (lowerPeg)
			target[10] = 1f - ((top-bottom) * (1/texWidth) + (offsetY * (1f/texWidth)));
		else //upper peg
			target[10] = 0 + (offsetY * (8f/texWidth));
		//color
		target[11] = rightLightColor != null ? rightLightColor.toFloatBits() : Color.WHITE.toFloatBits();
		
		//bottom right position
		target[12] = line.b.x;
		target[13] = bottom;
		target[14] = line.b.y;
		//texture coords
		target[15] = 0 + (line.a.dst(line.b)*((1f)/texWidth)) + (offsetX * (1f/texWidth));
		if (lowerPeg)
			target[16] = 1f;
		else //upper peg
			target[16] = (top-bottom) * (1f/texWidth) + (offsetY * (1f/texWidth));
		//color
		target[17] = rightLightColor != null ? rightLightColor.toFloatBits() : Color.WHITE.toFloatBits();
		
		
		//bottom left position
		target[18] = line.a.x;
		target[19] = bottom;
		target[20] = line.a.y;
		//texture coords
		target[21] = 0 + (offsetX * (1f/texWidth));
		if (lowerPeg)
			target[22] = 1f;
		else //upper peg
			target[22] = (top-bottom) * (1/texWidth) + (offsetY * (1f/texWidth));
		//color
		target[23] = leftLightColor != null ? leftLightColor.toFloatBits() : Color.WHITE.toFloatBits();
		
		//Deal with the insansity of checking for slopes,
		if (purpose == LOWER && checkForSlopedFloors) { 
			tmp.set(target[0],target[2]);
			float value = Math.max(getOtherSide().sector.getFloorHeight(tmp),sector.getFloorHeight(tmp));
			target[1] = value;
			
			tmp.set(target[6],target[8]);
			value = Math.max(getOtherSide().sector.getFloorHeight(tmp),sector.getFloorHeight(tmp));
			target[7] = value;
			
			tmp.set(target[12],target[14]);
			value = Math.min(getOtherSide().sector.getFloorHeight(tmp),sector.getFloorHeight(tmp));
			target[13] = value;
			target[16] = (target[7]-target[13]) * (1f/texWidth);
			
			tmp.set(target[18],target[20]);
			value = Math.min(getOtherSide().sector.getFloorHeight(tmp),sector.getFloorHeight(tmp));
			target[19] = value;
			target[22] = (target[1]-target[19]) * (1f/texWidth); 
			
		} else if (purpose == UPPER && checkForSlopedCeilings) {
			tmp.set(target[0],target[2]);
			float value = Math.max(getOtherSide().sector.getCeilingHeight(tmp),sector.getCeilingHeight(tmp));
			target[1] = value;
			
			tmp.set(target[6],target[8]);
			value = Math.max(getOtherSide().sector.getCeilingHeight(tmp),sector.getCeilingHeight(tmp));
			target[7] = value;
			
			tmp.set(target[12],target[14]);
			value = Math.min(getOtherSide().sector.getCeilingHeight(tmp),sector.getCeilingHeight(tmp));
			target[13] = value;
			
			tmp.set(target[18],target[20]);
			value = Math.min(getOtherSide().sector.getCeilingHeight(tmp),sector.getCeilingHeight(tmp));
			target[19] = value;
			
			if (lowerPeg) {
				target[10] = 1f - ((target[7]-target[13]) * 1f/texWidth);
				target[4] = 1f - ((target[1]-target[9]) * 1f/texWidth);
			} else {
				target[16] = (target[7]-target[13]) * (1f/texWidth);
				target[22] = (target[1]-target[19]) * (1f/texWidth); 
			}
			
		} else if (purpose == MIDDLE && (checkForSlopedFloors || checkForSlopedCeilings)) {
			
			tmp.set(target[0],target[2]);
			float value = Math.min(getOtherSide().sector.getCeilingHeight(tmp),sector.getCeilingHeight(tmp));
			target[1] = value;
			
			tmp.set(target[6],target[8]);
			value = Math.min(getOtherSide().sector.getCeilingHeight(tmp),sector.getCeilingHeight(tmp));
			target[7] = value;
			
			tmp.set(target[12],target[14]);
			value = Math.max(getOtherSide().sector.getFloorHeight(tmp),sector.getFloorHeight(tmp));
			target[13] = value;
			
			tmp.set(target[18],target[20]);
			value = Math.max(getOtherSide().sector.getFloorHeight(tmp),sector.getFloorHeight(tmp));
			target[19] = value;
			
			if (lowerPeg) {
				target[10] = 1f - ((target[7]-target[13]) * 1f/texWidth);
				target[4] = 1f - ((target[1]-target[9]) * 1f/texWidth);
			} else {
				target[16] = (target[7]-target[13]) * (1f/texWidth);
				target[22] = (target[1]-target[19]) * (1f/texWidth); 
			}
		}
	}
	
	public static SideDef getRightMost(SideDef[] list) {
		SideDef curBest = list[0];
		float x = Float.MIN_VALUE;
		for (SideDef s : list) {
			if (s.line.a.x > x) {
				curBest = s;
				x = s.line.a.x;
			}
			if (s.line.b.x > x) {
				curBest = s;
				x = s.line.b.x;
			}
		}
		return curBest;
	}
	
	/** technically this got roled into the other one */
	@Deprecated
	static boolean isConnected(SideDef from, SideDef other) {
		if (from.isFront) {
			if (other.isFront) {
				return other.line.a.equals(from.line.b);
			} else {
				return other.line.b.equals(from.line.b);
			}
		} else {
			if (other.isFront) {
				return other.line.a.equals(from.line.a);
			} else {
				return other.line.b.equals(from.line.a);
			}
		}
	}
	
	@Deprecated
	static boolean isBConnected(SideDef from, SideDef other) {
		if (other.line.a.equals(from.line.b))
			return false;
		if (other.line.b.equals(from.line.b))
			return true;
		
		return false; //??errp?
	}
	
	@Deprecated
	public static SideDef getNext(SideDef current, SideDef[] list, Array<SideDef> ignore) {
		for (SideDef s : list) {
			if (s == current)
				continue;
			//if (s.isFront) {
				if (isConnected(current,s) && !ignore.contains(s, true))
					return s;
			//} else {
				//if (s.line.b.equals(current.line.a))
					//return s;
			//}
		}
		return null;
	}
	
	protected boolean isLowerUnpegged() {
		return sector.index == 1;
	}
	protected boolean isUpperUnpegged() {
		return sector.index == 1;
	}
	
	public static SideDef getNextSide(SideDef current, Sector s) {
		if (current.isFront) {
			for (SideDef side : s.sides) {
				if (side == current)
					continue;
				if (side.isFront) {
					if (side.line.a.equals(current.line.b))
						return side;
				} else {
					if (side.line.b.equals(current.line.b))
						return side;
				}
			}
		} else {
			for (SideDef side : s.sides) {
				if (side == current)
					continue;
				if (side.isFront) {
					if (side.line.a.equals(current.line.a))
						return side;
				} else {
					if (side.line.b.equals(current.line.a))
						return side;
				}
			}
		}
		return null;
	}
	
	public static void fill(Sector s, SideDef start, Array<Vector2> pts) {
		SideDef current = start;
		if (start.isFront) {
			pts.add(start.line.a);
			pts.add(start.line.b);
		} else {
			pts.add(start.line.b);
			pts.add(start.line.a);
		}
		do {
			current = getNextSide(current,s);
			if (current != null) {
				if (current.isFront) {
					pts.add(current.line.b);
				} else {
					pts.add(current.line.a);
				}
			}
		} while (current != null && current != start);
	}
}
