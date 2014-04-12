package com.doom.collision;

import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.doom.LineDef;

public class ContactHandler implements ContactListener {

	@Override
	public void beginContact(Contact arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void endContact(Contact arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void postSolve(Contact arg0, ContactImpulse arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void preSolve(Contact arg0, Manifold arg1) {
		Object left = arg0.getFixtureA().getBody().getUserData();
		Object right = arg0.getFixtureB().getBody().getUserData();
		
		if (left instanceof LineDef) {
			
		} else if (right instanceof LineDef) {
			
		}
	}

}
