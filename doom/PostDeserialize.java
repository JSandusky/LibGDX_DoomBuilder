package com.doom;

/** Something that needs to be reconstructed after deserialization */
public interface PostDeserialize {
	public void postDeserialize(DoomMap map, ResourceResolver res);
}
