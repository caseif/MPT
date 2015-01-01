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

import com.google.common.collect.ImmutableMap;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.HashMap;
import java.util.Map;

public class HelpCommand extends SubcommandManager {

	static Map<String, String[]> commands;

	static {
		commands = new HashMap<String, String[]>();
		commands.put("add-repo",
				new String[]{"Adds the repository at the given URL to the local store",
						"/mpt add-repo [url]",
						"mpt.use"});
		commands.put("remove-repo",
				new String[]{"Removes the repository with the given ID from the local store.",
						"/mpt remove-repo [id]",
						"mpt.use"});
		commands.put("update",
				new String[]{"Updates the local package store from the remote repositories.",
						"/mpt update",
						"mpt.use"});
		commands.put("list-repos",
				new String[]{"Prints out all added repositories.",
						"/mpt list-repos",
						"mpt.use"});
		commands.put("upgrade",
				new String[]{"Upgrades the given package(s), or all if no arguments are provided.",
						"/mpt upgrade {package1} {package2}...",
						"mpt.use"});
		commands.put("install",
				new String[]{"Installs the package(s) with the given ID(s).",
						"/mpt install [package1] {package2}...",
						"mpt.use"});
		commands.put("remove",
				new String[]{"Removes the package(s) with the given ID(s).",
						"/mpt remove [package1] {package2}...",
						"mpt.use"});
		commands.put("list",
				new String[]{"Prints out all installed packages.",
						"/mpt list",
						"mpt.use"});
		commands.put("list-all",
				new String[]{"Prints out all available packages.",
						"/mpt list-all",
						"mpt.use"});
		commands.put("abort",
				new String[]{"Attempts to abort any currently running tasks and unlock the local stores. This " +
						"command is not officially supported and may mess up your stores. Use it as a last resort " +
						"ONLY.",
						"/mpt abort",
						"mpt.abort"});
		commands.put("help",
				new String[]{"Displays this help menu",
						"/mpt help",
						"mpt.help"});
		commands = ImmutableMap.copyOf(commands);
	}

	public HelpCommand(CommandSender sender, String[] args){
		super(sender, args);
	}

	@Override
	public void handle(){
		if (!checkPerms()) return;
		sender.sendMessage(INFO_COLOR + "Commands available to you:");
		for (Map.Entry e : commands.entrySet()){
			if (sender.hasPermission(((String[])e.getValue())[2])){
				sender.sendMessage(ChatColor.DARK_GRAY + "-----------------------------------------------");
				sender.sendMessage(COMMAND_COLOR + "/mpt " + e.getKey() + ChatColor.WHITE + " - " +
						INFO_COLOR + ((String[])e.getValue())[0]);
				sender.sendMessage(INFO_COLOR + "Usage: " + ((String[])e.getValue())[1]);
				sender.sendMessage(INFO_COLOR + "Permission node: " + ((String[])e.getValue())[2]);
			}
		}
	}
}
