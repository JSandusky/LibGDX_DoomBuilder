package com.doom.collision;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.EdgeShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.doom.DoomMap;
import com.doom.Sector;
import com.doom.SideDef;

/** populates a Box2d world with static edge shapes, use the box ContactHandler and a ContactMultiplexer (of your own writing) */
public class Box2dBuilder {
	World world;
	final float sclFactor;
	
	BodyDef rigid;
	BodyDef blockMonsters;
	
	Array<Body> bodies = new Array<Body>();
	
	public Box2dBuilder(World world, float scl) {
		this.world = world;
		sclFactor = scl;
		
		rigid = new BodyDef();
		rigid.type = BodyDef.BodyType.StaticBody;
		
		blockMonsters = new BodyDef();
		blockMonsters.type = BodyDef.BodyType.StaticBody;
	}
	
	public Array<Body> getBodies() {
		return bodies;
	}
	
	public void populateWorld(DoomMap map) {
		for (Sector s : map.getSectors()) {
			for (SideDef sd : s.sides) {
				if (sd.line.flags.get("impassable",false)) {
					com.badlogic.gdx.physics.box2d.EdgeShape edge = new EdgeShape();
					edge.set(sd.line.a.x * sclFactor, sd.line.a.y * sclFactor, sd.line.b.x * sclFactor, sd.line.b.y * sclFactor);
					
					FixtureDef fdef = new FixtureDef();
					fdef.shape = edge;
					
					Body body = world.createBody(rigid);
					body.createFixture(fdef);
					body.setUserData(sd.line);
					bodies.add(body);
				}
			}
		}
	}
}
