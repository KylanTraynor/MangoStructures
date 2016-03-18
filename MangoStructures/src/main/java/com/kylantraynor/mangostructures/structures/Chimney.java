package com.kylantraynor.mangostructures.structures;

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
		BlockFace cd = getChimneyDirection(currentBlock.getState());
		List<BlockState> list = new ArrayList<BlockState>();
		if (cm != null){
			if (cm == ChimneyMaterial.FURNACE){
				if (hasValidConnection(BlockFace.UP, currentBlock.getRelative(BlockFace.UP).getState())){
					list.add(currentBlock.getState());
					currentBlock = currentBlock.getRelative(BlockFace.UP);
				} else if (hasValidConnection(cd, currentBlock.getRelative(cd).getState())) {
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
					BlockFace cd1 = getChimneyDirection(currentBlock.getState());
					if (cm1 != null){
						if (hasValidConnection(cd1, currentBlock.getRelative(cd1).getState())){
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

	private boolean hasValidConnection(BlockFace from, BlockState to){
		if ((getChimneyMaterial(to.getType()) == null) || (getChimneyMaterial(to.getType()) == ChimneyMaterial.FURNACE)) {
			return false;
		}
		switch (from){
		case UP: 
			if ((to.getData() instanceof Stairs)){
				Stairs st = (Stairs)to.getData();
				if (st.isInverted()) {
					return false;
				}
				return true;
			}
			if ((to.getData() instanceof Step)) {
				return false;
			}
			return true;
		case NORTH: case SOUTH: case WEST: case EAST: 
			if ((to.getData() instanceof Stairs)){
				Stairs st = (Stairs)to.getData();
				if (st.isInverted()){
					if (st.getDescendingDirection().equals(from)) {
						return true;
					}
					return false;
				}
				return false;
			}
			if ((to.getData() instanceof Step)) {
				return false;
			}
			return true;
		}
		return false;
	}

	private BlockFace getChimneyDirection(BlockState state){
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
		switch (getSmokeColor()){
		case BLACK: 
			for(Player p : Bukkit.getServer().getOnlinePlayers()){
				if(p.getLocation().distance(getEmiterLocation()) < 150){
					p.spawnParticle(Particle.SMOKE_NORMAL,
							getEmiterLocation(), 20, 0.5, 0.5, 0.5, 0.5);
					//getEmiterLocation().getWorld().playEffect(getEmiterLocation(), Effect.LARGE_SMOKE, 0, 200);
				}
			}
			//getEmiterLocation().getWorld().playEffect(getEmiterLocation(), Effect.LARGE_SMOKE, 0, 200);
			break;
		case WHITE: default: 
			for(Player p : Bukkit.getServer().getOnlinePlayers()){
				if(p.getLocation().distance(getEmiterLocation()) < 150){
					p.spawnParticle(Particle.SMOKE_LARGE,
							getEmiterLocation(), 20, 0.5, 0.5, 0.5, 0.5);
					//getEmiterLocation().getWorld().playEffect(getEmiterLocation(), Effect.LARGE_SMOKE, 0, 200);
				}
			}
			//getEmiterLocation().getWorld().playEffect(getEmiterLocation(), Effect.CLOUD, 0, 200);
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