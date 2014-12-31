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

import net.amigocraft.mpt.Main;
import net.amigocraft.mpt.util.Config;
import net.amigocraft.mpt.util.MPTException;
import net.amigocraft.mpt.util.MiscUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

public class UpdateCommand extends SubcommandManager {

	public UpdateCommand(CommandSender sender, String[] args){
		super(sender, args);
	}

	@Override
	public void handle(){
		if (!checkPerms()) return;
		if (args.length  == 1){
			Bukkit.getScheduler().runTaskAsynchronously(Main.plugin, new Runnable(){
				public void run(){
					try {
						threadSafeSendMessage(sender, INFO_COLOR + "[MPT] Updating local package store...");
						updateStore();
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

	@SuppressWarnings("unchecked")
	public static void updateStore() throws MPTException {
		if (Thread.currentThread().getId() == Main.mainThreadId)
			throw new MPTException(ERROR_COLOR + "Package store may not be updated from the main thread!");
		lockStores();
		final File rStoreFile = new File(Main.plugin.getDataFolder(), "repositories.json");
		if (!rStoreFile.exists())
			Main.initializeRepoStore(rStoreFile); // gotta initialize it before using it
		final File pStoreFile = new File(Main.plugin.getDataFolder(), "packages.json");
		if (!pStoreFile.exists())
			Main.initializePackageStore(pStoreFile);
		JSONObject repos = (JSONObject)Main.repoStore.get("repositories");
		Set<Map.Entry> entries = repos.entrySet();
		for (Map.Entry<String, JSONObject> e : entries){
			final String id = e.getKey();
			JSONObject repo = (JSONObject)e.getValue();
			final String url = repo.get("url").toString();
			if (VERBOSE)
				Main.log.info("Updating repository \"" + id + "\"");
			JSONObject json = MiscUtil.getRemoteIndex(url);
			String repoId = json.get("id").toString();
			JSONObject packages = (JSONObject)json.get("packages");
			Set<Map.Entry> pEntries = packages.entrySet();
			for (Map.Entry en : pEntries){
				String packId = en.getKey().toString();
				JSONObject o = (JSONObject)en.getValue();
				if (o.containsKey("name") && o.containsKey("version") && o.containsKey("url")){
					if (o.containsKey("sha1") || !Config.ENFORCE_CHECKSUM){
						String name = o.get("name").toString();
						String desc = o.containsKey("description") ? o.get("description").toString() : "";
						String version = o.get("version").toString();
						String contentUrl = o.get("url").toString();
						String sha1 = o.containsKey("sha1") ? o.get("sha1").toString() : "";
						if (VERBOSE)
							Main.log.info("Fetching package \"" + packId + "\"");
						JSONObject localPackages = (JSONObject)Main.packageStore.get("packages");
						String installed = localPackages.containsKey(packId) &&
								((JSONObject)localPackages.get(packId)).containsKey("installed") ?
								(((JSONObject)localPackages.get(packId)).get("installed")).toString() :
								"";
						JSONArray files = localPackages.containsKey(packId) &&
								((JSONObject)localPackages.get(packId)).containsKey("files") ?
								((JSONArray)((JSONObject)localPackages.get(packId)).get("files")) :
								null;
						JSONObject pObj = new JSONObject();
						pObj.put("repo", repoId);
						pObj.put("name", name);
						if (!desc.isEmpty())
							pObj.put("description", desc);
						pObj.put("version", version);
						pObj.put("url", contentUrl);
						if (!sha1.isEmpty())
							pObj.put("sha1", sha1);
						if (!installed.isEmpty())
							pObj.put("installed", installed);
						if (files != null)
							pObj.put("files", files);
						localPackages.put(packId, pObj);
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
