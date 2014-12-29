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
import static net.amigocraft.mpt.util.MiscUtil.*;

import com.google.gson.JsonObject;
import net.amigocraft.mpt.Main;
import net.amigocraft.mpt.util.Config;
import net.amigocraft.mpt.util.MPTException;
import net.amigocraft.mpt.util.MiscUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

public class InstallCommand extends SubcommandManager {

	public InstallCommand(CommandSender sender, String[] args){
		super(sender, args);
	}

	@Override
	public void handle(){
		if (args.length > 1){
			Bukkit.getScheduler().runTaskAsynchronously(Main.plugin, new Runnable() {
				public void run(){
					for (int i = 1; i < args.length; i++){
						threadSafeSendMessage(sender, INFO_COLOR + "[MPT] Fetching package " + ID_COLOR + args[i] +
								INFO_COLOR + "...");
						try {
							downloadPackage(args[i]);
						}
						catch (MPTException ex){
							threadSafeSendMessage(sender, ERROR_COLOR + "[MPT] " + ex.getMessage());
						}
					}
				}
			});
		}
		else
			sender.sendMessage(ERROR_COLOR + "[MPT] Too few arguments! Type " + COMMAND_COLOR + "/mpt help" +
			ERROR_COLOR + " for help.");
	}

	private void downloadPackage(String id) throws MPTException {
		JsonObject packages = Main.packageStore.getAsJsonObject("packages");
		if (packages != null){
			JsonObject pack = packages.getAsJsonObject(id);
			if (pack != null){
				if (pack.has("name") && pack.has("version") && pack.has("url")){
					if (pack.has("sha1") || !Config.ENFORCE_CHECKSUM){
						String name = pack.get("name").getAsString();
						String version = pack.get("version").getAsString();
						String url = pack.get("url").getAsString();
						String sha1 = pack.has("sha1") ? pack.get("sha1").getAsString() : "";
						threadSafeSendMessage(sender, INFO_COLOR + "Installing package " + ID_COLOR + name + " v" +
								version + INFO_COLOR + "...");
						try {URLConnection conn = new URL(url).openConnection();
							conn.connect();
							ReadableByteChannel rbc = Channels.newChannel(conn.getInputStream());
							File file = new File(Main.plugin.getDataFolder(), "cache" + File.separator + id + ".zip");
							file.setReadable(true, false);
							file.setWritable(true, false);
							file.getParentFile().mkdirs();
							file.createNewFile();
							FileOutputStream os = new FileOutputStream(file);
							os.getChannel().transferFrom(rbc, 0, MiscUtil.getFileSize(new URL(url)));
							os.close();
							extractPackage(id);
						}
						catch (IOException ex){
							throw new MPTException("Failed to download package " + ID_COLOR + id + INFO_COLOR + "!");
						}
					}
					else
						throw new MPTException("Package " + ID_COLOR + id + INFO_COLOR +
								" is missing SHA-1 checksum! Aborting...");
				}
				else
					throw new MPTException("Package " + ID_COLOR + id + INFO_COLOR + " is missing required elements!");
			}
			else
				throw new MPTException("Cannot find package " + ID_COLOR + id);
		}
		else {
			throw new MPTException("Package store is malformed!");
		}
	}

	private void extractPackage(String id) throws MPTException {
		//TODO
	}
}
