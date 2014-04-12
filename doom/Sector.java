package com.doom;

import java.io.Serializable;
import java.util.Comparator;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.math.EarClippingTriangulator;
import com.badlogic.gdx.math.Frustum;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Plane;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ShortArray;
import com.badlogic.gdx.utils.XmlReader;
import com.doom.util.Slopes;
import com.doom.vis.FrustumClipper;

//Testing Checklist
//	-Floor/ceiling tilt works
//	-SideDefs do not adjust to the floor ceiling
/** a sector is an open polygon space */
@SuppressWarnings("serial")
public class Sector extends MapObject implements IDoomDrawable, Serializable, PostDeserialize {
	static Color[] lightLevels;
	
	static {
		lightLevels = new Color[256];
		for (int i = 0; i <= 255; ++i) {
			lightLevels[i] = new Color(i/255f,i/255f,i/255f,1f);
		}
	}
	
	@Override
	public String toString() {
		return "Index: " + index + " - Tag: " + tag;
	}
	
	public int index;
	public int tag;
	public int special;
	public int lighting;
	public Color lightColor;
	
	public float floorHeight;
	public float ceilingHeight;
	
	public float currentFloorHeight;
	public float currentCeilingHeight;
	
	public String floorTextureName;
	public String ceilingTextureName;
	
	public Vector2 floorTextureOffset = new Vector2();
	public Vector2 ceilingTextureOffset = new Vector2();
	
	//rebuild/postSerialize will set these
	public transient Texture floorTexture;
	public transient Texture ceilingTexture;
	public transient DoomMap map;
	
	int[] sideIndexes; //these are used for reconstruction from serialization
	public transient SideDef[] sides;
	public transient Array<SectorAction> actions = new Array<SectorAction>();
	
	public ObjectMap<String,UniField> fields = new ObjectMap<String,UniField>();
	
	public Sector(XmlReader.Element elem, ResourceResolver resolver, Vector2 offset, Vector2 scale, float vScale) {
		index = elem.getInt("idx");
		currentCeilingHeight = ceilingHeight = (int) (elem.getInt("ceiling-height") * vScale);
		currentFloorHeight = floorHeight = (int) (elem.getInt("floor-height") * vScale);
		floorTextureName = elem.get("floor-tex","");
		ceilingTextureName = elem.get("ceiling-tex","");
		lighting = elem.getInt("lighting",255);
		special = elem.getInt("special",0);
		
		loadTextures(resolver);
		
		tag = elem.getInt("tag");
		
		String[] sideList = elem.get("sides").split(",");
		sides = new SideDef[sideList.length];
		sideIndexes = new int[sideList.length];
		for (int i = 0; i < sideList.length; ++i) {
			final int val = Integer.parseInt(sideList[i]);
			sideIndexes[i] = val;
			sides[i] = new SideDef(val);
		}
		
		XmlReader.Element fields = elem.getChildByName("fields");
		DoomMap.readFields(fields, this.fields);
	}
	
	protected Sector(int idx) {
		index = idx;
	}
	
	@SuppressWarnings("unused")
	protected Sector() {
		//serializer
	}
	
	void rebuild(IntMap<SideDef> sides) {
		for (int i = 0; i < this.sides.length; ++i) {
			SideDef other = sides.get(this.sides[i].index);
			this.sides[i] = other;
			other.sector = this;
		}
	}
	
	public void postDeserialize(DoomMap map, ResourceResolver reso) {
		if (tag != 0)
			map.sectorTags.put(tag, this);
		
		sides = new SideDef[sideIndexes.length];
		this.map = map;
		for (int i = 0; i < this.sideIndexes.length; ++i) {
			SideDef def = map.sides.get(sideIndexes[i]);
			if (def == null) {
				for (SideDef d : map.sides.values()) {
					if (d.index == sideIndexes[i]) {
						def = d;
						break;
					}
				}
			}
			sides[i] = def;
			def.sector = this;
		}
		
		loadTextures(reso);
	}
	
	void loadTextures(ResourceResolver resolver) {
		if (floorTextureName.length() > 0) {
			floorTexture = resolver.getTexture(floorTextureName, TextureRole.Flat);
			floorTexture.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
		}
		if (ceilingTextureName.length() > 0) {
			ceilingTexture = resolver.getTexture(ceilingTextureName, TextureRole.Flat);
			ceilingTexture.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
		}
	}
	
