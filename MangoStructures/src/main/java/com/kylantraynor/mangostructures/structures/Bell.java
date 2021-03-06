package com.kylantraynor.mangostructures.structures;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
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
		"[..x..-..x..][.xcx.-.xcx.][.xcx.-.xcx.][..c..-..c..]",
		"[.xxx.-.xxx.][.xcx.-.xcx.][.xcx.-.xcx.][..c..-..c..]",
		"[..x..-..x..][.xcx.-.xcx.][.xcx.-.xcx.][.xcx.-.xcx.][..c..-..c..]",
		"[.xxx.-.xxx.][.xcx.-.xcx.][.xcx.-.xcx.][.xcx.-.xcx.][..c..-..c..]",
		"[..x..-..x..][.xcx.-.xcx.][.xcx.-.xcx.][.xcx.-.xcx.][.xcx.-.xcx.][..c..-..c..]",
		"[.xxx.-.xxx.][.xcx.-.xcx.][.xcx.-.xcx.][.xcx.-.xcx.][.xcx.-.xcx.][..c..-..c..]",
		"[..x..-..x..][.xcx.-.xcx.][.xcx.-.xcx.][.xcx.-.xcx.][x.c.x-x.c.x][..c..-..c..]",
		"[.xxx.-.xxx.][.xcx.-.xcx.][.xcx.-.xcx.][.xcx.-.xcx.][x.c.x-x.c.x][..c..-..c..]",
		"[..x..-..x..][.xcx.-.xcx.][.xcx.-.xcx.][.xcx.-.xcx.][.xcx.-.xcx.][x.c.x-x.c.x][..c..-..c..]"
	};

	private static String sound = "bell";
	
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
	
	public int getMaxHeight(){
		int maxHeight = 0;
		for(String s : validShapes){
			maxHeight = Math.max(maxHeight, s.split("-").length - 1);
		}
		return maxHeight;
	}

	public void loadShape() {
		String clapper = "c";
		String bell = "x";
		String air = "0";
		String other = ".";
		int maxHeight = getMaxHeight();
		StringBuilder sb = new StringBuilder();
		for(int y = 1; y <= maxHeight; y++){
			StringBuilder sb1 = new StringBuilder();
			sb1.append("[");
			for(int x = -2; x <= 2; x++){
				switch(getLocation().clone().add(x, -y, 0).getBlock().getType()){
				case IRON_BLOCK: case GOLD_BLOCK:
					sb1.append(bell);
					break;
				case FENCE: case ACACIA_FENCE: case DARK_OAK_FENCE: case SPRUCE_FENCE: case JUNGLE_FENCE: case BIRCH_FENCE: case COBBLE_WALL:
					sb1.append(clapper);
					break;
				//case AIR:
				//	sb1.append(air);
				default:
					sb1.append(other);
					break;
				}
			}
			sb1.append("-");
			for(int z = -2; z <= 2; z++){
				switch(getLocation().clone().add(0, -y, z).getBlock().getType()){
				case IRON_BLOCK: case GOLD_BLOCK:
					sb1.append(bell);
					break;
				case FENCE: case DARK_OAK_FENCE: case SPRUCE_FENCE: case JUNGLE_FENCE: case BIRCH_FENCE: case COBBLE_WALL:
					sb1.append(clapper);
					break;
				default:
					sb1.append(other);
					break;
				}
			}
			sb1.append("]");
			if(sb1.toString().equalsIgnoreCase("[.....-.....]")){
				break;
			} else {
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
					if(!blocks.contains(b.getState()) && isBellBlock(b)){
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
		loadShape();
		if(!isValidShape()) return;
		float pitch = 2F - (((float) getIronAmount()) / 15.0F) - (((float) getBrassAmount()) / 20.0F);
		float volume = (float) ((2.0f - pitch) * 3.0F + ((float) getLocation().getY()) * 0.1F);
		MangoStructures.DEBUG(this.getName() + " is ringing with pitch: " + pitch + " and volume: " + volume + ".");
		for(Player player : Bukkit.getOnlinePlayers()){
			player.playSound(getLocation(), sound, volume, pitch);
			//player.playSound(getLocation(), Sound.BLOCK_ANVIL_PLACE, volume, pitch);
		}
		for(int i = 0; i < 8; i++){
			double randx = (Math.random() * 2) - 1;
			double randz = (Math.random() * 2) - 1;
			this.getLocation().getWorld().spawnParticle(Particle.NOTE, this.getLocation().clone().add(0.5 + randx, -(shape.split("-").length - 1), 0.5 + randz), 5);
		}
		//this.getLocation().getWorld().playEffect(getLocation().clone().add(0, -3, 0), Effect.RECORD_PLAY, 1);
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
		if(y1 < this.getLocation().getBlockY() - getMaxHeight() || y1 > this.getLocation().getBlockY()) return false;
		if(z1 < this.getLocation().getBlockZ() - 3 || z1 > this.getLocation().getBlockZ() + 3) return false;
		for(BlockState s : blocks){
			Location l = s.getLocation();
			if(l.getBlockX() == x1 && l.getBlockY() == y1 && l.getBlockZ() == z1){
				return true;
			}
		}
		return false;
	}

	public String getShape() {
		return shape;
	}

	public static void setSound(String s) {
		sound  = s;
	}

	public static Bell get(String name) {
		for(Bell b : bells){
			if(b.getName() != null){
				if(b.getName().equalsIgnoreCase(name)){
					return b;
				}
			}
		}
		return null;
	}

	public static Bell getAt(Location location) {
		for(Bell b : bells){
			if(!b.getLocation().getWorld().getName().equals(location.getWorld().getName())) continue;
			if(b.getLocation().getBlockX() != location.getBlockX()) continue;
			if(b.getLocation().getBlockY() != location.getBlockY()) continue;
			if(b.getLocation().getBlockZ() != location.getBlockZ()) continue;
			return b;
		}
		return null;
	}
	
}
