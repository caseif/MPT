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

import net.caseif.mpt.util.Config;

import org.bukkit.command.CommandSender;

public abstract class SubcommandManager {

    protected CommandSender sender;
    protected String[] args;

    public SubcommandManager(CommandSender sender, String[] args) {
        this.sender = sender;
        this.args = args;
    }

    public abstract void handle();

    public boolean checkPerms() {
        if (!HelpCommand.commands.containsKey(args[0]) && sender.hasPermission(HelpCommand.commands.get(args[0])[2])) {
            sender.sendMessage(Config.ERROR_COLOR + "[MPT] You do not have access to this command!");
            return false;
        }
        return true;
    }

}
