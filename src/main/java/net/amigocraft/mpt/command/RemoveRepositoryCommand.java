/*
 * MPT (Map Packaging Tool)
 *
 * Copyright (c) 2014 Maxim Roncacé <https://github.com/mproncace>
 *
 * The MIT License (MIT)
 *
 *     Permission is hereby granted, free of charge, to any person obtaining a copy
 *     of this software and associated documentation files (the "Software"), to deal
 *     in the Software without restriction, including without limitation the rights
 *     to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *     copies of the Software, and to permit persons to whom the Software is
 *     furnished to do so, subject to the following conditions:
 *
 *     The above copyright notice and this permission notice shall be included in all
 *     copies or substantial portions of the Software.
 *
 *     THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *     IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *     FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *     AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *     LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *     OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *     SOFTWARE.
 */
package net.amigocraft.mpt.command;

import static net.amigocraft.mpt.util.MiscUtil.*;

import net.amigocraft.mpt.Main;

import com.google.gson.JsonElement;
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
				if (lockStores()){
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
					unlockStores();
				}
				else
					sender.sendMessage(ChatColor.RED + "[MPT] The local store is currently locked! " +
							"Perhaps another MPT task is running?");
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
