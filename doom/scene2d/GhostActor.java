package com.doom.scene2d;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Array;
import com.doom.Thing;
import com.doom.things.DecorationThing;
import com.doom.things.MonsterThing;
import com.doom.things.PickupThing;
import com.doom.things.PlayerThing;
import com.doom.things.ProjectileThing;

public class GhostActor extends Actor {
	Thing thing;
	Body body;
	
	static float ToPhysics = 1/30f;
	static float ToGfx = 30f;
	
	public static void setPhysicsFraction(float frac) {
		ToPhysics = 1/frac;
		ToGfx = frac;
	}
	
	public GhostActor(Thing thing) {
		this.thing = thing;
		if (thing instanceof DecorationThing) {
			
		} else if (thing instanceof MonsterThing) {
			
			if (thing instanceof PlayerThing) {
				
			}
		} else if (thing instanceof PickupThing) {
			
		} else if (thing instanceof ProjectileThing) {
			
		}
	}
	
	public Thing getThing() {
		return thing;
	}
	
	@Override
	public void act(float td) {
		if (body.getType() == BodyType.DynamicBody) {
			setPosition(body.getPosition().x*ToGfx,body.getPosition().y*ToGfx);
			setRotation(body.getAngle());
		}
		super.act(td);
	}
	
	public void createBody(World world, float radius) {
		BodyDef bdef = new BodyDef();
		
		if (thing instanceof DecorationThing)
			bdef.type = BodyDef.BodyType.StaticBody;
		else
			bdef.type = BodyDef.BodyType.DynamicBody;
		
		FixtureDef fdef = new FixtureDef();
		CircleShape cs = new CircleShape();
		cs.setRadius(radius);
		fdef.shape = cs;
		
		body = world.createBody(bdef);
		body.createFixture(fdef);
		body.setTransform(new Vector2(getX()*ToPhysics, getY()*ToPhysics), 0);
	}
	
	public void destroyBody(Array<Body> holder) {
		holder.add(body);
		
		
	}
	
	public boolean shouldCollideWith(GhostActor other) {
		if (other.getThing().getClass().equals(getThing().getClass()))
			return true;
		//projectiles collide with monsters
		if (other.getThing().getClass() == MonsterThing.class && getThing().getClass() == ProjectileThing.class)
			return true;
		return false;
	}
	
	@Override
	public void setX(float x) {
		super.setX(x);
		updatePosition();
	}
	
	@Override
	public void setY(float y) {
		super.setY(y);
		updatePosition();
	}
	
	@Override
	public void setPosition(float x, float y) {
		super.setPosition(x, y);
		updatePosition();
	}
	
	@Override
	public void translate(float x, float y) {
		super.translate(x, y);
		updatePosition();
	}
	
	void updatePosition() {
		thing.position.set(getX(),getY());
		thing.doomFacing = (int)getRotation();
		
		if (body.getType() != BodyDef.BodyType.StaticBody) {
			body.setTransform(getX()*ToPhysics, getY()*ToPhysics, getRotation());
		}
	}
	
	@Override
	public void setRotation(float rot) {
		super.setRotation(rot);
		updatePosition();
	}
	
	@Override
	public void rotate(float amountInDegrees) {
		super.rotate(amountInDegrees);
		updatePosition();
	}
}
