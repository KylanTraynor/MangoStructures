package com.kylantraynor.mangostructures;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.FurnaceBurnEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.kylantraynor.mangostructures.commands.ChimneyCommand;
import com.kylantraynor.mangostructures.structures.Chimney;
import com.kylantraynor.mangostructures.structures.Structure;

public class MangoStructures extends JavaPlugin implements Listener{
	private List<Structure> structures = new ArrayList<Structure>();
	private List<Chimney> activeChimneys = new ArrayList<Chimney>();
	public static ProtocolManager protocolManager;
	public static boolean useChimneys = true;
	
	public void onEnable(){
		protocolManager = ProtocolLibrary.getProtocolManager();
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(this, this);
		BukkitRunnable bk = new BukkitRunnable(){
			public void run(){
				if(!MangoStructures.useChimneys) return;
				Chimney[] chmny = activeChimneys.toArray(new Chimney[activeChimneys.size()]);
				for (Chimney c : chmny) {
					if (c.isSafe()){
						Block b = c.getLocation().getBlock();
						if (b.getType() == Material.BURNING_FURNACE){
							if (b.getChunk().isLoaded()) {
								c.puff();
							}
						} else {
							activeChimneys.remove(c);
						}
					}
				}
			}
		};
		bk.runTaskTimer(this, 10L, 20L);
		
		this.getCommand("Chimney").setExecutor(new ChimneyCommand());
	}
	
	public void onDisable() {}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {}

	@EventHandler
	public void onFurnaceBurn(FurnaceBurnEvent e)
	{
		Chimney c = new Chimney(e.getBlock().getLocation());
		if (!activeChimneys.contains(c)) {
			activeChimneys.add(c);
		}
	}

	public Structure getStructure(Block block)
	{
		return null;
	}
}