	/** To be used as just ONE part of the rational behind moving from sector to sector, this just checks height and step restrictions*/
	public boolean canEnter(Sector from, int stepHeight, int objectHeight) {
		if (!checkHeight(objectHeight))
			return false;
		
		final float difference = Math.max(from.currentFloorHeight,currentFloorHeight) - Math.min(from.currentFloorHeight, currentFloorHeight);
		final boolean iAmLower = currentFloorHeight < from.currentFloorHeight;
		
		if (iAmLower) {
			return true;
		}
		if (difference <= stepHeight)
			return true;
		
		return false;
	}
	
	public boolean contains(Vector2 pt) {
		if (floor.getBoundingRectangle().contains(pt)) {
			if (floor != null)
				return floor.contains(pt.x, pt.y);
		}
		return false;
	}
	
	public boolean checkHeight(float height) { //check that something fits into this sector
		return currentFloorHeight + height < currentCeilingHeight;
	}
	
	public void setFloorHeight(float newHeight) {
		currentFloorHeight = newHeight;
		rebuildGeometry();
	}
	
	public void setCeilingHeight(float newHeight) {
		currentCeilingHeight = newHeight;
		rebuildGeometry();
	}
	
	public void restoreHeights() {
		currentCeilingHeight = this.ceilingHeight;
		currentFloorHeight = this.floorHeight;
		rebuildGeometry();
	}
	
	public void setFloorTexture(Texture tex) {
		this.floorTexture = tex;
	}
	
	public void setCeilingTexture(Texture tex) {
		this.ceilingTexture = tex;
	}

	transient Array<SideDef> sectorSortedSideDefs;
	void sortSideDefs() {
		if (sectorSortedSideDefs == null) {
			sectorSortedSideDefs = new Array<SideDef>();
			for (SideDef sd : sides) {
				sectorSortedSideDefs.add(sd);
			}
			sectorSortedSideDefs.sort(sideSorter);
		}
	}
	static Comparator<SideDef> sideSorter = new Comparator<SideDef>() {
		@Override
		public int compare(SideDef lhs, SideDef rhs) {
			final boolean lhsOther = lhs.getOtherSide() != null;
			final boolean rhsOther = rhs.getOtherSide() != null;
			if (!lhsOther && !rhsOther) {
				return 0;
			} else if (lhsOther && !rhsOther) {
				return -1;
			} else if (rhsOther && !lhsOther) {
				return 1;
			}
			
			final int lhsIndex = lhs.getOtherSide().sector.index;
			final int rhsIndex = rhs.getOtherSide().sector.index;
			if (lhsIndex == rhsIndex) {
				return 0;
			} else if (lhsIndex < rhsIndex) {
				return -1;
			} else {
				return 1;
			}
		}
	};
	
	//transient BoundingBox renderBounds = new BoundingBox(); //this bounds is set by our parent when rendering
	static BoundingBox tmp = new BoundingBox();
	transient boolean[] renderPassingSides;
	transient Frustum localFrustum = new Frustum(); //parent in render call will set this
	@Override
	public void draw(DoomMapRenderer batch, Frustum frustum, Vector2 relTo, int depth) {
		if (depth > 128) //64 would be the same as the classic VisPlane crash
			return;
		BoundingBox clipper = null;
		if (renderPassingSides == null) {
			renderPassingSides = new boolean[sides.length];
		}
		for (int i = 0; i < renderPassingSides.length; ++i)
			renderPassingSides[i] = false;
		
		
		if (geometryDirty)
			rebuildGeometry_();
		if (!drawSurfaces(batch))
			return;
		
		sortSideDefs();
		
		boolean anyPassed = false;
		Sector lastSector = null;
		for (int i = 0; i < sectorSortedSideDefs.size; ++i) {
			final SideDef sd = sectorSortedSideDefs.get(i); 
			if (sd.line.frontSide(relTo))
				sd.draw(batch, relTo);
				
			//If our frustum is null we will have drawn everything like a idiot
			/* that case is mostly only valid for weird things */
			//TODO calculate different frustums for each unique sector
			if (frustum != null && sd.getOtherSide() != null) {
				final Sector thisSector = sd.getOtherSide().sector;
				sd.line.calculateBounds(tmp);
				
				/*if (thisSector != lastSector) {
					clipper = bndsMaps.get(thisSector.index);
					if (clipper == null) {
						clipper = new BoundingBox(tmp);
						bndsMaps.put(thisSector.index, clipper);
					} else
						clipper.set(tmp);
				}*/
				if (frustum.boundsInFrustum(tmp)) {
					//clipper.ext(tmp);
					thisSector.draw(batch, frustum, relTo, depth+1);
					//renderPassingSides[i] = true;
					//anyPassed = true;
				}
				lastSector = thisSector;
			}
		}
		
		/*if (frustum != null && anyPassed) {
			//FrustumClipper.clipBox(frustum, clipper);
			lastSector = null;
			for (int i = 0; i < renderPassingSides.length; ++i) {
				if (renderPassingSides[i]) { //side passed
					final Sector thisSector = sectorSortedSideDefs.get(i).getOtherSide().sector;
					if (thisSector != lastSector) {
						FrustumClipper.set(thisSector.localFrustum,frustum);
						//FrustumClipper.clipBox(thisSector.localFrustum, bndsMaps.get(thisSector.index));
						thisSector.draw(batch, thisSector.localFrustum, relTo, depth + 1);
					}
				}
			}
		}*/
	}
	transient IntMap<BoundingBox> bndsMaps = new IntMap<BoundingBox>();
	
