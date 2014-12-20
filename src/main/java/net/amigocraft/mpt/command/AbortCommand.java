package net.amigocraft.mpt.command;

import net.amigocraft.mpt.Main;
import net.amigocraft.mpt.util.MiscUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

public class AbortCommand extends SubcommandManager {

	public AbortCommand(CommandSender sender, String[] args){
		super(sender, args);
	}

	@Override
	public void handle(){
		if (sender.hasPermission("mpt.abort")){
			Main.log.info("Forcibly unlocking stores...");
			Bukkit.getScheduler().cancelTasks(Main.plugin); // cancel any currently running tasks
			MiscUtil.unlockStores();
		}
		else
			sender.sendMessage("You do not have permission to use this command!");
	}
}
