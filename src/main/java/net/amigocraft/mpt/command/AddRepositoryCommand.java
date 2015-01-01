/*
 * MPT (Map Packaging Tool)
 *
 * Copyright (c) 2014-2015 Maxim Roncacé <mproncace@lapis.blue>
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

import net.amigocraft.mpt.util.MPTException;
import net.amigocraft.mpt.util.MiscUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.json.simple.JSONObject;

import java.io.*;
import java.util.Map;
import java.util.Set;

public class AddRepositoryCommand extends SubcommandManager {

	public AddRepositoryCommand(CommandSender sender, String[] args){
		super(sender, args);
	}

	@Override
	public void handle(){
		if (!checkPerms()) return;
		if (args.length == 2){
			final String path = args[1];
			// get the main array from the JSON object
			JSONObject repos = (JSONObject)Main.repoStore.get("repositories");
			// verify the repo hasn't already been added
			Set<Map.Entry> entries = repos.entrySet();
			for (Map.Entry e : entries){ // iterate repos in local store
				JSONObject o = (JSONObject)e.getValue();
				// check URL
				if (o.containsKey("url") && o.get("url").toString().equalsIgnoreCase(path)){
					sender.sendMessage(ERROR_COLOR + "[MPT] The repository at that URL has already been added!");
					return;
				}
			}
			// no way we're making the main thread wait for us to open and read the stream
			Bukkit.getScheduler().runTaskAsynchronously(Main.plugin, new Runnable(){
				public void run(){
					try {
						threadSafeSendMessage(sender, INFO_COLOR +
								"[MPT] Attempting connection to repository...");
						String id = addRepository(path);
						threadSafeSendMessage(sender, INFO_COLOR + "[MPT] Successfully added " +
								"repository under ID " + ID_COLOR + id + INFO_COLOR +
								" to local store! You may now use " + COMMAND_COLOR + "/mpt update" +
								INFO_COLOR + " to fetch available packages.");
					}
					catch (MPTException ex){
						threadSafeSendMessage(sender, ERROR_COLOR + "[MPT] " + ex.getMessage());
					}
				}

			});
		}
		else if (args.length < 2)
			sender.sendMessage(ERROR_COLOR + "[MPT] Too few arguments! Type " + COMMAND_COLOR +
					"/mpt help " + ERROR_COLOR + "for help");
		else
			sender.sendMessage(ERROR_COLOR + "[MPT] Too many arguments! Type " + COMMAND_COLOR +
					"/mpt help " + ERROR_COLOR + "for help");
	}

	@SuppressWarnings("unchecked")
	public static String addRepository(String path) throws MPTException {
		if (Thread.currentThread().getId() == Main.mainThreadId)
			throw new MPTException(ERROR_COLOR + "Repositories may not be added from the main thread!");
		try {
			JSONObject json = MiscUtil.getRemoteIndex(path);
			String id = json.get("id").toString(); // get ID from remote
			File store = new File(Main.plugin.getDataFolder(), "repositories.json");
			if (!store.exists())
				Main.initializeRepoStore(store); // gotta initialize it before using it
			lockStores();
			JSONObject repoElement = new JSONObject(); // create a new JSON object
			repoElement.put("url", path); // set the repo URL
			((JSONObject)Main.repoStore.get("repositories")).put(id, repoElement);
			writeRepositoryStore();
			unlockStores();
			return id;
			// apt-get doesn't fetch packages when a repo is added, so I'm following that precedent
		}
		catch (IOException ex){
			unlockStores();
			throw new MPTException(ERROR_COLOR + "Failed to add repository to local " +
					"store!");
		}
	}
}
