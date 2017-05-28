package com.kylantraynor.mangostructures.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import com.kylantraynor.mangostructures.structures.Bell;

public class BellsCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label,
			String[] args) {
		switch(args[0].toUpperCase()){
		case "SOUND":
			Bell.setSound(args[1]);
		}
		return false;
	}

}
