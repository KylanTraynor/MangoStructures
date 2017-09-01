package com.kylantraynor.mangostructures;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.inventory.FurnaceBurnEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.material.MaterialData;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.kylantraynor.mangostructures.commands.BellsCommand;
import com.kylantraynor.mangostructures.commands.ChimneyCommand;
import com.kylantraynor.mangostructures.structures.Bell;
import com.kylantraynor.mangostructures.structures.Chimney;
import com.kylantraynor.mangostructures.structures.Kiln;
import com.kylantraynor.mangostructures.structures.Structure;

public class MangoStructures extends JavaPlugin implements Listener{
	private List<Structure> structures = new ArrayList<Structure>();
	private List<Chimney> activeChimneys = new ArrayList<Chimney>();
	private List<Kiln> allKilns = new ArrayList<Kiln>();
	public static ProtocolManager protocolManager;
	public static boolean useChimneys = true;
	private static boolean DEBUG = false;
	public static Plugin currentInstance;
	
	public void onEnable(){
		currentInstance = this;
		saveDefaultConfig();
		protocolManager = ProtocolLibrary.getProtocolManager();
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(this, this);
		BukkitRunnable bk = new BukkitRunnable(){
			public void run(){
				if(!MangoStructures.useChimneys) return;
				Chimney[] chmny = activeChimneys.toArray(new Chimney[activeChimneys.size()]);
				for (Chimney c : chmny) {
					if(c.getLocation().getWorld().isChunkLoaded(c.getLocation().getBlockX() >> 4, c.getLocation().getBlockZ() >> 4)){
						Block b = c.getLocation().getBlock();
						if (b.getType() == Material.BURNING_FURNACE){
							c.puff();
						} else {
							activeChimneys.remove(c);
						}
					}
				}
				for(Kiln k : allKilns.toArray(new Kiln[allKilns.size()])){
					if(k.getLocation().getWorld().isChunkLoaded(k.getLocation().getBlockX() >> 4, k.getLocation().getBlockZ() >> 4)){
						if(k.isValidShape()){
							k.update();
						} else {
							allKilns.remove(k);
						}
					}
				}
			}
		};
		bk.runTaskTimer(this, 10L, 20L);
		
		this.getCommand("Chimney").setExecutor(new ChimneyCommand());
		this.getCommand("Bells").setExecutor(new BellsCommand());
		
		reloadKilns();
		reloadBells();
		addCustomRecipes();
	}
	
