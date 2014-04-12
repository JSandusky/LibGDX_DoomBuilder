package com.doom;

import java.io.Serializable;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntIntMap;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.XmlReader;
import com.doom.actions.lights.LightBlinkAction;

@SuppressWarnings("serial")
public class DoomMap implements Serializable, PostDeserialize {
	Array<Sector> sectors = new Array<Sector>();
	Array<Thing> things = new Array<Thing>();
	
	//These are only used for serialization
	private ObjectMap<Integer,LineDef> serialLines = new ObjectMap<Integer,LineDef>();
	private ObjectMap<Integer,SideDef> serialSides = new ObjectMap<Integer,SideDef>();
	
	//by tags
	transient IntMap<Sector> sectorTags = new IntMap<Sector>();
	transient IntMap<LineDef> lineTags = new IntMap<LineDef>();
	
	//by indexes
	transient IntMap<LineDef> lines = new IntMap<LineDef>();
	transient IntMap<SideDef> sides = new IntMap<SideDef>();
	String mapName;
	
	transient IntMap<SectorActionBuilder> actionBuilders = new IntMap<SectorActionBuilder>();
	transient IntMap<ThingBuilder> thingBuilders = new IntMap<ThingBuilder>();
	transient ResourceResolver reso;
	
	public static class DefaultTextureResolver extends ResourceResolver {
		AssetManager assets;
		
		public DefaultTextureResolver(AssetManager am) { assets = am; }
		
		@Override
		public Texture getTexture(String name, TextureRole role) {
			String ext = "";
			if (Gdx.files.local("data/textures/" + name.toLowerCase() + ".png").exists())
				ext = ".png";
			else if (Gdx.files.local("data/textures/" + name.toLowerCase() + ".jpg").exists())
				ext = ".jpg";
			else
				return null; //??failed
			assets.load("data/textures/" + name.toLowerCase() + ext,Texture.class);
			assets.finishLoading();
			return assets.get("data/textures/" + name.toLowerCase() + ext);
		}
	}
	
	public DoomMap() {
		
	}
	
	public static DoomMap createFromJSON(FileHandle file, ResourceResolver reso) {
		Json json = new Json();
		DoomMap map = json.fromJson(DoomMap.class, file);
		map.setResourceResolver(reso);
		map.postDeserialize(map, reso);
		
		return map;
	}
	
	public static void save(DoomMap map, FileHandle file) {
		Json json = new Json();
		map.prepareSerialize();
		json.toJson(map,file);
		map.unprepareSerialize();
	}
	
	private void prepareSerialize() {
		for (IntMap.Entry<LineDef> line : lines.entries())
			serialLines.put(line.key, line.value);
		for (IntMap.Entry<SideDef> side : sides.entries())
			serialSides.put(side.key, side.value);
	}
	
	private void unprepareSerialize() {
		serialLines.clear();
		serialSides.clear();
	}
	
	public void setResourceResolver(ResourceResolver reso) {
		this.reso = reso;
	}
	
	public void loadXML(XmlReader.Element elem) {
		loadXML(elem,new Vector2(0,0),new Vector2(1,1),1f);
	}
	
	public void loadXml(XmlReader.Element elem, Vector2 offset) {
		loadXML(elem,offset,new Vector2(1,1),1f);
	}
	
	public void loadXML(XmlReader.Element elem, Vector2 offset, Vector2 scale, float vScale) {
		mapName = elem.get("name","");
		XmlReader.Element sectors = elem.getChildByName("sectors");
		for (int i = 0; i < sectors.getChildCount(); ++i) {
			final XmlReader.Element sector = sectors.getChild(i);
			Sector s = createSector(sector.getInt("tag",0), sector, reso, offset, scale, vScale);
			if (s != null)
				this.sectors.add(s);
		}
		
		XmlReader.Element linedefs = elem.getChildByName("lines");
		for (int i = 0; i < linedefs.getChildCount(); ++i) {
			final XmlReader.Element line = linedefs.getChild(i);
			LineDef l = createLineDef(line.getInt("tag",0), line, offset, scale, vScale);
			if (l != null)
				lines.put(l.index,l);
		}
		
		XmlReader.Element sidedefs = elem.getChildByName("sides");
		for (int i = 0; i < sidedefs.getChildCount(); ++i) {
			final XmlReader.Element side = sidedefs.getChild(i); 
			SideDef s = createSideDef(side.getInt("tag",0), side, reso);
			if (s != null)
				sides.put(s.index, s);
		}
		
		XmlReader.Element things = elem.getChildByName("things");
		for (int i = 0; i < things.getChildCount(); ++i) {
			final XmlReader.Element thingElem = things.getChild(i);
			int type = thingElem.getIntAttribute("type");
			Thing thing = createThing(type, thingElem.getIntAttribute("tag",0), thingElem, offset, scale, vScale);
			if (thing != null)
				this.things.add(thing);
		}
		
		for (LineDef line : lines.values()) {
			line.rebuild(sides);
			if (line.tag != 0) {
				lineTags.put(line.tag, line);
			}
		}
		
		for (SideDef side : sides.values()) {
			side.rebuild(lines);
		}
		
		for (Sector s : this.sectors) {
			s.rebuild(sides);
			if (s.tag != 0) {
				sectorTags.put(s.tag, s);
			}
			if (s.special != 0) {
				if (actionBuilders.containsKey(s.special)) {
					s.actions.add(actionBuilders.get(s.special).createAction(s));
				}
			}
		}
	}
	
