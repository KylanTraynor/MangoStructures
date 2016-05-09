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

	public Location getEmiterLocation() { return this.emiterLocation; }
	public void setEmiterLocation(Location emiterLocation) { this.emiterLocation = emiterLocation; }

	public boolean isSafe() {
		if (!getBlocks().isEmpty()){
			if ((getBlocks().size() == 1) && (getLocation().getBlock().getLightFromSky() >= 14)) {
				return true;
			}
			if (getBlocks().size() > 1) {
				return true;
			}
			return false;
		}
		return false;
	}

	private void updateConduit(){
		Block currentBlock = getLocation().getBlock();
		ChimneyMaterial cm = getChimneyMaterial(currentBlock.getType());
		BlockFace cd = getOutput(currentBlock.getState());
		List<BlockState> list = new ArrayList<BlockState>();
		if (cm != null){
			if (cm == ChimneyMaterial.FURNACE){
				if (hasValidConnection(BlockFace.UP, getInput(currentBlock.getRelative(BlockFace.UP).getState()))){
					list.add(currentBlock.getState());
					currentBlock = currentBlock.getRelative(BlockFace.UP);
				} else if (hasValidConnection(cd, getInput(currentBlock.getRelative(cd).getState()))) {
					list.add(currentBlock.getState());
					currentBlock = currentBlock.getRelative(cd);
				} else {
					setEmiterLocation(currentBlock.getRelative(cd.getOppositeFace()).getLocation().add(0.5D, 0.0D, 0.5D));
					list.add(currentBlock.getState());
					setBlocks(list);
					return;
				}
				while (currentBlock.getY() < 255){
					ChimneyMaterial cm1 = getChimneyMaterial(currentBlock.getType());
					BlockFace cd1 = getOutput(currentBlock.getState());
					if (cm1 != null){
						if (hasValidConnection(cd1, getInput(currentBlock.getRelative(cd1).getState()))){
							list.add(currentBlock.getState());
							currentBlock = currentBlock.getRelative(cd);
						} else {
							if (currentBlock.getLightFromSky() >= 14){
								setEmiterLocation(currentBlock.getRelative(cd1).getLocation().add(0.5D, 0.0D, 0.5D));
								list.add(currentBlock.getState());
								setBlocks(list);
								return;
							}
							setBlocks(new ArrayList<BlockState>());
							return;
						}
					} else {
						if (currentBlock.getLightFromSky() >= 14) {
							setEmiterLocation(currentBlock.getLocation().add(0.5D, 0.0D, 0.5D));
							setBlocks(list);
							return;
						}
						setBlocks(new ArrayList<BlockState>());
						return;
					}
				}
			}
		} else {
			setBlocks(list);
		}
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
		if ((state.getData() instanceof Stairs)){
			Stairs stairs = (Stairs)state.getData();
			if (!stairs.isInverted()) {
				return BlockFace.DOWN;
			}
			switch (stairs.getAscendingDirection()){
			case NORTH: 
				return BlockFace.NORTH;
			case SOUTH: 
				return BlockFace.SOUTH;
			case WEST: 
				return BlockFace.WEST;
			case EAST: 
				return BlockFace.EAST;
			}
			return null;
		}
		if ((state.getData() instanceof Furnace)){
			return BlockFace.DOWN;
		}
		return null;
	}
	
	private BlockFace getOutput(BlockState state){
		if ((state.getData() instanceof Stairs)){
			Stairs stairs = (Stairs)state.getData();
			if (stairs.isInverted()) {
				return BlockFace.UP;
			}
			switch (stairs.getAscendingDirection()){
			case NORTH: 
				return BlockFace.NORTH;
			case SOUTH: 
				return BlockFace.SOUTH;
			case WEST: 
				return BlockFace.WEST;
			case EAST: 
				return BlockFace.EAST;
			}
			return null;
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

	public void puff(){
		if(!MangoStructures.useChimneys) return;
		switch (getSmokeColor()){
		case BLACK: 
			for(Player p : Bukkit.getServer().getOnlinePlayers()){
				if(!p.getLocation().getWorld().equals(getEmiterLocation().getWorld())) continue;
				if(p.getLocation().distance(getEmiterLocation()) < 150){
					sendSmoke(p);
					//p.spawnParticle(Particle.SMOKE_NORMAL, getEmiterLocation(), 20, 0.5, 0.5, 0.5, 0.5, BlockFace.UP);
					//getEmiterLocation().getWorld().playEffect(getEmiterLocation(), Effect.LARGE_SMOKE, 0, 200);
				}
			}
			//getEmiterLocation().getWorld().playEffect(getEmiterLocation(), Effect.LARGE_SMOKE, 0, 200);
			break;
		case WHITE: default: 
			for(Player p : Bukkit.getServer().getOnlinePlayers()){
				if(!p.getLocation().getWorld().equals(getEmiterLocation().getWorld())) continue;
				if(p.getLocation().distance(getEmiterLocation()) < 150){
					sendSmoke(p);
					//p.spawnParticle(Particle.SMOKE_LARGE, getEmiterLocation(), 20, 0.5, 0.5, 0.5, 0.5, BlockFace.UP);
					//getEmiterLocation().getWorld().playEffect(getEmiterLocation(), Effect.LARGE_SMOKE, 0, 200);
				}
			}
			//getEmiterLocation().getWorld().playEffect(getEmiterLocation(), Effect.CLOUD, 0, 200);
		}
	}
	
	private void sendSmoke(Player p){
		PacketContainer smoke = MangoStructures.protocolManager.createPacket(PacketType.Play.Server.WORLD_PARTICLES);
		smoke.getIntegers().write(0, 12);
		smoke.getBooleans().write(0, true);
		smoke.getFloat().write(0,  (float) getEmiterLocation().getX()).
		write(1, (float) getEmiterLocation().getY()).
		write(2, (float) getEmiterLocation().getZ()).
		write(3, 0.5f).
		write(4, 0.5f).
		write(5, 0.5f);
		smoke.getIntegers().write(0,  15);
		try {
			MangoStructures.protocolManager.sendServerPacket(p, smoke);
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private SmokeType getSmokeColor(){
		if (!getBlocks().isEmpty()){
			switch (((Furnace)(Furnace)((BlockState)getBlocks().get(0)).getData()).getItemType()){
			case IRON_ORE: case GOLD_ORE: 
				return SmokeType.BLACK;
			}
			return SmokeType.WHITE;
		}
		return null;
	}
}