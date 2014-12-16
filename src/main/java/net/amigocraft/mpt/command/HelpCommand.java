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

import com.google.common.collect.ImmutableMap;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.HashMap;
import java.util.Map;

public class HelpCommand extends SubcommandManager {

	private static Map<String, String[]> commands;

	static {
		commands = new HashMap<String, String[]>();
		commands.put("add-repo",
				new String[]{"Adds the repository at the given URL to the plugin config", "/mpt add-repo [id] [url]",
						"mpt.addrepo"});
		commands.put("help", new String[]{"Displays this help menu", "/mpt help", "mpt.help"});
		commands = ImmutableMap.copyOf(commands);
	}

	public HelpCommand(CommandSender sender, String[] args){
		super(sender, args);
	}

	@Override
	public void handle(){
		if (sender.hasPermission("mpt.help")){
			sender.sendMessage(ChatColor.GOLD + "Commands available to you:");
			for (Map.Entry e : commands.entrySet()){
				if (sender.hasPermission(((String[])e.getValue())[2])){
					sender.sendMessage(ChatColor.DARK_GRAY + "-----------------------------------------------");
					sender.sendMessage(ChatColor.DARK_BLUE + (String)e.getKey() + ChatColor.WHITE + " - " +
							ChatColor.GOLD + ((String[])e.getValue())[0]);
					sender.sendMessage(ChatColor.DARK_PURPLE + "Usage: " + ((String[])e.getValue())[1]);
					sender.sendMessage(ChatColor.GREEN + "Permission node: " + ((String[])e.getValue())[2]);
				}
			}
		}
		else
			sender.sendMessage(ChatColor.RED + "You do not have access to this command!");
	}
}