	public void postDeserialize(DoomMap map, ResourceResolver reso) {
		for (LineDef line : serialLines.values()) {
			lines.put(line.index, line);
		}
		for (SideDef side : serialSides.values()) {
			sides.put(side.index, side);
		}
		serialLines.clear();
		serialSides.clear();
		
		for (LineDef line : lines.values()) {
			line.postDeserialize(map, reso);
		}
		for (SideDef side : sides.values()) {
			side.postDeserialize(map, reso);
		}
		for (Sector sector : sectors)
			sector.postDeserialize(map,reso);
	}
	
	public void loadData(AssetManager assets) {
		for (Sector s : sectors) {
			s.buildGeometry();
		}
	}
	
	public Sector getSector(Vector2 position) {
		for (Sector s : sectors) {
			if (s.contains(position))
				return s;
		} return null;
	}
	
	public Sector getSector(int tag) {
		for (Sector s : sectors) {
			if (s.tag == tag)
				return s;
		} return null;
	}
	
	public Thing getThing(int tag) {
		for (Thing t : things) {
			if (t.tag == tag) {
				return t;
			}
		} return null;
	}
	
	public Array<Thing> getThings() {
		return things;
	}
	
	public Array<Thing> getThings(int typeID) {
		Array<Thing> ret = new Array<Thing>();
		for (Thing t : things) {
			if (t.type == typeID)
				ret.add(t);
		}
		return ret;
	}
	
	public void getThingList(int type, Array<Thing> holder) {
		for (Thing t : things) {
			if (t.type == type) {
				holder.add(t);
			}
		}
	}
	
	public Array<Sector> getSectors() {
		return sectors;
	}
	
	static void readFlags(XmlReader.Element root, ObjectMap<String,Boolean> target) {
		if (root == null)
			return;
		for (int i = 0; i < root.getChildCount(); ++i) {
			XmlReader.Element flag = root.getChild(i);
			flag.getAttribute("name");
			target.put(flag.getAttribute("name"), flag.getBoolean("value"));
		}
	}
	
	static void readFields(XmlReader.Element root, ObjectMap<String,UniField> target) {
		if (root == null)
			return;
		for (int i = 0; i < root.getChildCount(); ++i) {
			XmlReader.Element field = root.getChild(i);
			UniField fld = new UniField(field.getInt("type"), field.getText());
			target.put(field.get("name"), fld);
		}
	}
	
	public boolean hasLOS(Vector2 from, Vector2 to) {
		return deepRayCast(from,to) == null;
	}
	
	/** Casts to find the first sidedef that's been hit */
	public SideDef shortRayCast(Vector2 from, Vector2 to) {
		for (Sector s : sectors) {
			if (s.contains(from)) {
				for (SideDef side : s.sides) {
					if (side.line.intersects(from, to)) {
						return side;
					}
				}
			}
		}
		return null;
	}
	
	/** Continues casting through 2-sided lines */
	public SideDef deepRayCast(Vector2 from, Vector2 to) {
		for (Sector s : sectors) {
			if (s.contains(from)) {
				return deepRayCast_(s,from,to,null);
			}
		}
		return null;
	}
	
	SideDef deepRayCast_(Sector sector, Vector2 from, Vector2 to, SideDef passedThrough) {
		for (SideDef side : sector.sides){
			if (side != passedThrough) {
				if (side.line.intersects(from, to)) {
					if (side.getOtherSide() != null && side.getOtherSide().sector.getVSpace() > 0) {
						return deepRayCast_(side.getOtherSide().sector, from, to, side);
					}
					return side;
				}
			}
		}
		return null;
	}
	
