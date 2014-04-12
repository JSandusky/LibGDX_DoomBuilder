package com.doom;

import java.util.Comparator;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Frustum;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pools;
import com.doom.render.QuadBatch;
import com.doom.vis.FrustumClipper;

public class DoomMapRenderer {
	Camera camera;
	QuadBatch qb;
	ShaderProgram shader;
	
	int drawCallEstimate = 0;
	
	boolean drawCeilings = true;
	boolean drawFloors = true;
	
	Environment environment;
	Array<Sector> floorQueue = new Array<Sector>();
	Array<Sector> ceilingQueue = new Array<Sector>();
	
	Array<QuadCmd> queuedQuads = new Array<QuadCmd>();
	
	public DoomMapRenderer() {
		qb = new QuadBatch();
		environment = new Environment();
	}
	
	static class QuadCmd {
		Texture tex;
		float[] vertices;
		String name;
		
		static QuadCmd obtain() {
			return Pools.obtain(QuadCmd.class);
		}
		public void free() {
			Pools.free(this);
		}
	}
	
	Comparator<Sector> floorComp = new Comparator<Sector>() {
		@Override
		public int compare(Sector arg0, Sector arg1) {
			return arg0.floorTextureName.compareTo(arg1.floorTextureName);
		}
	};
	
	Comparator<Sector> ceilComp = new Comparator<Sector>() {
		@Override
		public int compare(Sector arg0, Sector arg1) {
			return arg0.ceilingTextureName.compareTo(arg1.ceilingTextureName);
		}
	};
	
	Comparator<QuadCmd> quadComp = new Comparator<QuadCmd>() {

		@Override
		public int compare(QuadCmd o1, QuadCmd o2) {
			return o1.name.compareTo(o2.name);
		}
	};
	
	public void setCamera(Camera cam) {
		camera = cam;
		
		createShader();
	}
	
	void createShader() {
		if (shader != null)
			return;
		String vertexShader = 
				"attribute vec4 a_position;\n" 
				+ "attribute vec2 a_texCoords;\n"
				+ "attribute vec4 a_color;\n"
				+ "uniform mat4 u_worldView;\n"
                + "varying vec2 v_texCoord;\n"
                + "varying vec4 v_color;\n" 
                + "void main()\n" 
                + "{\n"
                + "   gl_Position = u_worldView * a_position;\n" 
                + "   v_texCoord = a_texCoords;  v_color = a_color;\n" 
                + "}\n";

        String fragmentShader = 
        		"#ifdef GL_ES\n" 
				+ "precision mediump float;\n" 
				+ "#endif\n"
                + "varying vec2 v_texCoord; varying vec4 v_color;\n" 
				+ "uniform sampler2D s_texture;\n" 
        		+ "void main()\n"
                + "{\n"
                + "  gl_FragColor = texture2D( s_texture, v_texCoord ) * v_color;\n"
                + "}\n";
        shader = new ShaderProgram(vertexShader, fragmentShader);
        if (!shader.isCompiled()) {
        	Gdx.app.log("Shader failure", shader.getLog());
        }
	}
	
	static Vector2 tmpRel = new Vector2();
	
	public void render(DoomMap map) {
		render(map,null);
	}
	
	Frustum frustum = new Frustum();
	public void render(DoomMap map, Sector sector) {
		tmpRel.x = camera.position.x;
		tmpRel.y = camera.position.z;
		
		FrustumClipper.set(frustum, camera.frustum);
		
		if (sector != null) {
			sector.draw(this, frustum, tmpRel,0);
		} else {
			for (Sector s : map.sectors) {
				s.draw(this, null, tmpRel,-128); //null frustum, draw everything like a boob
			}
		}
	}
	
	public void flush() {
		floorQueue.sort(floorComp);
		ceilingQueue.sort(ceilComp);
		queuedQuads.sort(quadComp);
		
		//Render floors and ceilings, then walls via QuadBatch
		Texture lastTexture = null;
		
		Gdx.graphics.getGLCommon().glDepthMask(true);
		
		drawCallEstimate = 0;
		
		for (Sector s : floorQueue) {
			Texture newTex = s.floorTexture;
			if (newTex != lastTexture) {
				boolean wasNull = newTex == null;
				newTex.bind(0);
				lastTexture = newTex;
				if (wasNull) {
					shader.begin();
					shader.setUniformi("s_texture", 0);
					shader.setUniformMatrix("u_worldView", camera.combined);
				} else {
					//drawCallEstimate++;
					shader.end();
					shader.begin();
					shader.setUniformi("s_texture", 0);
					shader.setUniformMatrix("u_worldView", camera.combined);
				}
			}
			s.floorMesh.bind(shader);
			s.floorMesh.render(shader,GL20.GL_TRIANGLES);
		}
		shader.end();
		
		lastTexture = null;
		for (Sector s : ceilingQueue) {
			Texture newTex = s.ceilingTexture;
			if (newTex != lastTexture) {
				final boolean wasNull = newTex == null;
				newTex.bind(0);
				lastTexture = newTex;
				if (wasNull) {
					shader.begin();
					shader.setUniformi("s_texture", 0);
					shader.setUniformMatrix("u_worldView", camera.combined);
				} else {
					//drawCallEstimate++;
					shader.end();
					shader.begin();
					shader.setUniformi("s_texture", 0);
					shader.setUniformMatrix("u_worldView", camera.combined);
				}
			}
			s.ceilingMesh.bind(shader);
			s.ceilingMesh.render(shader,GL20.GL_TRIANGLES);
		}
		shader.end();
		
		qb.setProjectionMatrix(camera.combined);
		qb.begin();
		lastTexture = null;
		for (QuadCmd cmd : queuedQuads) {
			if (cmd.tex != lastTexture) {
				qb.flush();
			}
			//lastTexture = cmd.tex;
			qb.draw(cmd.tex, cmd.vertices, 0, cmd.vertices.length);
		}
		qb.end();
		drawCallEstimate += qb.renderCalls;
		for (QuadCmd cmd : queuedQuads)
			cmd.free();
		queuedQuads.clear();
		floorQueue.clear();
		ceilingQueue.clear();
	}
	
	public boolean queue(Sector s, boolean floor) {
		if (floor) {
			if (!floorQueue.contains(s, true)) {
				floorQueue.add(s);
				return true;
			}
		} else {
			if (!ceilingQueue.contains(s,true)) {
				ceilingQueue.add(s);
				return true;
			}
		} return false;
	}
	
	public void queue(float[] verts, Texture tex, String name) {
		QuadCmd cmd = QuadCmd.obtain();
		cmd.tex = tex;
		cmd.name = name;
		cmd.vertices = verts;
		queuedQuads.add(cmd);
	}
	
	public boolean isDrawingFloors() {return drawFloors;}
	public boolean isDrawingCeilings() {return drawCeilings;}
	public void setDrawFloors(boolean value) {drawFloors = value;}
	public void setDrawCeilings(boolean value) {drawCeilings = value;}
}
