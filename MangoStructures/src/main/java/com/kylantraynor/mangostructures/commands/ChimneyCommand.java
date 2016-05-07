package com.kylantraynor.mangostructures.commands;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import com.kylantraynor.mangostructures.MangoStructures;

public class ChimneyCommand implements CommandExecutor{

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		
		if(args.length == 0){
			sender.sendMessage("/Chimney Toggle " + ChatColor.GRAY + "Turns on or off the chimneys.");
			return true;
		}
		
		switch(args[0].toUpperCase()){
		case "TOGGLE":
			MangoStructures.useChimneys = !MangoStructures.useChimneys;
			sender.sendMessage("Using chimneys: " + MangoStructures.useChimneys);
			return true;
		}
		return false;
	}
	
}
