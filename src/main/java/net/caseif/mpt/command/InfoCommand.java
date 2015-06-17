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
package net.caseif.mpt.command;

import net.caseif.mpt.Main;
import net.caseif.mpt.util.Config;
import net.caseif.mpt.util.MPTException;

import org.bukkit.command.CommandSender;
import org.json.simple.JSONObject;

public class InfoCommand extends SubcommandManager {

    public InfoCommand(CommandSender sender, String[] args) {
        super(sender, args);
    }

    @Override
    public void handle() {
        if (!checkPerms()) {
            return;
        }
        if (args.length == 2) {
            String id = args[1].toLowerCase();
            try {
                String[] info = getPackageInfo(id);
                String[] messages = new String[]{
                        Config.COMMAND_COLOR + "Information for package ID " + Config.ID_COLOR + id
                                + Config.COMMAND_COLOR + ":",
                        Config.INFO_COLOR + "Name: " + Config.ID_COLOR + info[0],
                        Config.INFO_COLOR + "Description: " + Config.ID_COLOR + info[1],
                        Config.INFO_COLOR + "Latest version: " + Config.ID_COLOR + info[2],
                        Config.INFO_COLOR + "Content URL: " + Config.ID_COLOR + info[3],
                        Config.INFO_COLOR + "Repository: " + Config.ID_COLOR + info[4],
                        Config.INFO_COLOR + "SHA-1: " + Config.ID_COLOR + (info[5] != null ? info[5] : "not specified"),
                        Config.INFO_COLOR + "Installed: " + Config.ID_COLOR + (info[6] != null ? info[6] : "no")
                };
                sender.sendMessage(messages);
            } catch (MPTException ex) {
                sender.sendMessage(Config.ERROR_COLOR + "[MPT] " + ex.getMessage());
            }
        } else {
            sender.sendMessage(Config.ERROR_COLOR + "Invalid argument count! Type " + Config.COMMAND_COLOR + "/mpt help"
                    + Config.ERROR_COLOR + " for help.");
        }
    }

    @SuppressWarnings("unchecked")
    public static String[] getPackageInfo(String id) throws MPTException {
        id = id.toLowerCase();
        if (Main.packageStore.containsKey("packages")) {
            JSONObject packs = (JSONObject)Main.packageStore.get("packages");
            Object pack = packs.get(id);
            if (pack != null) {
                JSONObject jPack = (JSONObject)pack;
                if (jPack.containsKey("name")
                        && jPack.containsKey("description")
                        && jPack.containsKey("version")
                        && jPack.containsKey("url")
                        && jPack.containsKey("repo")) {
                    return new String[]{
                            jPack.get("name").toString(),
                            jPack.get("description").toString(),
                            jPack.get("version").toString(),
                            jPack.get("url").toString(),
                            jPack.get("repo").toString(),
                            jPack.containsKey("sha1") ? jPack.get("sha1").toString() : null,
                            jPack.containsKey("installed") ? jPack.get("installed").toString() : null,
                    };
                } else {
                    throw new MPTException(Config.ERROR_COLOR + "Package definition for " + Config.ID_COLOR + id
                            + Config.ERROR_COLOR + " is malformed! Running " + Config.COMMAND_COLOR + "/mpt update"
                            + Config.ERROR_COLOR + " may correct" + "this.");
                }
            } else {
                throw new MPTException(Config.ERROR_COLOR + "Cannot find package " + Config.ID_COLOR + id
                        + Config.ERROR_COLOR + "!");
            }
        } else {
            throw new MPTException("Package store is malformed!");
        }
    }
}