	public boolean drawSurfaces(DoomMapRenderer batch) {
		if (this.floorTexture != null)
			if (!batch.queue(this,true))
				return false;
		if (this.ceilingTexture != null)
			if (!batch.queue(this,false))
				return false;
		return true;
	}
	
	public void drawLines() {
		
	}
	
	transient Polygon floor;
	public Polygon getPolygon() {return floor;}
	transient ShortArray indices;
	transient Mesh floorMesh;
	transient Mesh ceilingMesh;
	
	public void debugDraw(ShapeRenderer sr) {
		for (int i = 0; i < floor.getVertices().length - 2; i += 2) {
			sr.line(floor.getVertices()[i], floorHeight, floor.getVertices()[i+1], 
					floor.getVertices()[i+2], floorHeight, floor.getVertices()[i+3]);
			
			sr.line(floor.getVertices()[i], ceilingHeight, floor.getVertices()[i+1], 
					floor.getVertices()[i+2], ceilingHeight, floor.getVertices()[i+3]);
			
			sr.line(floor.getVertices()[i], floorHeight, floor.getVertices()[i+1], 
					floor.getVertices()[i], ceilingHeight, floor.getVertices()[i+1]);
		}	
		sr.line(floor.getVertices()[0], floorHeight, floor.getVertices()[1],floor.getVertices()[floor.getVertices().length-2],floorHeight,floor.getVertices()[floor.getVertices().length-1]);
		sr.line(floor.getVertices()[0], ceilingHeight, floor.getVertices()[1],floor.getVertices()[floor.getVertices().length-2],ceilingHeight,floor.getVertices()[floor.getVertices().length-1]);
		sr.line(floor.getVertices()[floor.getVertices().length-2],ceilingHeight,floor.getVertices()[floor.getVertices().length-1],floor.getVertices()[floor.getVertices().length-2],floorHeight,floor.getVertices()[floor.getVertices().length-1]);
	}
	
	public void mapDraw(ShapeRenderer sr) {
		for (SideDef side : sides) {
			if (side.line.doubleSided) {
				sr.setColor(Color.GRAY);
				sr.line(side.line.a.x, side.line.a.y*-1, side.line.b.x, side.line.b.y*-1);
			} else if (!side.line.hidden) {
				sr.setColor(Color.WHITE);
				sr.line(side.line.a.x, side.line.a.y*-1, side.line.b.x, side.line.b.y*-1);
			}
		}
	}
	
	public void triDraw(ShapeRenderer sr) {
		for (int i = 0; i < indices.size; i+=3) {
			sr.line(floor.getVertices()[indices.get(i)*2], floor.getVertices()[indices.get(i)*2 + 1], floor.getVertices()[indices.get(i)*2 + 2], floor.getVertices()[indices.get(i)*2 + 3]);
		}
	}
	
	public Vector2 getCentroid() {
		Vector2 out = new Vector2();
		floor.getBoundingRectangle().getCenter(out);
		return out;
	}
	
