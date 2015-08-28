/*
 * MPT (Map Packaging Tool)
 *
 * Copyright (c) 2014-2015 Maxim Roncacé <me@caseif.net>
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
package net.caseif.mpt.command;

import net.caseif.mpt.Main;
import net.caseif.mpt.util.Config;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

public class ReloadCommand extends SubcommandManager {

    public ReloadCommand(CommandSender sender, String[] args) {
        super(sender, args);
    }

    @Override
    public void handle() {
        if (!checkPerms()) {
            return;
        }
        sender.sendMessage(Config.INFO_COLOR + "[MPT] Reloading MPT...");
        Bukkit.getPluginManager().disablePlugin(Main.plugin);
        Bukkit.getPluginManager().enablePlugin(Bukkit.getPluginManager().getPlugin("MPT"));
        sender.sendMessage(Config.INFO_COLOR + "[MPT] Successfully reloaded!");
    }
}
