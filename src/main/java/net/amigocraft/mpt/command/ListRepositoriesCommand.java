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

import net.amigocraft.mpt.Main;
import net.amigocraft.mpt.util.MPTException;
import org.bukkit.command.CommandSender;
import org.json.simple.JSONObject;

import java.util.*;

public class ListRepositoriesCommand extends SubcommandManager {

	public ListRepositoriesCommand(CommandSender sender, String[] args){
		super(sender, args);
	}

	@Override
	public void handle(){
		if (!checkPerms()) return;
		try {
			List<String> messages = new ArrayList<>();
			messages.add(INFO_COLOR + "[MPT] Added repositories:");
			for (String[] info : getRepositories()){
				messages.add(INFO_COLOR + "- " + ID_COLOR + info[0] +
						INFO_COLOR + " (url: " + ID_COLOR + info[1] + INFO_COLOR + ")");
			}
			sender.sendMessage(messages.toArray(new String[messages.size()]));
		}
		catch (MPTException ex){
			sender.sendMessage(ERROR_COLOR + "[MPT] " + ex.getMessage());
		}
	}

	@SuppressWarnings("unchecked")
	public static List<String[]> getRepositories() throws MPTException {
		if (Main.repoStore.containsKey("repositories")){
			JSONObject repos = (JSONObject)Main.repoStore.get("repositories");
			Set<Map.Entry<String, Object>> entries = repos.entrySet();
			List<String[]> repoList = new ArrayList<>();
			for (Map.Entry<String, Object> e : entries){
				if (((JSONObject)e.getValue()).containsKey("url")){
					repoList.add(new String[]{e.getKey(), ((JSONObject)e.getValue()).get("url").toString()});
				}
				else if (VERBOSE){
					Main.log.warning("Invalid repository definition \"" + e.getKey() + "\"");
				}
			}
			return repoList;
		}
		else {
			throw new MPTException("Repository store is malformed!");
		}
	}
}
