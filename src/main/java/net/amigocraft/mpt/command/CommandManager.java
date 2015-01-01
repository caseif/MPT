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

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class CommandManager implements CommandExecutor {

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
		if (label.equalsIgnoreCase("mpt")){ // probably not necessary, actually, but whatever
			if (args.length > 0){
				if (args[0].equalsIgnoreCase("add-repo"))
					new AddRepositoryCommand(sender, args).handle();
				else if (args[0].equalsIgnoreCase("remove-repo"))
					new RemoveRepositoryCommand(sender, args).handle();
				else if (args[0].equalsIgnoreCase("update"))
					new UpdateCommand(sender, args).handle();
				else if (args[0].equalsIgnoreCase("list-repos"))
					new ListRepositoriesCommand(sender, args).handle();
				else if (args[0].equalsIgnoreCase("upgrade"))
					new UpgradeCommand(sender, args).handle();
				else if (args[0].equalsIgnoreCase("install"))
					new InstallCommand(sender, args).handle();
				else if (args[0].equalsIgnoreCase("remove"))
					new RemoveCommand(sender, args).handle();
				else if (args[0].equalsIgnoreCase("list"))
					new ListInstalledCommand(sender, args).handle();
				else if (args[0].equalsIgnoreCase("list-all"))
					new ListAvailableCommand(sender, args).handle();
				else if (args[0].equalsIgnoreCase("abort"))
					new AbortCommand(sender, args).handle();
				else if (args[0].equalsIgnoreCase("reload"))
					new ReloadCommand(sender, args).handle();
				else if (args[0].equalsIgnoreCase("help"))
					new HelpCommand(sender, args).handle();
				else if (args[0].equalsIgnoreCase("?"))
					new HelpCommand(sender, args).handle();
				else
					sender.sendMessage(ERROR_COLOR + "[MPT] Invalid command! Type " + COMMAND_COLOR +
							"/mpt help" + ERROR_COLOR + " for help.");
			}
			else {
				sender.sendMessage(ERROR_COLOR + "[MPT] Too few arguments! Type " + COMMAND_COLOR +
						"/mpt help" + ERROR_COLOR + " for help.");
			}
			return true;
		}
		return false;
	}

}
