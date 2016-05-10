package com.kylantraynor.mangostructures.structures;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;

public class Kiln extends Structure{
	
	private String shape;
	private Location netherrackLocation;
	private Chimney chimney;
	
	public Kiln(Location l){
		super(l);
		loadShape();
	}
	
	public void update(){
		if(chimney == null){
			chimney = new Chimney(getChimneyStartLocation());
		}
		if(isActive()){
			chimney.puff(3);
		}
	}
	
	private boolean isActive() {
		return getNetherrackLocation().getBlock().getRelative(BlockFace.UP).getType() == Material.FIRE;
	}

	public boolean isInside(Location l){
		int minX = getLocation().getBlockX() - 1;
		int minY = getLocation().getBlockY() - 5;
		int minZ = getLocation().getBlockZ() - 1;
		int maxX = getLocation().getBlockX() + 1;
		int maxY = getLocation().getBlockY() + 1;
		int maxZ = getLocation().getBlockZ() + 1;
		return(l.getBlockX() <= maxX && l.getBlockX() >= minX &&
				l.getBlockY() <= maxY && l.getBlockY() >= minY &&
				l.getBlockZ() <= maxZ && l.getBlockZ() >= minZ);
	}
	
	public Location getChimneyStartLocation(){
		return getLocation().clone().add(0,1,0);
	}
	
	public Location getNetherrackLocation(){
		return getLocation().clone().add(0,-4,0);
	}
	
	private void loadShape(){
		// GetLocation() must return the location of the chest
		// Gets the height of the iron fence
		int maxHeight = getLocation().getBlockY() + 1;
		int minHeight = getLocation().getBlockY() - 5;
		// Starts at the position of the chest
		Location currentLocation = getLocation().clone();
		shape = "";
		for(int y = minHeight; y <= maxHeight; y++){
			for(int x = getLocation().getBlockX() - 1; x <= getLocation().getBlockX() + 1; x++){
				for(int z = getLocation().getBlockZ() - 1; z <= getLocation().getBlockZ() + 1; z++){
					currentLocation.setX(x);
					currentLocation.setY(y);
					currentLocation.setZ(z);
					switch(currentLocation.getBlock().getType()){
					case BRICK:
						shape += "b";
						break;
					case IRON_TRAPDOOR:
						shape += "i";
						break;
					case CHEST:
						shape += "c";
						break;
					case IRON_FENCE:
						shape += "f";
						break;
					case NETHERRACK:
						shape += "n";
						netherrackLocation = currentLocation.clone();
						break;
					default:
						shape += " ";
						break;
					}
				}
				shape += ",";
			}
			shape += "\n";
		}
	}
	
	
	
	public boolean isValidShape(){
		if(shape.matches("bbb,bbb,bbb,\nbbb,bnb,bbb,\nb[bf]b,[bf].[bf],b[bf]b,\nbbb,bib,bbb,\nb[bi]b,[bi]i[bi],b[bi]b,\nbbb,bcb,bbb,\n.b.,bfb,.b.,\n")){
			return true;
		}
		return false;
	}
}