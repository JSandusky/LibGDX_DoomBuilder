package com.doom.wad;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;

public class Wad {
	String fileName;
	FileHandle fileHandle;
	
	WadType type;
	int lumpCt;
	int lumpOfs;
	
	Array<Lump> lumps;
	
	public Wad(FileHandle file) {
		fileName = file.nameWithoutExtension();
		fileHandle = file;
		
		open();
	}
	
	void open() {
		
	}
	
	void buildHeader() {
		
	}
}