	public void update(float td) {
		for (Sector s : sectors) {
			for (int i = 0; i < s.actions.size; ++i) {
				if (s.actions.get(i).update(td)) {
					s.actions.removeIndex(i);
					--i;
				}
			}
		}
	}
	
	public void addAction(SectorAction act) {
		act.getSector().actions.add(act);
	}
	
	public void removeAction(SectorAction act) {
		act.getSector().actions.removeValue(act, true);
	}
	
	/** thing builders only apply loading from XML */
	public void registerThingBuilder(int type, ThingBuilder builder) {
		thingBuilders.put(type,builder);
	}
	
	/** SectorActionBuilders handle the creation of sector actions */
	public void registerSectorActionBuilder(int type, SectorActionBuilder builder) {
		actionBuilders.put(type, builder);
	}
	
	/** Override to create your own special sidedefs, ie, randomly offset cave walls*/
	protected SideDef createSideDef(int tag, XmlReader.Element elem, ResourceResolver reso) {
		SideDef ret = new SideDef(elem,reso);
		return ret;
	}
	
	/** Override to create your own Sectors as needed */
	protected Sector createSector(int tag, XmlReader.Element elem, ResourceResolver reso, Vector2 offset, Vector2 scale, float vScale) {
		Sector s = new Sector(elem,reso,offset,scale,vScale);
		return s;
	}
	
	/** You can override to provide your own LineDefs, probably for physics reasons */
	protected LineDef createLineDef(int tag, XmlReader.Element elem, Vector2 offset, Vector2 scale, float vScale) {
		LineDef ret = new LineDef(elem,offset,scale,vScale);
		return ret;
	}
	
	protected Thing createThing(int type, int tag, XmlReader.Element elem, Vector2 offset, Vector2 scale, float vScale) {
		if (this.thingBuilders.containsKey(type))
			return thingBuilders.get(type).createThing(type, elem.getInt("tag",0), elem);
		else
			return new Thing(elem);
	}
	
	/** when merging the merged map must be fully loaded and reconstructed, but without geometry built */
	public void merge(DoomMap merging, int[] mergeLineTags) {
		int index = 0;
		IntIntMap mergeLinesTable = new IntIntMap();
		IntIntMap mergeSidesTable = new IntIntMap();
		IntIntMap mergeSectorsTable = new IntIntMap();
		
		//find next line index
		for (LineDef line : this.lines.values()) {
			index = Math.max(index, line.index);
		}
		++index;
		
		//reindex merge source's lines
		for (LineDef line : merging.lines.values()) {
			int idx = line.index;
			line.index = ++index;
			mergeLinesTable.put(idx, line.index);
			for (int mergeLine : mergeLineTags) {
				if (idx == mergeLine) {
					
				}
			}
		}
		
		//find next side index
		index = 0;
		for (SideDef side : sides.values()) {
			index = Math.max(index, side.index);
		}
		++index;
		
		//reindex sides of source
		for (SideDef side : merging.sides.values()) {
			int idx = side.index;
			side.index = ++index;
			mergeSidesTable.put(idx, side.index);
		}
		
		index = 0;
		for (Sector s : sectors) {
			index = Math.max(index, s.index);
		}
		++index;
		
		//reindex sectors
		for (Sector s : merging.sectors) {
			int idx = s.index;
			s.index = ++index;
			mergeSectorsTable.put(idx, s.index);
		}
		
		//get next thing index
		index = 0;
		for (Thing th : things) {
			index = Math.max(index, th.index);
		}
		++index;
		
		for (Thing th : merging.things) {
			int idx = th.index;
			th.index = ++index;
		}
		
		/* Reconstruct dependencies */ {
			for (LineDef line : merging.lines.values()) {
				line.frontIndex = mergeSidesTable.get(line.frontIndex, line.frontIndex);
				line.backIndex = mergeSidesTable.get(line.backIndex, line.backIndex);
			}
			for (SideDef side : merging.sides.values()) {
				side.lineIndex = mergeLinesTable.get(side.lineIndex,side.lineIndex);
			}
			for (Sector sector : merging.sectors) {
				for (int i = 0; i < sector.sideIndexes.length; ++i) {
					sector.sideIndexes[i] = mergeSidesTable.get(sector.sideIndexes[i], sector.sideIndexes[i]);
				}
			}
		}
	}
}
