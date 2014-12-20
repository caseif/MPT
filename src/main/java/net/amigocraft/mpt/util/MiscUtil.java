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
package net.amigocraft.mpt.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import net.amigocraft.mpt.Main;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Calendar;

public class MiscUtil {

	/**
	 * Convenience method allow CommandSender#sendMessage(String) to be called from async tasks.
	 * @param sender the sender to send the message to
	 * @param message the message to send
	 */
	public static void threadSafeSendMessage(final CommandSender sender, final String message){
		Bukkit.getScheduler().runTask(Main.plugin, new Runnable() {
			public void run(){
				sender.sendMessage(message);
			}
		});
	}

	/**
	 * Attempts to lock the local stores and returns false if they are already locked.
	 * @return whether the stores could be locked
	 */
	public static boolean lockStores(){
		if (Main.LOCKED)
			return false;
		Main.LOCKED = true;
		return true;
	}

	/**
	 * Unlocks the local stores.
	 */
	public static void unlockStores(){
		Main.LOCKED = false;
	}

	/*public static void logToFile(String msg, String prefix){
		if (Config.LOG_TO_FILE){
			try {
				File logFile = new File(Main.plugin.getDataFolder(), "mpt.log");
				if (!logFile.exists())
					logFile.createNewFile();
				FileWriter writer = new FileWriter(logFile);
				Calendar cal = Calendar.getInstance();
				cal.setTimeInMillis(System.currentTimeMillis());
				String dateTime = "[" + cal.get(Calendar.YEAR) + "-" +
						(cal.get(Calendar.MONTH) < 9 ?
								"0" + (cal.get(Calendar.MONTH) + 1) :
								(cal.get(Calendar.MONTH) + 1)) + "-" +
						(cal.get(Calendar.DAY_OF_MONTH) < 9 ?
								"0" + (cal.get(Calendar.DAY_OF_MONTH) + 1) :
								(cal.get(Calendar.DAY_OF_MONTH) + 1)) + "T" +
						(cal.get(Calendar.HOUR_OF_DAY) < 9 ?
								"0" + (cal.get(Calendar.HOUR_OF_DAY) + 1) :
								(cal.get(Calendar.HOUR_OF_DAY) + 1)) + ":" +
						(cal.get(Calendar.MINUTE) < 9 ?
								"0" + (cal.get(Calendar.MINUTE) + 1) :
								(cal.get(Calendar.MINUTE) + 1)) + ":" +
						(cal.get(Calendar.SECOND) < 9 ?
								"0" + (cal.get(Calendar.SECOND) + 1) :
								(cal.get(Calendar.SECOND) + 1)) + "]";
				writer.append(dateTime + " " + " [" + prefix + "] " + msg);
			}
			catch (IOException ex){
				// do nothing lest we spam the console :P
			}
		}
	}*/

	public static JsonObject getRemoteIndex(String path) throws MPTException {
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
					if (json.has("id") && json.has("packages") && json.get("packages").isJsonArray()){
						return json;
					}
					else
						throw new MPTException(ChatColor.RED + "[MPT] Index for repository at " + path +
								"is missing required elements!");
				}
				else {
					String error = ChatColor.RED + "[MPT] Remote returned bad response code! (" + response + ")";
					if (!http.getResponseMessage().isEmpty())
						error += " The remote says: " + ChatColor.GRAY +
								ChatColor.ITALIC + http.getResponseMessage();
					throw new MPTException(error);
				}
			}
			else
				throw new MPTException(ChatColor.RED + "[MPT] Bad protocol for URL!");
		}
		catch (MalformedURLException ex){
			throw new MPTException(ChatColor.RED + "[MPT] Cannot parse URL!");
		}
		catch (IOException ex){
			throw new MPTException(ChatColor.RED + "[MPT] Cannot open connection to URL!");
		}
		catch (JsonParseException ex){
			throw new MPTException(ChatColor.RED + "[MPT] Repository index is not valid JSON!");
		}
	}

}
