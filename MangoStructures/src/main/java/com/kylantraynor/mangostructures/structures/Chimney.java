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
		BRICK,  STONEBRICK,  COBBLESTONE,  FURNACE;
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
			return getBlocks().get(getBlocks().size() - 1).getLocation().add(0.5, 1.5, 0.5);
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
	
	private void updateConduit(){
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
		case SMOOTH_BRICK: case SMOOTH_STAIRS: 
			return ChimneyMaterial.STONEBRICK;
		case BRICK: case BRICK_STAIRS: 
			return ChimneyMaterial.BRICK;
		case FURNACE: case BURNING_FURNACE: 
			return ChimneyMaterial.FURNACE;
		}
		return null;
	}
	
	private Block getNextBlock(Block b){
		
		BlockFace out = getOutput(b.getState());
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
		return null;
	}
	
	private boolean isStairs(Material m){
		if(m == Material.COBBLESTONE_STAIRS) return true;
		if(m == Material.SMOOTH_STAIRS) return true;
		if(m == Material.BRICK_STAIRS) return true;
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
		if(!MangoStructures.useChimneys) return;
		for(Player p : Bukkit.getServer().getOnlinePlayers()){
			if(!p.getLocation().getWorld().equals(getEmiterLocation().getWorld())) continue;
			if(p.getLocation().distance(getEmiterLocation()) < 255){
				sendSmoke(p);
				if(!isSafe(p)){
					p.damage(1);
					if(p.getLocation().distance(getEmiterLocation()) < 2){
						p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 20 * 5, 1));
					}
				}
				//p.spawnParticle(Particle.SMOKE_LARGE, getEmiterLocation(), 20, 0.5, 0.5, 0.5, 0.5, BlockFace.UP);
				//getEmiterLocation().getWorld().playEffect(getEmiterLocation(), Effect.LARGE_SMOKE, 0, 200);
			}
		}
	}
	
	private void sendSmoke(Player p){
		PacketContainer smoke = MangoStructures.protocolManager.createPacket(PacketType.Play.Server.WORLD_PARTICLES);
		smoke.getIntegers().write(0, 12);
		smoke.getBooleans().write(0, true);
		smoke.getFloat().write(0,  (float) getEmiterLocation().getX()).
		write(1, (float) getEmiterLocation().getY()).
		write(2, (float) getEmiterLocation().getZ()).
		write(3, 0.25f).
		write(4, 0.5f).
		write(5, 0.25f);
		switch(getSmokeColor()){
		case BLACK:
			smoke.getFloat().write(6, 0f);
			break;
		default:
			break;
		}
		smoke.getIntegers().write(0,  30);
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