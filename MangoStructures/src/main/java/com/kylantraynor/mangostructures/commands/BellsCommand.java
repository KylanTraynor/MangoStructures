package com.kylantraynor.mangostructures.commands;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.kylantraynor.mangostructures.structures.Bell;

public class BellsCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label,
			String[] args) {
		switch(args[0].toUpperCase()){
		case "SOUND":
			Bell.setSound(args[1]);
			sender.sendMessage("Changed bells sound to " + args[1]);
			return true;
		case "RENAME":
			if(!(sender instanceof Player)) return false;
			if(args.length < 2) return false;
			List<Block> list = ((Player) sender).getLineOfSight((Set<Material>)null, 16);
			for(Block b : list){
				if(Bell.isBellBlock(b)){
					for(Bell bell : Bell.getBells()){
						if(bell.has(b)){
							bell.setName(args[1]);
							sender.sendMessage(ChatColor.GREEN + "Successfully changed this bell's name to " + args[1] + ".");
							return true;
						}
					}
				} else {
					b.getWorld().spawnParticle(Particle.NOTE, b.getLocation().clone().add(0.5, 0.5, 0.5), 5);
					if(b.getType() != Material.AIR){
						break;
					}
				}
			}
			sender.sendMessage(ChatColor.RED + "Couldn't find a bell within 16 blocks.");
			
		}
		return false;
	}

}
