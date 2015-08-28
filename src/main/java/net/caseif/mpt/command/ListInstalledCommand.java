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
import net.caseif.mpt.util.MPTException;

import org.bukkit.command.CommandSender;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ListInstalledCommand extends SubcommandManager {

    public ListInstalledCommand(CommandSender sender, String[] args) {
        super(sender, args);
    }

    @Override
    public void handle() {
        if (!checkPerms()) {
            return;
        }
        try {
            List<String> messages = new ArrayList<>();
            messages.add(Config.INFO_COLOR + "[MPT] Installed packages:");
            for (String[] info : getPackages()) {
                messages.add(Config.INFO_COLOR + "- " + Config.ID_COLOR + info[1] + " v" + info[2] + Config.INFO_COLOR
                        + " (id: " + Config.ID_COLOR + info[0] + Config.INFO_COLOR + ")");
            }
            sender.sendMessage(messages.toArray(new String[messages.size()]));
        } catch (MPTException ex) {
            sender.sendMessage(Config.ERROR_COLOR + "[MPT] " + ex.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    public static List<String[]> getPackages() throws MPTException {
        if (Main.packageStore.containsKey("packages")) {
            JSONObject packs = (JSONObject)Main.packageStore.get("packages");
            Set<Map.Entry<String, Object>> entries = packs.entrySet();
            List<String[]> packList = new ArrayList<>();
            for (Map.Entry<String, Object> e : entries) {
                if (((JSONObject)e.getValue()).containsKey("installed")) {
                    if (((JSONObject)e.getValue()).containsKey("name")) {
                        packList.add(new String[]{
                                e.getKey().toLowerCase(),
                                ((JSONObject)e.getValue()).get("name").toString(),
                                ((JSONObject)e.getValue()).get("installed").toString(),
                        });
                    } else if (Config.VERBOSE) {
                        Main.log.warning("Invalid package definition \"" + e.getKey().toLowerCase() + "\"");
                    }
                }
            }
            return packList;
        } else {
            throw new MPTException("Package store is malformed!");
        }
    }
}
