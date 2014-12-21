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

import com.google.gson.*;
import net.amigocraft.mpt.Main;
import net.amigocraft.mpt.util.Config;
import net.amigocraft.mpt.util.MPTException;
import net.amigocraft.mpt.util.MiscUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

public class UpdateCommand extends SubcommandManager {

	public UpdateCommand(CommandSender sender, String[] args){
		super(sender, args);
	}

	@Override
	public void handle(){
		if (args.length  == 1){
			Bukkit.getScheduler().runTaskAsynchronously(Main.plugin, new Runnable(){
				public void run(){
					downloadRepos();
				}
			});
		}
		else
			sender.sendMessage(ChatColor.RED + "[MPT] Too many arguments! Type " + ChatColor.GOLD + "/mpt help" +
					ChatColor.RED + " for help.");
	}

	public void downloadRepos(){
		try {
			lockStores();
			final File rStoreFile = new File(Main.plugin.getDataFolder(), "repositories.json");
			if (!rStoreFile.exists())
				Main.initializeRepoStore(rStoreFile); // gotta initialize it before using it
			final File pStoreFile = new File(Main.plugin.getDataFolder(), "packages.json");
			if (!pStoreFile.exists())
				Main.initializePackageStore(pStoreFile);
			JsonObject repos = Main.repoStore.getAsJsonObject("repositories");
			threadSafeSendMessage(sender, ChatColor.DARK_PURPLE + "[MPT] Updating local package store...");
			if (!(sender instanceof ConsoleCommandSender))
				Main.log.info("Updating local package store");
			for (Map.Entry<String, JsonElement> e : repos.entrySet()){
				final String id = e.getKey();
				JsonObject repo = e.getValue().getAsJsonObject();
				final String url = repo.get("url").getAsString();
				if (Config.VERBOSE)
					Main.log.info("Updating repository \"" + id + "\"");
				Bukkit.getScheduler().runTaskAsynchronously(Main.plugin, new Runnable() {
					public void run(){
						connectAndUpdate(url);
						try {
							FileWriter writer = new FileWriter(pStoreFile); // get a writer for the store file
							writer.write(Main.gson.toJson(Main.packageStore)); // write to disk
							writer.flush();
						}
						catch (IOException ex){
							ex.printStackTrace();
							threadSafeSendMessage(sender, "Failed to write package store to disk!");
						}
						unlockStores();
					}
				});
			}
		}
		catch (MPTException ex){
			threadSafeSendMessage(sender, ex.getMessage());
		}
	}


	private void connectAndUpdate(String path){
		try {
			JsonObject json = MiscUtil.getRemoteIndex(path);
			String repoId = json.get("id").getAsString();
			JsonObject packages = json.getAsJsonObject("packages");
			for (Map.Entry<String, JsonElement> e : packages.entrySet()){
				JsonObject o = e.getValue().getAsJsonObject();
				if (o.has("name") && o.has("description") && o.has("version") && o.has("url")){
					String id = e.getKey();
					String name = o.get("name").getAsString();
					String desc = o.get("description").getAsString();
					String version = o.get("version").getAsString();
					String url = o.get("url").getAsString();
					if (Config.VERBOSE)
						Main.log.info("Fetching package \"" + id + "\"");
					JsonObject localPackages = Main.packageStore.getAsJsonObject("packages");
					JsonObject pObj = new JsonObject();
					pObj.addProperty("repo", repoId);
					pObj.addProperty("name", name);
					pObj.addProperty("description", desc);
					pObj.addProperty("version", version);
					pObj.addProperty("url", url);
					localPackages.add(id, pObj);
				}
				else if (Config.VERBOSE)
					Main.log.info("Found invalid package definition in repository \"" + repoId + "\"");
			}
		}
		catch (MPTException ex){
			threadSafeSendMessage(sender, ex.getMessage());
		}
		threadSafeSendMessage(sender, "[MPT] Finished updating local package store!");
		if (!(sender instanceof ConsoleCommandSender))
			Main.log.info(ChatColor.DARK_PURPLE + "Finished updating local package store!");
	}
}