	transient FloatArray floorZ;
	transient FloatArray ceilingZ;
	void buildGeometry() {
		//??
		Array<Vector2> pts = new Array<Vector2>();
		
		SideDef side = SideDef.getRightMost(sides);
		SideDef rightMost = side;
		/*SideDef prev = null;
		Array<SideDef> checked = new Array<SideDef>();
		floorZ = new FloatArray();
		ceilingZ = new FloatArray();
		while (side != null) {
			
			if (prev == null || !SideDef.isBConnected(prev, side)) {
				if (!pts.contains(side.line.a, false)) {
					pts.add(side.line.a.cpy());
					floorZ.add(side.line.floorZOffset);
					ceilingZ.add(side.line.ceilingZOffset);
				}
			} else {
				if (!pts.contains(side.line.b, false)) {
					pts.add(side.line.b.cpy());
					floorZ.add(side.line.floorZOffset);
					ceilingZ.add(side.line.ceilingZOffset);
				}
				if (!pts.contains(side.line.a, false)) {
					pts.add(side.line.a.cpy());
					floorZ.add(side.line.floorZOffset);
					ceilingZ.add(side.line.ceilingZOffset);
				}
			}
			
			checked.add(side);
			prev = side;
			side = SideDef.getNextSide(side, this);//sides,checked);
			if (checked.contains(side, true))
				side = null;
		}
		
		pts.clear();*/
		SideDef.fill(this, rightMost, pts);
		
		float[] floorVerts = new float[pts.size*2];
		for (int i = 0, v = 0; i < pts.size; ++i, v += 2) {
			Vector2 pt = pts.get(i);
			floorVerts[v] = pt.x;
			floorVerts[v+1] = pt.y;
		}
		floor = new Polygon(floorVerts);
		
		EarClippingTriangulator tri = new EarClippingTriangulator();
		indices = tri.computeTriangles(floor.getVertices());
	
		buildPlanes();
		
		floorMesh = buildMesh(null, true, floorZ, floorProjectionPlane);
		ceilingMesh = buildMesh(null, false, ceilingZ, ceilingProjectionPlane);
		
		for (SideDef sd : this.sides)
			sd.buildQuads();
	}
	
	/** Constructs planes for sloped surfaces, the floor/ceiling mesh will be projected onto its plane */
	void buildPlanes() {
		LineDef slope = Slopes.findSlopeLine(this, false /*floor*/);
		if (slope != null)
			floorProjectionPlane = Slopes.constructPlane(this, slope, false);
		
		slope = Slopes.findSlopeLine(this, true /*ceiling*/);
		if (slope != null)
			ceilingProjectionPlane = Slopes.constructPlane(this, slope, true);
	}
	
	/** if not null then this plane will be used projection the surface mesh onto a plane */
	private transient Plane floorProjectionPlane;
	/** if not null then this plane will be used projection the surface mesh onto a plane */
	private transient Plane ceilingProjectionPlane;
	
	protected transient boolean geometryDirty = false;
	
	/** marks geometry as requiring a rebuild on the next render */
	public void rebuildGeometry() {
		geometryDirty = true;
	}
	
	/** does the actual rebuilding of geometry */
	protected void rebuildGeometry_() {
		buildPlanes();
		buildMesh(floorMesh,true,floorZ, floorProjectionPlane);
		buildMesh(ceilingMesh,false,ceilingZ, ceilingProjectionPlane);
		for (SideDef side : this.sides)
			side.buildQuads();
	}
	
	static Vector3 tmpVec = new Vector3();
	static Ray tmpRay = new Ray(new Vector3(0,0,0),Vector3.Y);
	Mesh buildMesh(Mesh in, boolean isFloor,FloatArray zOffsets, Plane projectOnto) {
		
		final Vector2 offset = isFloor ? floorTextureOffset : ceilingTextureOffset;
		
		if (in == null) {
			in = new Mesh(false, false, (floor.getVertices().length/2), indices.size, 
					new VertexAttributes(
							new VertexAttribute(Usage.Position,3,"a_position"),
							new VertexAttribute(Usage.ColorPacked,4,"a_color"),
							new VertexAttribute(Usage.TextureCoordinates,2,"a_texCoords")
							));
		}
		
		float[] verts = new float[((floor.getVertices().length)/2) * 6];
		
		Color col = Sector.lightLevels[lighting];
		for (int i = 0, vert = 0, z = 0; vert < floor.getVertices().length; ++z) {
			verts[i] = floor.getVertices()[vert];
			verts[i+2] = floor.getVertices()[vert+1];
			if (projectOnto == null) {
				verts[i+1] = isFloor ? this.currentFloorHeight : this.currentCeilingHeight; 
				//verts[i+1] += zOffsets.get(z);
			} else {
				tmpRay.origin.set(verts[i],-10000,verts[i+2]);
				if (Intersector.intersectRayPlane(tmpRay, projectOnto, tmpVec)) {
					verts[i+1] = tmpVec.y;
				} else {
					//??errp
					verts[i+1] = 0;
				}
			}
			
			verts[i+3] = col.toFloatBits();
			
			final Texture tex = isFloor ? floorTexture : ceilingTexture;
			final float factor = tex.getWidth();
			verts[i+4] = offset.x + floor.getVertices()[vert]/factor;
			verts[i+5] = offset.y + (floor.getVertices()[vert+1]/factor);
			
			i += 6;
			vert += 2;
		}
		
		in.setVertices(verts);
		in.setIndices(indices.toArray());
		
		return in;
	}
	
