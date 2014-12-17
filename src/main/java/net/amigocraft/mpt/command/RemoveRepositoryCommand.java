package net.amigocraft.mpt.command;

import com.google.gson.JsonElement;
import net.amigocraft.mpt.Main;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class RemoveRepositoryCommand extends SubcommandManager {

	public RemoveRepositoryCommand(CommandSender sender, String[] args){
		super(sender, args);
	}

	@Override
	public void handle(){
		if (sender.hasPermission("mpt.repos")){
			if (args.length == 2){
				String id = args[1];
				JsonElement remove = null;
				synchronized(Main.REPO_STORE_LOCK){ // prevent CMEs
					for (JsonElement e : Main.repoStore.getAsJsonArray("repositories")){ // iterate repos in local store
						if (e.getAsJsonObject().get("id").getAsString().equalsIgnoreCase(id)){ // entry matches ID
							remove = e; // removing it here would throw a CME
							break; // no need to continue iterating
						}
					}
					if (remove != null){ // check if we found anything
						Main.repoStore.getAsJsonArray("repositories").remove(remove); // remove it from memory
						File store = new File(Main.plugin.getDataFolder(), "repositories.json"); // get the store file
						if (!store.exists()) // avoid dumb errors
							Main.initializeRepoStore(store);
						try {
							FileWriter writer = new FileWriter(store); // initialize writer
							writer.write(Main.gson.toJson(Main.repoStore)); // write JSON
							writer.flush(); // flush writer to disk
						}
						catch (IOException ex){
							ex.printStackTrace();
							sender.sendMessage(ChatColor.RED + "[MPT] Failed to remove repository from local store!");
							Main.repoStore.getAsJsonArray("repositories").add(remove); // readd it to prevent confusion
						}
					}
					else // repo doesn't exist in local store
						sender.sendMessage(ChatColor.RED + "[MPT] Cannot find repo with given ID!");
				}
			}
			else if (args.length < 2)
				sender.sendMessage(ChatColor.RED + "[MPT] Too few arguments! Type " + ChatColor.GOLD +
						"/mpt help" + ChatColor.RED + " for help.");
			else
				sender.sendMessage(ChatColor.RED + "[MPT] Too many arguments! Type " + ChatColor.GOLD +
						"/mpt help" + ChatColor.RED + " for help.");
		}
		else
			sender.sendMessage(ChatColor.RED + "[MPT] You do not have permission to use this command!");
	}
}
