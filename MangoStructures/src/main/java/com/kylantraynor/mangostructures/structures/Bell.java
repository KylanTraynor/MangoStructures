package com.kylantraynor.mangostructures.structures;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import com.kylantraynor.mangostructures.MangoStructures;

public class Bell extends Structure{

	enum BlockType{
		BRASS,
		IRON,
		CLAPPER,
		UNDEFINED
	}
	
	static String[] validShapes = {
		"\\[.0x0.\\-.0x0.\\]\\[.0c0.\\-.0c0.\\]",
		"\\[.0x0.\\-.0x0.\\]\\[.0x0.\\-.0x0.\\]\\[.0c0.\\-.0c0.\\]",
		"\\[.0x0.\\-.0x0.\\]\\[0xcx0\\-0xcx0\\]\\[.0c0.\\-.0c0.\\]",
		"\\[0xxx0\\-0xxx0\\]\\[0xcx0\\-0xcx0\\]\\[.0c0.\\-.0c0.\\]",
		"\\[.0x0.\\-.0x0.\\]\\[0xcx0\\-0xcx0\\]\\[0xcx0\\-0xcx0\\]\\[.0c0.\\-.0c0.\\]",
		"\\[0xxx0\\-0xxx0\\]\\[0xcx0\\-0xcx0\\]\\[0xcx0\\-0xcx0\\]\\[.0c0.\\-.0c0.\\]",
		"\\[.0x0.\\-.0x0.\\]\\[0xcx0\\-0xcx0\\]\\[0xcx0\\-0xcx0\\]\\[0xcx0\\-0xcx0\\]\\[.0c0.\\-.0c0.\\]",
		"\\[0xxx0\\-0xxx0\\]\\[0xcx0\\-0xcx0\\]\\[0xcx0\\-0xcx0\\]\\[0xcx0\\-0xcx0\\]\\[.0c0.\\-.0c0.\\]",
		"\\[.0x0.\\-.0x0.\\]\\[0xcx0\\-0xcx0\\]\\[0xcx0\\-0xcx0\\]\\[0xcx0\\-0xcx0\\]\\[0xcx0\\-0xcx0\\]\\[.0c0.\\-.0c0.\\]",
		"\\[0xxx0\\-0xxx0\\]\\[0xcx0\\-0xcx0\\]\\[0xcx0\\-0xcx0\\]\\[0xcx0\\-0xcx0\\]\\[0xcx0\\-0xcx0\\]\\[.0c0.\\-.0c0.\\]",
		"\\[.0x0.\\-.0x0.\\]\\[0xcx0\\-0xcx0\\]\\[0xcx0\\-0xcx0\\]\\[0xcx0\\-0xcx0\\]\\[x0c0x\\-x0c0x\\]\\[.0c0.\\-.0c0.\\]",
		"\\[0xxx0\\-0xxx0\\]\\[0xcx0\\-0xcx0\\]\\[0xcx0\\-0xcx0\\]\\[0xcx0\\-0xcx0\\]\\[x0c0x\\-x0c0x\\]\\[.0c0.\\-.0c0.\\]"
	};
	
	private String shape = "";
	private BlockType[][][] blockShape;
	private List<Block> blocks;
	private boolean inRefractoryPeriod = false;

	private String name;
	
	public Bell(Location l) {
		super(l);
		loadShape();
	}

	private void loadShape() {
		String clapper = "c";
		String bell = "x";
		String air = "0";
		int maxHeight = 0;
		for(String s : validShapes){
			maxHeight = Math.max(maxHeight, s.split("-").length - 1);
		}
		StringBuilder sb = new StringBuilder();
		for(int y = 1; y <= maxHeight; y++){
			StringBuilder sb1 = new StringBuilder();
			sb1.append("[");
			for(int x = -2; x <= 2; x++){
				switch(getLocation().clone().add(x, -y, 0).getBlock().getType()){
				case IRON_BLOCK: case GOLD_BLOCK:
					sb1.append(bell);
				case FENCE: case DARK_OAK_FENCE: case SPRUCE_FENCE: case JUNGLE_FENCE: case BIRCH_FENCE: case COBBLE_WALL:
					sb1.append(clapper);
				default:
					sb1.append(air);
				}
			}
			sb1.append("-");
			for(int z = -2; z <= 2; z++){
				switch(getLocation().clone().add(0, -y, z).getBlock().getType()){
				case IRON_BLOCK: case GOLD_BLOCK:
					sb1.append(bell);
				case FENCE: case DARK_OAK_FENCE: case SPRUCE_FENCE: case JUNGLE_FENCE: case BIRCH_FENCE: case COBBLE_WALL:
					sb1.append(clapper);
				default:
					sb1.append(air);
				}
			}
			sb1.append("]");
			if(!sb1.toString().equalsIgnoreCase("[00000-00000]")){
				sb.append(sb1.toString());
			}
		}
		shape = sb.toString();
		if(isValidShape()){
			Location l = getLocation().clone().add(0, -1, 0);
			blocks = new ArrayList<Block>();
			blocks.add(l.getBlock());
			addBlocksAround(l);
		}
	}

	private void addBlocksAround(Location l) {
		for(int y = 0; y >= -1; y--){
			for(int x = -1; x <= 1; x++){
				for(int z = -1; z <= 1; z++){
					Block b = l.getWorld().getBlockAt(l.getBlockX() + x, l.getBlockY() + y, l.getBlockZ() + z);
					if(!blocks.contains(b) && (b.getType() == Material.GOLD_BLOCK || b.getType() == Material.IRON_BLOCK)){
						blocks.add(b);
						addBlocksAround(b.getLocation());
					}
				}
			}
		}
	}

	public boolean isValidShape(){
		for(String s : validShapes){
			if(Pattern.matches(s, shape)) return true;
		}
		return false;
	}
	
	public int getBrassAmount(){
		int count = 0;
		for(Block b : blocks){
			if(b.getType() == Material.GOLD_BLOCK) count++;
		}
		return count;
	}
	
	public int getIronAmount(){
		int count = 0;
		for(Block b : blocks){
			if(b.getType() == Material.IRON_BLOCK) count++;
		}
		return count;
	}
	
	public void ring(){
		if(inRefractoryPeriod){ return;}
		float pitch = 2F - (((float) getIronAmount()) / 15.0F) - (((float) getBrassAmount()) / 20.0F);
		float volume = (float) ((2.0f - pitch) * 3.0F + ((float) getLocation().getY()) * 0.1F);
		for(Player player : Bukkit.getOnlinePlayers()){
			MangoStructures.DEBUG(this.getName() + " is ringing with pitch: " + pitch + " and volume: " + volume + ".");
			player.playSound(getLocation(), "bell01", volume, pitch);
		}
		this.getLocation().getWorld().playEffect(getLocation().clone().add(0, -3, 0), Effect.RECORD_PLAY, 1);
		inRefractoryPeriod = true;
	}

	private String getName() {
		return this.name;
	}
	
}
