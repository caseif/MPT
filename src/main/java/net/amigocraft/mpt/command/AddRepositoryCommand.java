/*
 * MPT (Map Packaging Tool)
 *
 * Copyright (c) 2014 Maxim Roncace
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package net.amigocraft.mpt.command;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.amigocraft.mpt.Main;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class AddRepositoryCommand extends SubcommandManager {

	public AddRepositoryCommand(CommandSender sender, String[] args){
		super(sender, args);
	}

	@Override
	public void handle(){
		if (sender.hasPermission("mpt.addrepo")){
			if (args.length == 3){
				String id = args[1];
				String url = args[2];
				// get the main array from the JSON object
				JsonArray array = Main.repos.getAsJsonArray("repositories");
				// verify the repo hasn't already been added
				for (JsonElement e : array){
					JsonObject o = e.getAsJsonObject();
					// check ID
					if (o.has("id") && o.get("id").getAsString().equalsIgnoreCase(id)){
						sender.sendMessage(ChatColor.RED + "A repository by that ID has already been added!");
						return;
					}
					// check URL
					if (o.has("url") && o.get("url").getAsString().equalsIgnoreCase(url)){
						sender.sendMessage(ChatColor.RED + "The repository at that URL has already been added!");
						return;
					}
					//TODO: verify remote is repository
				}
			}
			else if (args.length < 3)
				sender.sendMessage(ChatColor.RED + "Too few arguments! Type " + ChatColor.DARK_PURPLE + "/mpt help " +
						ChatColor.RED + "for help");
			else
				sender.sendMessage(ChatColor.RED + "Too many arguments! Type " + ChatColor.DARK_PURPLE + "/mpt help " +
						ChatColor.RED + "for help");
		}
		else
			sender.sendMessage(ChatColor.RED + "You do not have access to this command!");
	}
}
