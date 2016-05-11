package com.kylantraynor.mangostructures.structures;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

public class Kiln extends Structure implements InventoryHolder{
	
	static public Map<Material, Integer> cookingTimes = new HashMap<Material, Integer>();
	static public Map<Material, Integer> ingotWorth = new HashMap<Material, Integer>();
	static public Map<Material, Material> ingotKind = new HashMap<Material, Material>();
	
	public static void register(Material m, Material result, int amount){
		ingotWorth.put(m, amount);
		ingotKind.put(m, result);
	}
	
	private String shape;
	private Chimney chimney;
	
	
	
	public Kiln(Location l){
		super(l);
		loadShape();
	}
	
	public void update(){
		if(!isValidShape()) return;
		if(chimney == null){
			chimney = new Chimney(getChimneyStartLocation());
		}
		if(isActive()){
			chimney.updateConduit();
			chimney.puff(3);
		}
		if(tryConsumeFuel()){
			getNetherrackLocation().getBlock().getRelative(BlockFace.UP).setType(Material.FIRE);
			tryMelt();
		} else {
			getNetherrackLocation().getBlock().getRelative(BlockFace.UP).setType(Material.AIR);
		}
	}
	
	private void tryMelt() {
		List<Integer> ilist = getMeltableSlots();
		int i = (int) Math.floor((Math.random() * ilist.size()));
		if(isMeltable(getInventory().getItem(i))){
			tryTransform(i);
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
	
	public void loadShape(){
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

	@Override
	public Inventory getInventory() {
		Block chestBlock = getLocation().getBlock();
		if(chestBlock.getType() == Material.CHEST){
			BlockState state = chestBlock.getState();
			if(state instanceof Chest){
				Chest chest = (Chest) state;
				return chest.getBlockInventory();
			}
		}
		return null;
	}

	public void openInventory(Player p) {
		p.openInventory(getInventory());
	}
	
	public void tryTransform(int slot){
		if(isMeltable(getInventory().getItem(slot))){
			if(Math.random() < 0.2){
				add(ingotKind.get(getInventory().getItem(slot)), getIngotWorth(getInventory().getItem(slot)));
				if(getInventory().getItem(slot).getAmount() > 1){
					getInventory().getItem(slot).setAmount(getInventory().getItem(slot).getAmount() - 1);
				} else {
					getInventory().remove(getInventory().getItem(slot));;
				}
			}
		}
	}
	
	public void add(Material m, int amount){
		add(new ItemStack(m, amount));
	}
	
	public void add(ItemStack item){
		getInventory().addItem(item);
	}
	
	public void remove(ItemStack item){
		if(!getInventory().contains(item.getType())) return;
		for( int i = 0 ; i < getInventory().getSize(); i++){
			if(getInventory().getItem(i) != null){
				if(areSimilar(getInventory().getItem(i), item)){
					if(getInventory().getItem(i).getAmount() > 1){
						getInventory().getItem(i).setAmount(getInventory().getItem(i).getAmount() - 1);
					} else {
						getInventory().removeItem(getInventory().getItem(i));
					}
					break;
				}
			}
		}
	}
	
	public void remove(ItemStack item, int amount){
		while(amount > 0){
			remove(item);
			amount -= 1;
		}
	}
	
	public boolean areSimilar(ItemStack item1, ItemStack item2){
		if(item1.getType() == item2.getType()){
			return true;
		}
		
		// Everything below is just ignored
		
		if(item1.getType() == item2.getType()){
			if(item1.getData() == item2.getData()){
				if(item1.getItemMeta() != null && item2.getItemMeta() != null){
					if(item1.getItemMeta().getDisplayName().equalsIgnoreCase(item2.getItemMeta().getDisplayName())){
						if(item1.getItemMeta().getLore().size() == item2.getItemMeta().getLore().size()){
							return true;
						}
					}
				} else if(item1.getItemMeta() == null && item2.getItemMeta() == null){
					return true;
				}
			}
		}
		return false;
	}
	
	private int getIngotWorth(ItemStack item) {
		float durabilityPercent;
		if(item.getType().getMaxDurability() <= 0){
			durabilityPercent = 1.0f;
		} else {
			durabilityPercent = Math.max((item.getType().getMaxDurability() - item.getDurability()) / item.getType().getMaxDurability(), 0);
		}
		return (int)Math.floor(ingotWorth.get(item.getType()) * durabilityPercent);
	}

	public List<Integer> getMeltableSlots(){
		List<Integer> list = new ArrayList<Integer>();
		for(int i = 0; i < getInventory().getSize(); i++){
			if(getInventory().getItem(i) != null){
				if(isMeltable(getInventory().getItem(i))){
					list.add(i);
				}
			}
		}
		return list;
	}

	private boolean isMeltable(ItemStack item) {
		if(item == null) return false;
		return ingotWorth.containsKey(item.getType());
	}
	
	private boolean tryConsumeFuel(){
		int slot = getFuelSlot();
		if(slot >= 0){
			
			if(cookingTimes.containsKey(getInventory().getItem(slot).getType())){
				if(Math.random() * (double)cookingTimes.get(getInventory().getItem(slot).getType()) < 0.20){
					if(getInventory().getItem(slot).getAmount() > 1){
						getInventory().getItem(slot).setAmount(getInventory().getItem(slot).getAmount() - 1);
					} else {
						getInventory().removeItem(getInventory().getItem(slot));
					}
					return true;
				} else {
					return true;
				}
			}
			
		}
		return false;
	}

	private int getFuelSlot() {
		for(int i = 0; i < getInventory().getSize(); i++){
			if(getInventory().getItem(i) != null){
				if(cookingTimes.containsKey(getInventory().getItem(i).getType())){
					return i;
				}
			}
		}
		return -1;
	}
}