	void normalizePoly(Polygon poly) {
		//if (normalized)
		//	return;
		float[] verts = poly.getVertices();
		
		float value = 0;
		for (int i = 0; i < verts.length; i += 2) {
			float x1 = verts[i];
			float y1 = verts[i+1];
			float x2, y2 = 0;
			if (i == verts.length - 2) {
				x2 = verts[0];
				y2 = verts[1];
			} else {
				x2= verts[i+2];
				y2 = verts[i+3];
			}
			value += (x2-x1)*(y2+y1);
		}
		
		if (value < 0) {
			float[] dupe = new float[verts.length];
			for (int i = 0, dp = dupe.length-1; i < verts.length; i+=2) {
				dupe[dp--] = verts[i+1];
				dupe[dp--] = verts[i];
			}
			poly.setVertices(dupe);
		}
	}
	
	private static Array<Sector> _eventStack_ = new Array<Sector>();
	
	/** propagates a floor-filling event through the map sectors */
	public void sendEvent(SectorEvent event) {
		sendEvent_(event);
		_eventStack_.clear();
	}
	
	private boolean sendEvent_(SectorEvent event) {
		if (event.doEvent(this))
			return true;
		_eventStack_.add(this);
		
		for (SideDef side : sides) {
			if (side.line.doubleSided) {
				if (!_eventStack_.contains(side.getOtherSide().sector, true)) {
					if (side.getOtherSide().sector.sendEvent_(event)) {
						return true;
					}
				}
			}
			event.popped();
		}
		return false;
	}
	
	/** a shallow ray cast that only returns the first intesecting side */
	public SideDef castRay(Vector2 from, Vector2 to) {
		for (SideDef side : sides){
			if (side.line.intersects(from, to)) {
				return side;
			}
		}
		return null; 
	}
	
	/** deeply casts through sectors until it hits a one sided line */
	public SideDef castRayDeep(Vector2 from, Vector2 to) {
		return castRayDeep_(from,to,null);
	}
	
	/** repeated casting of a line segment */
	SideDef castRayDeep_(Vector2 from, Vector2 to, SideDef passedThrough) {
		for (SideDef side : sides){
			if (side != passedThrough) {
				if (side.line.intersects(from, to)) {
					if (side.line.doubleSided) {
						return side.getOtherSide().sector.castRayDeep_(from, to, side);
					}
					return side;
				}
			}
		}
		return null;
	}
	
	/** gets an accurate floor height using either currentFloorHeight or using the projection plane */
	public float getFloorHeight(Vector2 at) {
		if (floorProjectionPlane != null) {
			tmpRay.origin.x = at.x;
			tmpRay.origin.z = at.y;
			tmpRay.origin.y = -10000;
			if (Intersector.intersectRayPlane(tmpRay, floorProjectionPlane, tmpVec)) {
				return tmpVec.y;
			}
		} return currentFloorHeight;
	}

	/** gets an accurate ceiling height using either currentCeilingHeight or using the projection plane */
	public float getCeilingHeight(Vector2 at) {
		if (ceilingProjectionPlane != null) {
			tmpRay.origin.x = at.x;
			tmpRay.origin.z = at.y;
			tmpRay.origin.y = -10000;
			if (Intersector.intersectRayPlane(tmpRay, ceilingProjectionPlane, tmpVec)) {
				return tmpVec.y;
			}
		} return currentCeilingHeight;
	}
	
	/** fills the given array with all neighboring sectors of this sector */
	public void getNeighbors(Array<Sector> holder) {
		getNeighbors(holder,null);
	}
	
	/** gets a list of neighboring sectors that are not in the exclusion array (if it is not null)*/
	public void getNeighbors(Array<Sector> holder, Array<Sector> exclude) {
		for (SideDef side : sides) {
			if (side.getOtherSide() != null) {
				if (exclude == null || !exclude.contains(side.getOtherSide().sector, true)) {
					holder.add(side.getOtherSide().sector);
				}
			}
		}
	}

	public float getVSpace() {
		return currentCeilingHeight - currentFloorHeight;
	}
}
