package com.kylantraynor.mangostructures;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.FurnaceBurnEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.kylantraynor.mangostructures.commands.ChimneyCommand;
import com.kylantraynor.mangostructures.structures.Chimney;
import com.kylantraynor.mangostructures.structures.Kiln;
import com.kylantraynor.mangostructures.structures.Structure;

public class MangoStructures extends JavaPlugin implements Listener{
	private List<Structure> structures = new ArrayList<Structure>();
	private List<Chimney> activeChimneys = new ArrayList<Chimney>();
	private List<Kiln> allKilns = new ArrayList<Kiln>();
	public static ProtocolManager protocolManager;
	public static boolean useChimneys = true;
	
	public void onEnable(){
		saveDefaultConfig();
		protocolManager = ProtocolLibrary.getProtocolManager();
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(this, this);
		BukkitRunnable bk = new BukkitRunnable(){
			public void run(){
				if(!MangoStructures.useChimneys) return;
				Chimney[] chmny = activeChimneys.toArray(new Chimney[activeChimneys.size()]);
				for (Chimney c : chmny) {
					Block b = c.getLocation().getBlock();
					if (b.getType() == Material.BURNING_FURNACE){
						if (b.getChunk().isLoaded()) {
							c.puff();
						}
					} else {
						activeChimneys.remove(c);
					}
				}
				for(Kiln k : allKilns.toArray(new Kiln[allKilns.size()])){
					if(k.getLocation().getChunk().isLoaded()){
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
		
		reloadKilns();
	}
	
	private void reloadKilns() {
		Kiln.cookingTimes.put(Material.COAL, 9);
		Kiln.cookingTimes.put(Material.COAL_BLOCK, 81);
		Kiln.cookingTimes.put(Material.LOG, 3);
		Kiln.cookingTimes.put(Material.LOG_2, 3);
		Kiln.register(Material.IRON_ORE, Material.IRON_INGOT, 2);
		Kiln.register(Material.GOLD_ORE, Material.GOLD_INGOT, 2);
		Kiln.register(Material.IRON_CHESTPLATE, Material.IRON_INGOT, 8);
		Kiln.register(Material.IRON_LEGGINGS, Material.IRON_INGOT, 7);
		Kiln.register(Material.IRON_HELMET, Material.IRON_INGOT, 5);
		Kiln.register(Material.IRON_BOOTS, Material.IRON_INGOT, 4);
		Kiln.register(Material.GOLD_CHESTPLATE, Material.GOLD_INGOT, 8);
		Kiln.register(Material.GOLD_LEGGINGS, Material.GOLD_INGOT, 7);
		Kiln.register(Material.GOLD_HELMET, Material.GOLD_INGOT, 5);
		Kiln.register(Material.GOLD_BOOTS, Material.GOLD_INGOT, 4);
		Kiln.register(Material.IRON_AXE, Material.IRON_INGOT, 3);
		Kiln.register(Material.IRON_PICKAXE, Material.IRON_INGOT, 3);
		Kiln.register(Material.IRON_HOE, Material.IRON_INGOT, 2);
		Kiln.register(Material.IRON_SWORD, Material.IRON_INGOT, 2);
		Kiln.register(Material.IRON_SPADE, Material.IRON_INGOT, 1);
		Kiln.register(Material.GOLD_AXE, Material.GOLD_NUGGET, 3 * 9);
		Kiln.register(Material.GOLD_PICKAXE, Material.GOLD_NUGGET, 3 * 9);
		Kiln.register(Material.GOLD_HOE, Material.GOLD_NUGGET, 2 * 9);
		Kiln.register(Material.GOLD_SWORD, Material.GOLD_NUGGET, 2 * 9);
		Kiln.register(Material.GOLD_SPADE, Material.GOLD_NUGGET, 1 * 9);
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

	public void onDisable() {
		saveAllKilns();
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event){
		if(event.getAction() == Action.RIGHT_CLICK_BLOCK){
			for(Kiln k : allKilns){
				if(k.isInside(event.getClickedBlock().getLocation())){
					if(event.getClickedBlock().getType() == Material.IRON_TRAPDOOR){
						k.openInventory(event.getPlayer());
					} else {
						event.getPlayer().sendMessage(ChatColor.GRAY + "This is a Kiln.");
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

	public Structure getStructure(Block block)
	{
		return null;
	}
}
