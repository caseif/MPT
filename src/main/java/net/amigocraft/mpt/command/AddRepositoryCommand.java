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

import static net.amigocraft.mpt.util.MiscUtil.threadSafeSendMessage;

import net.amigocraft.mpt.Main;

import com.google.gson.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;

public class AddRepositoryCommand extends SubcommandManager {

	public AddRepositoryCommand(CommandSender sender, String[] args){
		super(sender, args);
	}

	@Override
	public void handle(){
		if (sender.hasPermission("mpt.repo.add")){
			if (args.length == 2){
				final String path = args[1];
				// get the main array from the JSON object
				JsonArray array = Main.repoStore.getAsJsonArray("repositories");
				// verify the repo hasn't already been added
				for (JsonElement e : array){ // iterate repos in local store
					JsonObject o = e.getAsJsonObject();
					// check URL
					if (o.has("url") && o.get("url").getAsString().equalsIgnoreCase(path)){
						sender.sendMessage(ChatColor.RED + "[MPT] The repository at that URL has already been added!");
						return;
					}
				}
				// no way we're making the main thread wait for us to open and read the stream
				Bukkit.getScheduler().runTaskAsynchronously(Main.plugin, new Runnable(){
					public void run(){
						threadSafeSendMessage(sender, ChatColor.DARK_PURPLE +
								"[MPT] Attempting connection to repository...");
						connectAndStore(path); // in separate method for organization purposes
					}

				});
			}
			else if (args.length < 2)
				sender.sendMessage(ChatColor.RED + "[MPT] Too few arguments! Type " + ChatColor.DARK_PURPLE +
						"/mpt help " + ChatColor.RED + "for help");
			else
				sender.sendMessage(ChatColor.RED + "[MPT] Too many arguments! Type " + ChatColor.DARK_PURPLE +
						"/mpt help " + ChatColor.RED + "for help");
		}
		else
			sender.sendMessage(ChatColor.RED + "[MPT] You do not have access to this command!");
	}

	private void connectAndStore(String path){
		try {
			URL url = new URL(path + (!path.endsWith("/") ? "/" : "") + "mpt.json"); // get URL object for data file
			URLConnection conn = url.openConnection();
			if (conn instanceof HttpURLConnection){
				HttpURLConnection http = (HttpURLConnection)conn; // cast the connection
				int response = http.getResponseCode(); // get the response
				if (response >= 200 && response <= 299){ // verify the remote isn't upset at us
					InputStream is = http.getInputStream(); // open a stream to the URL
					BufferedReader reader = new BufferedReader(new InputStreamReader(is)); // get a reader
					JsonParser parser = new JsonParser(); // get a new parser
					JsonObject json = parser.parse(reader).getAsJsonObject(); // parse JSON object from stream
					// vefify remote config is valid
					if (json.has("name") && json.has("packages") && json.get("packages").isJsonArray()){
						String id = json.get("id").getAsString(); // get ID from remote
						try { // inner try block is necessary so local issues aren't misreported as remote
							File store = new File(Main.plugin.getDataFolder(), "repositories.json");
							if (!store.exists())
								Main.initializeRepoStore(store); // gotta initialize it before using it
							JsonObject repoElement = new JsonObject(); // create a new JSON object
							repoElement.addProperty("id", id); // set the repo name (determined by remote config)
							repoElement.addProperty("url", path); // set the repo URL
							synchronized(Main.REPO_STORE_LOCK){
								Main.repoStore.getAsJsonArray("repositories").add(repoElement);
							}
							FileWriter writer = new FileWriter(store); // get a writer for the store file
							writer.write(Main.gson.toJson(Main.repoStore)); // write to disk
							writer.flush();
							// apt-get doesn't fetch packages when a repo is added, so I'm following that precedent
							threadSafeSendMessage(sender, ChatColor.DARK_PURPLE + "[MPT] Successfully added " +
									"repository under ID " + ChatColor.AQUA + id + ChatColor.DARK_PURPLE +
									" to local store! You may now use " + ChatColor.GOLD + "/mpt update" +
									ChatColor.DARK_PURPLE + " to fetch available packages.");
						}
						catch (IOException ex){
							threadSafeSendMessage(sender, ChatColor.RED + "[MPT] Failed to add repository to local " +
									"store!");
						}
					}
					else
						threadSafeSendMessage(sender, ChatColor.RED + "[MPT] Repository index is missing required " +
								"elements!");
				}
				else {
					threadSafeSendMessage(sender, ChatColor.RED + "[MPT] Remote returned bad response code! (" +
							response + ")");
					if (!http.getResponseMessage().isEmpty())
						threadSafeSendMessage(sender, ChatColor.RED + "[MPT] The remote says: " + ChatColor.GRAY +
								ChatColor.ITALIC + http.getResponseMessage());
				}
			}
			else
				threadSafeSendMessage(sender, ChatColor.RED + "[MPT] Bad protocol for URL!");
		}
		catch (MalformedURLException ex){
			threadSafeSendMessage(sender, ChatColor.RED + "[MPT] Cannot parse URL!");
		}
		catch (IOException ex){
			threadSafeSendMessage(sender, ChatColor.RED + "[MPT] Cannot open connection to URL!");
		}
		catch (JsonParseException ex){
			threadSafeSendMessage(sender, ChatColor.RED + "[MPT] Repository index is not valid JSON!");
		}
	}
}
