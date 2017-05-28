package com.kylantraynor.mangostructures.structures;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.kylantraynor.mangostructures.MangoStructures;

public class Bell extends Structure{

	private static ArrayList<Bell> bells = new ArrayList<Bell>();
	
	enum BlockType{
		BRASS,
		IRON,
		CLAPPER,
		UNDEFINED
	}
	
	static String[] validShapes = {
		"[..x..-..x..][..c..-..c..]",
		"[..x..-..x..][..x..-..x..][..c..-..c..]",
		"[..x..-..x..][.xcx.-.xcx.][..c..-..c..]",
		"[.xxx.-.xxx.][.xcx.-.xcx.][..c..-..c..]",
		"[..x..-..x..][.xcx.-.xcx.][0xcx0-0xcx0][.0c0.-.0c0.]",
		"[.xxx.-.xxx.][.xcx.-.xcx.][0xcx0-0xcx0][.0c0.-.0c0.]",
		"[..x..-..x..][.xcx.-.xcx.][0xcx0-0xcx0][0xcx0-0xcx0][.0c0.-.0c0.]",
		"[.xxx.-.xxx.][.xcx.-.xcx.][0xcx0-0xcx0][0xcx0-0xcx0][.0c0.-.0c0.]",
		"[..x..-..x..][.xcx.-.xcx.][0xcx0-0xcx0][0xcx0-0xcx0][0xcx0-0xcx0][.0c0.-.0c0.]",
		"[.xxx.-.xxx.][.xcx.-.xcx.][0xcx0-0xcx0][0xcx0-0xcx0][0xcx0-0xcx0][.0c0.-.0c0.]",
		"[..x..-..x..][.xcx.-.xcx.][0xcx0-0xcx0][0xcx0-0xcx0][x0c0x-x0c0x][.0c0.-.0c0.]",
		"[.xxx.-.xxx.][.xcx.-.xcx.][0xcx0-0xcx0][0xcx0-0xcx0][x0c0x-x0c0x][.0c0.-.0c0.]"
	};
	
	private String shape = "";
	private BlockType[][][] blockShape;
	private List<BlockState> blocks;
	private boolean inRefractoryPeriod = false;

	private String name;
	
	public Bell(Location l) {
		super(l);
		loadShape();
		if(isValidShape()){
			bells.add(this);
		}
	}

	private void loadShape() {
		String clapper = "c";
		String bell = "x";
		String air = "0";
		String other = ".";
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
				case FENCE: case ACACIA_FENCE: case DARK_OAK_FENCE: case SPRUCE_FENCE: case JUNGLE_FENCE: case BIRCH_FENCE: case COBBLE_WALL:
					sb1.append(clapper);
				//case AIR:
				//	sb1.append(air);
				default:
					sb1.append(other);
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
			blocks = new ArrayList<BlockState>();
			blocks.add(l.getBlock().getState());
			addBlocksAround(l);
		}
	}

	private void addBlocksAround(Location l) {
		for(int y = 0; y >= -1; y--){
			for(int x = -1; x <= 1; x++){
				for(int z = -1; z <= 1; z++){
					Block b = l.getWorld().getBlockAt(l.getBlockX() + x, l.getBlockY() + y, l.getBlockZ() + z);
					if(!blocks.contains(b) && isBellBlock(b)){
						blocks.add(b.getState());
						addBlocksAround(b.getLocation());
					}
				}
			}
		}
	}
	
	public static boolean isBellBlock(Block b){
		if(b.getType() == Material.GOLD_BLOCK) return true;
		if(b.getType() == Material.IRON_BLOCK) return true;
		return isClapperMaterial(b.getType());
	}

	public boolean isValidShape(){
		for(String s : validShapes){
			if(s.equals(shape)) return true;
		}
		return false;
	}
	
	public int getBrassAmount(){
		int count = 0;
		for(BlockState b : blocks){
			if(b.getType() == Material.GOLD_BLOCK) count++;
		}
		return count;
	}
	
	public int getIronAmount(){
		int count = 0;
		for(BlockState b : blocks){
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
		BukkitRunnable r = new BukkitRunnable(){
			@Override
			public void run() {
				inRefractoryPeriod = false;
			}
		};
		r.runTaskLater(MangoStructures.currentInstance, 10L);
	}

	public String getName() {
		return this.name;
	}
	
	public void setName(String newName){
		this.name = newName;
	}

	public static boolean isClapperMaterial(Material type) {
		if(type == Material.FENCE) return true;
		if(type == Material.COBBLE_WALL) return true;
		if(type == Material.SPRUCE_FENCE) return true;
		if(type == Material.DARK_OAK_FENCE) return true;
		if(type == Material.BIRCH_FENCE) return true;
		if(type == Material.JUNGLE_FENCE) return true;
		if(type == Material.ACACIA_FENCE) return true;
		return false;
	}

	public static ArrayList<Bell> getBells() {
		return bells;
	}

	public static void setBells(ArrayList<Bell> bells) {
		Bell.bells = bells;
	}

	public boolean has(Block clickedBlock) {
		Location l1 = clickedBlock.getLocation();
		int x1 = l1.getBlockX();
		int y1 = l1.getBlockY();
		int z1 = l1.getBlockZ();
		if(x1 < this.getLocation().getBlockX() - 3 || x1 > this.getLocation().getBlockX() + 3) return false;
		if(y1 < this.getLocation().getBlockY() - 6 || y1 > this.getLocation().getBlockY()) return false;
		if(z1 < this.getLocation().getBlockZ() - 3 || z1 > this.getLocation().getBlockZ() + 3) return false;
		for(BlockState s : blocks){
			Location l = s.getLocation();
			if(l.getBlockX() == x1 && l.getBlockY() == y1 && l.getBlockZ() == z1){
				return true;
			}
		}
		return false;
	}
	
}
