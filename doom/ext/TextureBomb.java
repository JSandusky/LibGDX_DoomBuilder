package com.doom.ext;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

/** will texture bomb whatever we're drawing with */
public class TextureBomb {
	Array<Vector3> pts = new Array<Vector3>();
	transient TextureRegion region;
	static ShaderProgram simpleShader;
	static ShaderProgram dripShader;
	static ShaderProgram puddleShader;
	
	transient ShaderProgram shader;
	
	public TextureBomb(TextureRegion region, ShaderProgram shader) {
		
	}
	
	public void begin() {
		
	}
	
	
}
