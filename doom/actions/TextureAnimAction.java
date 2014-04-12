package com.doom.actions;

import com.badlogic.gdx.graphics.Texture;
import com.doom.Sector;
import com.doom.SectorAction;

/** animates the textures of the floor or ceiling or a sector */
public class TextureAnimAction extends SectorAction {
	Texture[] list;
	float frameTime;
	float runTime;
	int frame;
	boolean floor;
	
	public TextureAnimAction(Sector target, boolean floor, Texture[] imgs, float cycleTime) {
		super(target);
		frameTime = cycleTime/imgs.length;
		this.floor = floor;
		if (floor)
			getSector().setFloorTexture(imgs[0]);
		else
			getSector().setCeilingTexture(imgs[0]);
	}

	@Override
	public boolean update(float td) {
		runTime += td;
		if (runTime > frameTime) {
			runTime = 0;
			frame++;
			if (frame > list.length - 1)
				frame = 0;
			if (floor)
				getSector().setFloorTexture(list[frame]);
			else
				getSector().setCeilingTexture(list[frame]);
		}
		return false;
	}

}
