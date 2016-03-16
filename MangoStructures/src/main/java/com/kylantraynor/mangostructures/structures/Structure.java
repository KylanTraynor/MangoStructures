package com.kylantraynor.mangostructures.structures;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.block.BlockState;

public class Structure {
	private Location location;
	private List<BlockState> blocks = new ArrayList<BlockState>();
  
	public Structure(Location l) {
		setLocation(l);
	}
  
	public Location getLocation() { return this.location; }
	public void setLocation(Location location) { this.location = location; }
  
	public List<BlockState> getBlocks() { return this.blocks; }
	public void setBlocks(List<BlockState> blocks) { this.blocks = blocks; }
}
