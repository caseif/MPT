/*
 * MPT (Map Packaging Tool)
 *
 * Copyright (c) 2015 Maxim Roncacé <mproncace@lapis.blue>
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
import org.bukkit.command.CommandSender;
import org.json.simple.JSONObject;

import java.io.File;
import java.io.IOException;

public class RemoveRepositoryCommand extends SubcommandManager {

	public RemoveRepositoryCommand(CommandSender sender, String[] args){
		super(sender, args);
	}

	@Override
	public void handle(){
		if (!checkPerms()) return;
			if (args.length == 2){
				String id = args[1];
				try {
					removeRepository(id);
					sender.sendMessage(INFO_COLOR + "[MPT] Successfully removed repository " +
							ID_COLOR + id + INFO_COLOR + " from local store.");
				}
				catch (MPTException ex){
					sender.sendMessage(ERROR_COLOR + "[MPT] " + ex.getMessage());
				}
			}
			else if (args.length < 2)
				sender.sendMessage(ERROR_COLOR + "[MPT] Too few arguments! Type " + COMMAND_COLOR +
						"/mpt help" + ERROR_COLOR + " for help.");
			else
				sender.sendMessage(ERROR_COLOR + "[MPT] Too many arguments! Type " + COMMAND_COLOR +
						"/mpt help" + ERROR_COLOR + " for help.");
	}

	public static void removeRepository(String id) throws MPTException {
		JSONObject repos = (JSONObject)Main.repoStore.get("repositories");
		if (repos.containsKey(id)){
			lockStores();
			repos.remove(id); // remove it from memory
			File store = new File(Main.plugin.getDataFolder(), "repositories.json"); // get the store file
			if (!store.exists()) // avoid dumb errors
				Main.initializeRepoStore(store);
			try {
				writeRepositoryStore();
			}
			catch (IOException ex){
				ex.printStackTrace();
				unlockStores();
				throw new MPTException(ERROR_COLOR + "Failed to remove repository from local store!");
			}
			unlockStores();
		}
		else // repo doesn't exist in local store
			throw new MPTException(ERROR_COLOR + "Cannot find repo with given ID!");
	}
}
