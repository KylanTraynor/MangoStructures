package com.kylantraynor.mangostructures.structures;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.material.Furnace;
import org.bukkit.material.Stairs;
import org.bukkit.material.Step;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.kylantraynor.mangostructures.MangoStructures;

public class Chimney extends Structure {
	private Location emiterLocation;

	static enum ChimneyMaterial {
		BRICK,  STONEBRICK,  COBBLESTONE, NETHERBRICK,  FURNACE, WALL, POT;
		private ChimneyMaterial() {}
	}

	static enum SmokeType {
		BLACK,  WHITE,  GREEN,  RED;
		private SmokeType() {}
	}

	public Chimney(Location l) {
		super(l);
		updateConduit();
	}

	public Location getEmiterLocation() {
		if(getBlocks().size() == 0){
			return getLocation().add(0.5,0.5,0.5);
		} else {
			/*if(getBlocks().get(getBlocks().size() - 1).getLocation().add(0, 1, 0).getBlock().getLightFromSky() <= 10){
				return getLocation().add(0.5,0.5,0.5);
			} else {*/
				return getBlocks().get(getBlocks().size() - 1).getLocation().add(0.5, 1.5, 0.5);
			//}
		}
	}

	public boolean isSafe() {
		if (!getBlocks().isEmpty()){
			if ((getLocation().getBlock().getLightFromSky() >= 14)) {
				return true;
			}
			if (getBlocks().size() > 1) {
				return true;
			}
			return false;
		} else if ((getLocation().getBlock().getLightFromSky() >= 14)) {
			return true;
		}
		return false;
	}
	
	public void updateConduit(){
		Block currentBlock = getLocation().getBlock();
		List<BlockState> list = new ArrayList<BlockState>();
		while((currentBlock = getNextBlock(currentBlock)) != null){
			list.add(currentBlock.getState());
		}
		setBlocks(list);
	}

	private boolean isChimneyMaterial(Material m){
		return getChimneyMaterial(m) != null;
	}
	
	private ChimneyMaterial getChimneyMaterial(Material m){
		switch (m){
		case COBBLESTONE: case COBBLESTONE_STAIRS: 
			return ChimneyMaterial.COBBLESTONE;
		case NETHER_BRICK: case NETHER_BRICK_STAIRS:
			return ChimneyMaterial.NETHERBRICK;
		case SMOOTH_BRICK: case SMOOTH_STAIRS: 
			return ChimneyMaterial.STONEBRICK;
		case BRICK: case BRICK_STAIRS: 
			return ChimneyMaterial.BRICK;
		case FURNACE: case BURNING_FURNACE: 
			return ChimneyMaterial.FURNACE;
		case COBBLE_WALL:
			return ChimneyMaterial.WALL;
		case FLOWER_POT:
			return ChimneyMaterial.POT;
		}
		return null;
	}
	
	private Block getNextBlock(Block b){
		
		BlockFace out = getOutput(b.getState());
		if(out == null) return null;
		if(!isChimneyMaterial(b.getRelative(out).getType())) return null;
		BlockFace in = getInput(b.getRelative(out).getState());
		
		if(hasValidConnection(out, in)){
			return b.getRelative(out);
		} else {
			return null;
		}
	}
	
	private boolean hasValidConnection(BlockFace from, BlockFace to){
		if(to == null) return true;
		switch(to){
		case NORTH:
			return from == BlockFace.SOUTH;
		case SOUTH:
			return from == BlockFace.NORTH;
		case EAST:
			return from == BlockFace.WEST;
		case WEST:
			return from == BlockFace.EAST;
		case DOWN:
			return from == BlockFace.UP;
		case UP:
			return from == BlockFace.DOWN;
		}
		return false;
	}
	
	private BlockFace getInput(BlockState state){
		if (isStairs(state.getType())){
			Stairs stairs = (Stairs)state.getData();
			if (!stairs.isInverted()) {
				return BlockFace.DOWN;
			}
			return stairs.getFacing().getOppositeFace();
		}
		if(state.getType() == Material.COBBLE_WALL){
			return BlockFace.DOWN;
		} else if(state.getType() == Material.FLOWER_POT){
			return BlockFace.DOWN;
		}
		return null;
	}
	
