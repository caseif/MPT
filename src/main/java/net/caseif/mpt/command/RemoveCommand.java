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

import static net.caseif.mpt.util.MiscUtil.lockStores;
import static net.caseif.mpt.util.MiscUtil.threadSafeSendMessage;
import static net.caseif.mpt.util.MiscUtil.unlockStores;
import static net.caseif.mpt.util.MiscUtil.writePackageStore;

import net.caseif.mpt.Main;
import net.caseif.mpt.Telemetry;
import net.caseif.mpt.util.Config;
import net.caseif.mpt.util.MPTException;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.File;
import java.io.IOException;

public class RemoveCommand extends SubcommandManager {

    public RemoveCommand(CommandSender sender, String[] args) {
        super(sender, args);
    }

    @Override
    public void handle() {
        if (sender.hasPermission("mpt.use")) {
            if (args.length > 1) {
                Bukkit.getScheduler().runTaskAsynchronously(Main.plugin, new Runnable() {
                    public void run() {
                        try {
                            for (int i = 1; i < args.length; i++) {
                                String id = args[i];
                                threadSafeSendMessage(sender, Config.INFO_COLOR + "Removing " + Config.ID_COLOR + id
                                        + Config.INFO_COLOR + "...");
                                removePackage(id);
                                threadSafeSendMessage(sender, Config.INFO_COLOR + "Successfully removed "
                                        + Config.ID_COLOR + id);
                            }
                        } catch (MPTException ex) {
                            threadSafeSendMessage(sender, Config.ERROR_COLOR + ex.getMessage());
                        }
                    }
                });
            } else {
                sender.sendMessage(Config.ERROR_COLOR + "[MPT] Too few arguments! Type " + Config.COMMAND_COLOR
                        + "/mpt help" + Config.ERROR_COLOR + " for help.");
            }
        } else {
            sender.sendMessage(Config.ERROR_COLOR + "[MPT] You do not have permission to use this command!");
        }
    }

    public static void removePackage(String id) throws MPTException {
        if (Thread.currentThread().getId() == Main.mainThreadId) {
            throw new MPTException(Config.ERROR_COLOR + "Packages may not be removed from the main thread!");
        }
        id = id.toLowerCase();
        if (((JSONObject)Main.packageStore.get("packages")).containsKey(id)) {
            JSONObject pack = (JSONObject)((JSONObject)Main.packageStore.get("packages")).get(id);
            if (pack.containsKey("installed")) {
                lockStores();
                if (pack.containsKey("files")) {
                    for (Object e : (JSONArray)pack.get("files")) {
                        File f = new File(Bukkit.getWorldContainer(), e.toString());
                        if (f.exists()) {
                            if (!f.delete()) {
                                if (Config.VERBOSE) {
                                    Main.log.warning("Failed to delete file " + f.getName());
                                }
                            } else {
                                checkParent(f);
                            }
                        }
                    }
                    pack.remove("files");
                } else {
                    Main.log.warning("No file listing for package " + id + "!");
                }
                File archive = new File(Main.plugin.getDataFolder(), "cache" + File.separator + id + ".zip");
                if (archive.exists()) {
                    if (!archive.delete() && Config.VERBOSE) {
                        Main.log.warning("Failed to delete archive from cache");
                    }
                }
                pack.remove("installed");
                try {
                    writePackageStore();
                } catch (IOException ex) {
                    unlockStores();
                    throw new MPTException(Config.ERROR_COLOR + "Failed to save changes to disk!");
                }
                unlockStores();
            } else {
                throw new MPTException(Config.ERROR_COLOR + "Package " + Config.ID_COLOR + id + Config.ERROR_COLOR
                        + " is not installed!");
            }
        } else {
            throw new MPTException(Config.ERROR_COLOR + "Cannot find package with id " + Config.ID_COLOR + id);
        }
        unlockStores();
        Telemetry.setDirty();
    }

    public static void checkParent(File file) {
        if (file != null) {
            if (file.getParentFile().listFiles().length == 0) {
                file.getParentFile().delete();
                checkParent(file.getParentFile());
            }
        } else {
            throw new IllegalArgumentException("File cannot be null!");
        }
    }
}
