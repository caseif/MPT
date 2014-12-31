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

import static net.amigocraft.mpt.util.Config.*;
import static net.amigocraft.mpt.util.MiscUtil.*;

import com.google.gson.*;
import net.amigocraft.mpt.Main;
import net.amigocraft.mpt.util.Config;
import net.amigocraft.mpt.util.MPTException;
import net.amigocraft.mpt.util.MiscUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.io.File;
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
					try {
						threadSafeSendMessage(sender, INFO_COLOR + "[MPT] Updating local package store...");
						downloadRepos();
						threadSafeSendMessage(sender, INFO_COLOR + "[MPT] Finished updating local package store!");
					}
					catch (MPTException ex){
						threadSafeSendMessage(sender, ERROR_COLOR + "[MPT] " + ex.getMessage());
					}
				}
			});
		}
		else
			sender.sendMessage(ERROR_COLOR + "[MPT] Too many arguments! Type " + COMMAND_COLOR + "/mpt help" +
					ERROR_COLOR + " for help.");
	}

	public static void downloadRepos() throws MPTException {
		if (Thread.currentThread().getId() == Main.mainThreadId)
			throw new MPTException(ERROR_COLOR + "Package store may not be updated from the main thread!");
		lockStores();
		final File rStoreFile = new File(Main.plugin.getDataFolder(), "repositories.json");
		if (!rStoreFile.exists())
			Main.initializeRepoStore(rStoreFile); // gotta initialize it before using it
		final File pStoreFile = new File(Main.plugin.getDataFolder(), "packages.json");
		if (!pStoreFile.exists())
			Main.initializePackageStore(pStoreFile);
		JsonObject repos = Main.repoStore.getAsJsonObject("repositories");
		for (Map.Entry<String, JsonElement> e : repos.entrySet()){
			final String id = e.getKey();
			JsonObject repo = e.getValue().getAsJsonObject();
			final String url = repo.get("url").getAsString();
			if (VERBOSE)
				Main.log.info("Updating repository \"" + id + "\"");
			JsonObject json = MiscUtil.getRemoteIndex(url);
			String repoId = json.get("id").getAsString();
			JsonObject packages = json.getAsJsonObject("packages");
			for (Map.Entry<String, JsonElement> en : packages.entrySet()){
				String packId = en.getKey();
				JsonObject o = en.getValue().getAsJsonObject();
				if (o.has("name") && o.has("version") && o.has("url")){
					if (o.has("sha1") || !Config.ENFORCE_CHECKSUM){
						String name = o.get("name").getAsString();
						String desc = o.has("description") ? o.get("description").getAsString() : "";
						String version = o.get("version").getAsString();
						String contentUrl = o.get("url").getAsString();
						String sha1 = o.has("sha1") ? o.get("sha1").getAsString() : "";
						if (VERBOSE)
							Main.log.info("Fetching package \"" + packId + "\"");
						JsonObject localPackages = Main.packageStore.getAsJsonObject("packages");
						String installed = localPackages.has(packId) &&
								localPackages.getAsJsonObject(packId).has("installed") ?
								localPackages.getAsJsonObject(packId).get("installed").getAsString() :
								"";
						JsonArray files = localPackages.has(packId) &&
								localPackages.getAsJsonObject(packId).has("files") ?
								localPackages.getAsJsonObject(packId).getAsJsonArray("files") :
								null;
						JsonObject pObj = new JsonObject();
						pObj.addProperty("repo", repoId);
						pObj.addProperty("name", name);
						if (!desc.isEmpty())
							pObj.addProperty("description", desc);
						pObj.addProperty("version", version);
						pObj.addProperty("url", contentUrl);
						if (!sha1.isEmpty())
							pObj.addProperty("sha1", sha1);
						if (!installed.isEmpty())
							pObj.addProperty("installed", installed);
						if (files != null)
							pObj.add("files", files);
						localPackages.add(packId, pObj);
					}
					else if (VERBOSE)
						Main.log.warning("Missing checksum for package \"" + packId + ".\" Ignoring package...");
				}
				else if (VERBOSE)
					Main.log.warning("Found invalid package definition \"" + packId + "\" in repository \"" +
							repoId + "\"");
			}
		}
		try {
			writePackageStore();
		}
		catch (IOException ex){
			throw new MPTException(ERROR_COLOR + "Failed to save repository store to disk!");
		}
		unlockStores();
	}
}
