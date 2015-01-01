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
import static net.amigocraft.mpt.util.MiscUtil.*;

import net.amigocraft.mpt.Main;
import net.amigocraft.mpt.util.MPTException;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.json.simple.JSONObject;

import java.util.Map;
import java.util.Set;

public class UpgradeCommand extends SubcommandManager {

	public UpgradeCommand(CommandSender sender, String[] args){
		super(sender, args);
	}

	@Override
	@SuppressWarnings("unchecked")
	public void handle(){
		if (!checkPerms()) return;
		Bukkit.getScheduler().runTaskAsynchronously(Main.plugin, new Runnable() {
			public void run(){
				if (args.length > 1){
					threadSafeSendMessage(sender, INFO_COLOR + "[MPT] Upgrading selected packages...");
					for (int i = 1; i < args.length; i++){
						String id = args[i];
						try {
							threadSafeSendMessage(sender, INFO_COLOR + "[MPT] Attempting to upgrade package " +
									ID_COLOR + id);
							String v = upgradePackage(id);
							if (v != null)
								threadSafeSendMessage(sender, INFO_COLOR + "[MPT] Successfully upgraded " +
										ID_COLOR + id + INFO_COLOR + " to " + ID_COLOR + "v" + v);
							else
								threadSafeSendMessage(sender, INFO_COLOR + "[MPT] Package " + ID_COLOR + id +
										INFO_COLOR + " is already up-to-date");
						}
						catch (MPTException ex){
							threadSafeSendMessage(sender, ERROR_COLOR + "[MPT] " + ex.getMessage());
						}
					}
					// the extra task is so the messages will be sent in the proper order
					Bukkit.getScheduler().runTask(Main.plugin, new Runnable() {
						public void run(){
							threadSafeSendMessage(sender, INFO_COLOR + "[MPT] Finished uprgading packages!");
						}
					});
				}
				else {
					if (Main.packageStore.containsKey("packages")){
						threadSafeSendMessage(sender, INFO_COLOR + "[MPT] Upgrading all installed packages...");
						Set<Map.Entry> entries = ((JSONObject)Main.packageStore.get("packages")).entrySet();
						for (Map.Entry e : entries){
							if (((JSONObject)e.getValue()).containsKey("installed")){
								try {
									String id = e.getKey().toString();
									threadSafeSendMessage(sender, INFO_COLOR +
											"[MPT] Attempting to upgrade package " + ID_COLOR + id);
									String v = upgradePackage(id);
									if (v != null)
										threadSafeSendMessage(sender, INFO_COLOR + "[MPT] Successfully upgraded " +
												ID_COLOR + id + INFO_COLOR + " to " + ID_COLOR + "v" + v);
								}
								catch (MPTException ex){
									threadSafeSendMessage(sender, ERROR_COLOR + "[MPT] " + ex.getMessage());
								}
							}
						}
						// the extra task is so the messages will be sent in the proper order
						Bukkit.getScheduler().runTask(Main.plugin, new Runnable() {
							public void run(){
								threadSafeSendMessage(sender, INFO_COLOR+"[MPT] Finished uprgading packages!");
							}
						});
					}
					else
						threadSafeSendMessage(sender, ERROR_COLOR + "[MPT] Local package store is malformed!");
				}
			}
		});;
	}

	/**
	 * Attempts to upgrade a package by the given ID
	 * <strong>This method may not be called from the main thread.</strong>
	 * @param id the ID of the package to upgrade
	 * @return the new version, or null if the package was not upgraded
	 * @throws MPTException if called from the main thread, or if something goes wrong while upgrading
	 */
	public static String upgradePackage(String id) throws MPTException {
		if (Thread.currentThread().getId() == Main.mainThreadId)
			throw new MPTException(ERROR_COLOR + "Packages may not be upgraded from the main thread!");
		if (Main.packageStore.containsKey("packages") &&
				((JSONObject)Main.packageStore.get("packages")).containsKey(id)){
			JSONObject pack = (JSONObject)((JSONObject)Main.packageStore.get("packages")).get(id);
			if (pack.containsKey("installed")){
				if (pack.containsKey("version")){
					int diff = compareVersions(pack.get("installed").toString(), pack.get("version").toString());
					if (diff > 0){
						// easy way out
						RemoveCommand.removePackage(id);
						InstallCommand.installPackage(id);
						return pack.get("version").toString();
					}
					else // up-to-date
						return null;
				}
				else
					throw new MPTException(ERROR_COLOR + "Package " + ID_COLOR + id + ERROR_COLOR +
							" is missing version string! Type " + COMMAND_COLOR + "/mpt update" + ERROR_COLOR +
							" to fix this.");
			}
			else
				throw new MPTException(ERROR_COLOR + "Package " + ID_COLOR + id + ERROR_COLOR + " is not installed!");
		}
		return null;
	}
}