	private boolean isStairs(Material m){
		if(m == Material.COBBLESTONE_STAIRS) return true;
		if(m == Material.SMOOTH_STAIRS) return true;
		if(m == Material.BRICK_STAIRS) return true;
		if(m == Material.NETHER_BRICK_STAIRS) return true;
		return false;
	}
	
	private BlockFace getOutput(BlockState state){
		if (isStairs(state.getType())){
			Stairs stairs = (Stairs)state.getData();
			if (stairs.isInverted()) {
				return BlockFace.UP;
			}
			return stairs.getFacing().getOppositeFace();
		}
		if ((state.getData() instanceof Furnace)){
			Furnace fr = (Furnace)state.getData();
			if(state.getBlock().getRelative(BlockFace.UP).getType() == Material.COBBLE_WALL){
				return BlockFace.UP;
			}
			switch (fr.getFacing()){
			case NORTH: 
				return BlockFace.SOUTH;
			case SOUTH: 
				return BlockFace.NORTH;
			case EAST: 
				return BlockFace.WEST;
			case WEST: 
				return BlockFace.EAST;
			}
			return BlockFace.UP;
		} else if(state.getType() == Material.FLOWER_POT){
			return null;
		}
		return BlockFace.UP;
	}
	
	public boolean isSafe(Player p){
		if(getEmiterLocation().getBlock().getLightFromSky() >= 14 || p.getLocation().getBlock().getLightFromSky() >= 14){
			return true;
		} else {
			if(getEmiterLocation().distance(p.getLocation()) < 5){
				return false;
			} else {
				return true;
			}
		}
	}

	public void puff(){
		puff(1);
	}
	
	public void puff(int intensity){
		if(!MangoStructures.useChimneys) return;
		for(Player p : Bukkit.getServer().getOnlinePlayers()){
			if(!p.getLocation().getWorld().equals(getEmiterLocation().getWorld())) continue;
			if(p.getLocation().distance(getEmiterLocation()) < 255){
				sendSmoke(p, intensity);
				if(!isSafe(p)){
					p.damage(1);
					if(p.getLocation().distance(getEmiterLocation()) < 2 * intensity){
						p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 20 * 2, 1));
					}
				}
				//p.spawnParticle(Particle.SMOKE_LARGE, getEmiterLocation(), 20, 0.5, 0.5, 0.5, 0.5, BlockFace.UP);
				//getEmiterLocation().getWorld().playEffect(getEmiterLocation(), Effect.LARGE_SMOKE, 0, 200);
			}
		}
	}
	
	private void sendSmoke(Player p, int intensity){
		PacketContainer smoke = MangoStructures.protocolManager.createPacket(PacketType.Play.Server.WORLD_PARTICLES);
		smoke.getIntegers().write(0, 12);
		smoke.getBooleans().write(0, true);
		smoke.getFloat().write(0,  (float) getEmiterLocation().getX()).
		write(1, (float) getEmiterLocation().getY() + (0.25f * intensity)).
		write(2, (float) getEmiterLocation().getZ()).
		write(3, 0.25f * intensity).
		write(4, 0.5f * intensity).
		write(5, 0.25f * intensity);
		switch(getSmokeColor()){
		case BLACK:
			smoke.getFloat().write(6, 0f);
			break;
		default:
			break;
		}
		smoke.getIntegers().write(0,  30 * (intensity * intensity));
		try {
			MangoStructures.protocolManager.sendServerPacket(p, smoke);
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private SmokeType getSmokeColor(){
		Block furnaceBlock = getLocation().getBlock();
		BlockState state = furnaceBlock.getState();
		if(!(state instanceof Furnace)) return SmokeType.WHITE;
		org.bukkit.block.Furnace furnace = (org.bukkit.block.Furnace) state;
		if(furnace.getInventory().getSmelting() == null) return SmokeType.WHITE;
		switch (furnace.getInventory().getSmelting().getType()){
		case IRON_ORE: case GOLD_ORE: 
			return SmokeType.BLACK;
		default:
			return SmokeType.WHITE;
		}
	}
}