	private void addCustomRecipes(){
		ItemStack item;
		Recipe recipe;
		MaterialData data;
		
		item = new ItemStack(Material.STONE_BUTTON);
		item.setAmount(4);
		recipe = new ShapelessRecipe(new NamespacedKey(this, "stone_button"), item);
		((ShapelessRecipe) recipe).addIngredient(Material.REDSTONE);
		((ShapelessRecipe) recipe).addIngredient(Material.STONE);
		Bukkit.addRecipe(recipe);
		
		item = new ItemStack(Material.COBBLESTONE);
		recipe = new ShapedRecipe(new NamespacedKey(this, "cobble_from_flint"), item);
		((ShapedRecipe) recipe).shape("xx", "xx");
		((ShapedRecipe) recipe).setIngredient('x', Material.FLINT);
		Bukkit.addRecipe(recipe);
		
		item = new ItemStack(Material.SMOOTH_BRICK);
		recipe = new ShapedRecipe(new NamespacedKey(this, "brick_from_steps0"), item);
		((ShapedRecipe) recipe).shape("x", "x");
		data = new MaterialData(Material.STEP);
		data.setData((byte) 0);
		((ShapedRecipe) recipe).setIngredient('x', data);
		Bukkit.addRecipe(recipe);
		
		item = new ItemStack(Material.STEP);
		MaterialData d = item.getData();
		d.setData((byte) 2);
		item.setData(d);
		item.setAmount(4);
		recipe = new ShapedRecipe(new NamespacedKey(this, "dirt_slab"), item);
		((ShapedRecipe) recipe).shape("xx");
		data = new MaterialData(Material.DIRT);
		data.setData((byte) 0);
		((ShapedRecipe) recipe).setIngredient('x', data);
		Bukkit.addRecipe(recipe);
		
		recipe = new ShapedRecipe(new NamespacedKey(this, "dirst_from_slabs"), new ItemStack(Material.DIRT));
		((ShapedRecipe) recipe).shape("x","x");
		data = new MaterialData(Material.STEP);
		data.setData((byte) 2);
		((ShapedRecipe) recipe).setIngredient('x', data);
		Bukkit.addRecipe(recipe);
		
		item = new ItemStack(Material.COBBLESTONE);
		recipe = new ShapedRecipe(new NamespacedKey(this, "cobble_from_steps"), item);
		((ShapedRecipe) recipe).shape("x", "x");
		data = new MaterialData(Material.STEP);
		data.setData((byte) 3);
		((ShapedRecipe) recipe).setIngredient('x', data);
		Bukkit.addRecipe(recipe);
		
		item = new ItemStack(Material.SMOOTH_BRICK);
		recipe = new ShapedRecipe(new NamespacedKey(this, "brick_from_steps5"), item);
		((ShapedRecipe) recipe).shape("x", "x");
		data = new MaterialData(Material.STEP);
		data.setData((byte) 5);
		((ShapedRecipe) recipe).setIngredient('x', data);
		Bukkit.addRecipe(recipe);
		
		item = new ItemStack(Material.STONE_AXE);
		recipe = new ShapedRecipe(new NamespacedKey(this, "flint_axe0"), item);
		((ShapedRecipe) recipe).shape("sx", "s ");
		((ShapedRecipe) recipe).setIngredient('x', Material.FLINT);
		((ShapedRecipe) recipe).setIngredient('s', Material.STICK);
		Bukkit.addRecipe(recipe);
		recipe = new ShapedRecipe(new NamespacedKey(this, "flint_axe1"), item);
		((ShapedRecipe) recipe).shape("xs", " s");
		((ShapedRecipe) recipe).setIngredient('x', Material.FLINT);
		((ShapedRecipe) recipe).setIngredient('s', Material.STICK);
		Bukkit.addRecipe(recipe);
		
		item = new ItemStack(Material.STONE_PICKAXE);
		recipe = new ShapedRecipe(new NamespacedKey(this, "flint_pick0"), item);
		((ShapedRecipe) recipe).shape("xx", " s");
		((ShapedRecipe) recipe).setIngredient('x', Material.FLINT);
		((ShapedRecipe) recipe).setIngredient('s', Material.STICK);
		Bukkit.addRecipe(recipe);
		recipe = new ShapedRecipe(new NamespacedKey(this, "flint_pick1"), item);
		((ShapedRecipe) recipe).shape("xx", "s ");
		((ShapedRecipe) recipe).setIngredient('x', Material.FLINT);
		((ShapedRecipe) recipe).setIngredient('s', Material.STICK);
		Bukkit.addRecipe(recipe);
		
		item = new ItemStack(Material.STONE_HOE);
		recipe = new ShapedRecipe(new NamespacedKey(this, "flint_hoe1"), item);
		((ShapedRecipe) recipe).shape("sx", "s ", "s ");
		((ShapedRecipe) recipe).setIngredient('x', Material.FLINT);
		((ShapedRecipe) recipe).setIngredient('s', Material.STICK);
		Bukkit.addRecipe(recipe);
		recipe = new ShapedRecipe(new NamespacedKey(this, "flint_hoe2"), item);
		((ShapedRecipe) recipe).shape("xs", " s" , " s");
		((ShapedRecipe) recipe).setIngredient('x', Material.FLINT);
		((ShapedRecipe) recipe).setIngredient('s', Material.STICK);
		Bukkit.addRecipe(recipe);
	}
	
