package com.doom;

import java.io.Serializable;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.XmlReader;

/** Currently unused, however I may switch to lines being made of vertices
 * it could simplify some things, and with some tweaks to DoomBuilder could probably do full 3d segments more naturally
 * as well as make moving walls possible */
@Deprecated
public class Vertex implements Serializable, PostDeserialize {
	private static final long serialVersionUID = 1L;
	
	public int index;
	public Vector2 position;
	public Vector3 lowerBound;
	public Vector3 upperBound;
	
	int[] lineIndexes;
	public transient LineDef[] lines;
	
	public Vertex(XmlReader.Element elem, Vector2 translate, Vector2 scl, float vScale) {
		index = elem.getInt("idx");
		position = new Vector2(elem.getFloat("x"),elem.getFloat("y")*-1);
		position.add(translate);
		position.scl(scl);
		
		String[] linesParts = elem.get("lines","").split(",");
		lineIndexes = new int[linesParts.length];
		lines = new LineDef[linesParts.length];
		for (int i = 0; i < linesParts.length; ++i) {
			lineIndexes[i] = Integer.parseInt(linesParts[i]);
			lines[i] = new LineDef(lineIndexes[i]);
		}
	}
	
	public void rebuild(DoomMap map) {
		for (int i = 0; i < lines.length; ++i) {
			lines[i] = map.lines.get(lines[i].index);
		}
	}

	@Override
	public void postDeserialize(DoomMap map, ResourceResolver res) {
		rebuild(map);
	}
}