	private void reloadKilns() {
		Kiln.cookingTimes.put(Material.COAL, 9);
		Kiln.cookingTimes.put(Material.COAL_BLOCK, 81);
		Kiln.cookingTimes.put(Material.LOG, 4);
		Kiln.cookingTimes.put(Material.LOG_2, 4);
		Kiln.cookingTimes.put(Material.WOOD, 1);
		
		Kiln.register(Material.IRON_DOOR, Material.IRON_INGOT, 6);
		Kiln.register(Material.IRON_TRAPDOOR, Material.IRON_INGOT, 4);
		
		Kiln.register(Material.IRON_FENCE, Material.IRON_FENCE, 6);
		
		Kiln.register(Material.IRON_ORE, Material.IRON_INGOT, 2);
		Kiln.register(Material.GOLD_ORE, Material.GOLD_INGOT, 2);
		
		Kiln.register(Material.IRON_CHESTPLATE, Material.IRON_NUGGET, 8 * 9);
		Kiln.register(Material.IRON_LEGGINGS, Material.IRON_NUGGET, 7 * 9);
		Kiln.register(Material.IRON_BARDING, Material.IRON_NUGGET, 6 * 9);
		Kiln.register(Material.IRON_HELMET, Material.IRON_NUGGET, 5 * 9);
		Kiln.register(Material.IRON_BOOTS, Material.IRON_NUGGET, 4 * 9);
		
		Kiln.register(Material.SADDLE, Material.IRON_NUGGET, 3 * 9);
		
		Kiln.register(Material.GOLD_CHESTPLATE, Material.GOLD_NUGGET, 8 * 9);
		Kiln.register(Material.GOLD_LEGGINGS, Material.GOLD_NUGGET, 7 * 9);
		Kiln.register(Material.GOLD_BARDING, Material.GOLD_NUGGET, 6 * 9);
		Kiln.register(Material.GOLD_HELMET, Material.GOLD_NUGGET, 5 * 9);
		Kiln.register(Material.GOLD_BOOTS, Material.GOLD_NUGGET, 4 * 9);
		
		Kiln.register(Material.IRON_AXE, Material.IRON_NUGGET, 3 * 9);
		Kiln.register(Material.IRON_PICKAXE, Material.IRON_NUGGET, 3 * 9);
		Kiln.register(Material.IRON_HOE, Material.IRON_NUGGET, 2 * 9);
		Kiln.register(Material.IRON_SWORD, Material.IRON_NUGGET, 2 * 9);
		Kiln.register(Material.IRON_SPADE, Material.IRON_NUGGET, 1 * 9);
		
		Kiln.register(Material.GOLD_AXE, Material.GOLD_NUGGET, 3 * 9);
		Kiln.register(Material.GOLD_PICKAXE, Material.GOLD_NUGGET, 3 * 9);
		Kiln.register(Material.GOLD_HOE, Material.GOLD_NUGGET, 2 * 9);
		Kiln.register(Material.GOLD_SWORD, Material.GOLD_NUGGET, 2 * 9);
		Kiln.register(Material.GOLD_SPADE, Material.GOLD_NUGGET, 1 * 9);
		
		Kiln.register(Material.CLAY_BALL, Material.CLAY_BRICK, 2);
		
		Kiln.register(Material.SAND, Material.GLASS, 2);
		File f = new File(getDataFolder(), "Kilns.yml");
		if(!f.exists()){
			try {
				f.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return;
		}
		YamlConfiguration config = new YamlConfiguration();
		try {
			config.load(f);
			allKilns = new ArrayList<Kiln>();
			int i = 0;
			while(config.contains("" + i)){
				World w = Bukkit.getServer().getWorld(config.getString(""+i+".world"));
				int x = config.getInt(""+i+".x");
				int y = config.getInt(""+i+".y");
				int z = config.getInt(""+i+".z");
				Location l = new Location(w, x, y, z);
				Kiln k = new Kiln(l);
				if(k.isValidShape()){
					allKilns.add(k);
				}
				i++;
			}
		} catch (IOException | InvalidConfigurationException e) {
			e.printStackTrace();
		}
	}
	
	public void saveAllKilns(){
		YamlConfiguration config = new YamlConfiguration();
		for(int i = 0; i < allKilns.size(); i++){
			config.set(""+i+".world", allKilns.get(i).getLocation().getWorld().getName());
			config.set(""+i+".x", allKilns.get(i).getLocation().getBlockX());
			config.set(""+i+".y", allKilns.get(i).getLocation().getBlockY());
			config.set(""+i+".z", allKilns.get(i).getLocation().getBlockZ());
		}
		File f = new File(getDataFolder(), "Kilns.yml");
		try {
			config.save(f);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void reloadBells(){
		File f = new File(getDataFolder(), "Bells.yml");
		if(!f.exists()){
			try {
				f.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return;
		}
		YamlConfiguration config = new YamlConfiguration();
		try {
			config.load(f);
			Bell.getBells().clear();
			int i = 0;
			while(config.contains("" + i)){
				World w = Bukkit.getServer().getWorld(config.getString(""+i+".world"));
				int x = config.getInt(""+i+".x");
				int y = config.getInt(""+i+".y");
				int z = config.getInt(""+i+".z");
				Location l = new Location(w, x, y, z);
				if(Bell.getAt(l) == null){
					Bell b = new Bell(l);
					if(config.contains("" + i + ".name")){
						b.setName(config.getString("" + i + ".name"));
					}
				}
				i++;
			}
		} catch (IOException | InvalidConfigurationException e) {
			e.printStackTrace();
		}
	}

	public void onDisable() {
		saveAllKilns();
		saveAllBells();
	}

	private void saveAllBells() {
		YamlConfiguration config = new YamlConfiguration();
		for(int i = 0; i < Bell.getBells().size(); i++){
			config.set("" + i + ".world", Bell.getBells().get(i).getLocation().getWorld().getName());
			config.set("" + i + ".x", Bell.getBells().get(i).getLocation().getBlockX());
			config.set("" + i + ".y", Bell.getBells().get(i).getLocation().getBlockY());
			config.set("" + i + ".z", Bell.getBells().get(i).getLocation().getBlockZ());
			if(Bell.getBells().get(i).getName() != null){
				config.set("" + i + ".name", Bell.getBells().get(i).getName());
			}
		}
		File f = new File(getDataFolder(), "Bells.yml");
		try{
			config.save(f);
		} catch(IOException e){
			e.printStackTrace();
		}
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event){
		if(event.getAction() == Action.RIGHT_CLICK_BLOCK){
			for(Kiln k : allKilns){
				if(k.isInside(event.getClickedBlock().getLocation())){
					if(event.getClickedBlock().getType() == Material.IRON_TRAPDOOR){
						event.setCancelled(true);
						k.openInventory(event.getPlayer());
					} else {
						event.getPlayer().sendMessage(ChatColor.GRAY + "This is a Kiln.");
					}
				}
			}
			if(event.getItem() == null){
				for(Bell b : Bell.getBells()){
					if(b.has(event.getClickedBlock())){
						if(Bell.isClapperMaterial(event.getClickedBlock().getType())){
							b.ring();
							event.setCancelled(true);
						}
					}
				}
			}
		}
	}
	
	@EventHandler
	public void onBlockBreak(BlockBreakEvent event){
		for(Kiln k : allKilns){
			if(k.isInside(event.getBlock().getLocation())) k.loadShape();
		}
	}
	
	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event){
		for(Kiln k : allKilns){
			if(k.isInside(event.getBlock().getLocation())) k.loadShape();
		}
		if(event.getPlayer() != null){
			if(event.getBlock().getType() == Material.IRON_FENCE){
				if(event.getBlock().getRelative(BlockFace.DOWN).getType() == Material.CHEST){
					Kiln k = new Kiln(event.getBlock().getRelative(BlockFace.DOWN).getLocation());
					if(k.isValidShape()){
						event.getPlayer().sendMessage(ChatColor.GREEN + "Kiln built!");
						allKilns.add(k);
					}
				}
			} else if(Bell.isClapperMaterial(event.getBlock().getType())){
				Block cb = event.getBlock().getRelative(BlockFace.UP);
				boolean foundBell = false;
				while(Bell.isBellBlock(cb)){
					if(!Bell.isClapperMaterial(cb.getType())){
						foundBell = true;
					} else if(foundBell){
						Bell b = Bell.getAt(cb.getLocation());
						if(b == null){
							b = new Bell(cb.getLocation());
							if(b.isValidShape()){
								event.getPlayer().sendMessage(ChatColor.GREEN + "Bell created!");
							} else {
								event.getPlayer().sendMessage(ChatColor.RED + "Bell doesn't have a valid shape.");
								event.getPlayer().sendMessage(b.getShape());
							}
						}else{ 
							b.loadShape();
							event.getPlayer().sendMessage(ChatColor.GREEN + "Bell's shape has been updated.");
						}
						break;
					}
					cb = cb.getRelative(BlockFace.UP);
				}
			}
		}
	}

	@EventHandler
	public void onFurnaceBurn(FurnaceBurnEvent e)
	{
		Chimney c = new Chimney(e.getBlock().getLocation());
		for(Chimney ch : activeChimneys){
			if(ch.getLocation().getWorld().equals(c.getLocation().getWorld())){
				if(ch.getLocation().distance(c.getLocation()) < 1.0){
					ch.updateConduit();
					return;
				}
			}
		}
		activeChimneys.add(c);
	}
	
	@EventHandler
	public void onBlockRedstone(BlockRedstoneEvent event){
		int x = event.getBlock().getX();
		int y = event.getBlock().getY();
		int z = event.getBlock().getZ();
		if(event.getBlock().getWorld().getBlockAt(x+1,y,z).getState() instanceof Sign){
			signRedstoneEvent(event,x+1,y,z,event.getNewCurrent() > 0);
		}
		if(event.getBlock().getWorld().getBlockAt(x-1,y,z).getState() instanceof Sign){
			signRedstoneEvent(event,x-1,y,z,event.getNewCurrent() > 0 );
		}
		if(event.getBlock().getWorld().getBlockAt(x,y,z+1).getState() instanceof Sign){
			signRedstoneEvent(event,x,y,z+1,event.getNewCurrent() > 0);
		}
		if(event.getBlock().getWorld().getBlockAt(x,y,z-1).getState() instanceof Sign){
			signRedstoneEvent(event,x,y,z-1,event.getNewCurrent() > 0);
		}
	}
	
	public void signRedstoneEvent(BlockRedstoneEvent event, int x, int y, int z, boolean isPowered){
		Sign sign = (Sign)event.getBlock().getWorld().getBlockAt(x,y,z).getState();
		if (sign.getLine(0).equalsIgnoreCase("[BELL]")){
			if (isPowered){
				Bell target = Bell.get(sign.getLine(1).replaceFirst("" + ChatColor.GREEN, ""));
				if (target == null){return;}
				if(Utils.get2DDistanceSquared(target.getLocation().getBlockX(), target.getLocation().getZ(), sign.getX(), sign.getZ()) > 900) return;
				
				target.ring();
			}
		}
	}
	
	@EventHandler
	public void onSignChange(SignChangeEvent event){
		if (event.getLine(0).equalsIgnoreCase("[BELL]")){
			Bell b = Bell.get(event.getLine(1));
			if(b != null){
				if(Utils.get2DDistanceSquared(b.getLocation().getBlockX(), b.getLocation().getBlockZ(), event.getBlock().getX(), event.getBlock().getZ()) > 900) {
					event.getPlayer().sendMessage(ChatColor.RED + "[Bell] Bell " + b.getName() + " is too far from this sign to create a link.");
					return;
				}
				event.getPlayer().sendMessage(ChatColor.GREEN + "[Bell] Sign linked!");
				event.setLine(1, ChatColor.GREEN + b.getName());
			} else {
				event.getPlayer().sendMessage(ChatColor.RED + "[Bell] A bell named " + event.getLine(1) + " doesn't exist.");
				event.setLine(1, ChatColor.RED + event.getLine(1));
			}
		}
	}

	public Structure getStructure(Block block)
	{
		return null;
	}
	
	public void removeRecipeResultingIn(ItemStack is){
		Iterator<Recipe> it = Bukkit.recipeIterator();
		while(it.hasNext()){
			Recipe r = it.next();
			if (r != null && r.getResult().getType() == Material.BREAD) {
				it.remove();
			}
		}
	}

	public static void DEBUG(String string) {
		if(!DEBUG) return;
		Bukkit.getServer().getLogger().info(string);
	}
}
