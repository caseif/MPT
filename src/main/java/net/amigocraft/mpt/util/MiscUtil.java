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

import static net.amigocraft.mpt.util.Config.*;

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
import java.security.MessageDigest;

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
	 * @throws MPTException if the store is already locked
	 */
	public static void lockStores() throws MPTException {
		if (Main.LOCKED)
			throw new MPTException(ERROR_COLOR + "Failed to lock local stores! Perhaps a task is currently " +
					"running or has uncleanly terminated?");
		Main.LOCKED = true;
	}

	/**
	 * Unlocks the local stores.
	 */
	public static void unlockStores(){
		Main.LOCKED = false;
	}

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
					String line;
					StringBuilder content = new StringBuilder();
					while ((line = reader.readLine()) != null)
						content.append(line);
					JsonObject json = parser.parse(content.toString()).getAsJsonObject(); // parse JSON object
					// vefify remote config is valid
					if (json.has("packages") && json.get("packages").isJsonObject()){
						return json;
					}
					else
						throw new MPTException(ERROR_COLOR + "[MPT] Index for repository at " + path +
								"is missing required elements!");
				}
				else {
					String error = ERROR_COLOR + "[MPT] Remote returned bad response code! (" + response + ")";
					if (!http.getResponseMessage().isEmpty())
						error += " The remote says: " + ChatColor.GRAY +
								ChatColor.ITALIC + http.getResponseMessage();
					throw new MPTException(error);
				}
			}
			else
				throw new MPTException(ERROR_COLOR + "[MPT] Bad protocol for URL!");
		}
		catch (MalformedURLException ex){
			throw new MPTException(ERROR_COLOR + "[MPT] Cannot parse URL!");
		}
		catch (IOException ex){
			throw new MPTException(ERROR_COLOR + "[MPT] Cannot open connection to URL!");
		}
		catch (JsonParseException ex){
			throw new MPTException(ERROR_COLOR + "[MPT] Repository index is not valid JSON!");
		}
	}

	public static String sha1(String path){ // convenience method for calculating SHA-1 hash of a file
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-1");
			FileInputStream fis = new FileInputStream(path);
			byte[] dataBytes = new byte[1024];

			int nread;
			while ((nread = fis.read(dataBytes)) != -1){
				md.update(dataBytes, 0, nread);
			}
			fis.close();

			byte[] mdbytes = md.digest();

			StringBuilder sb = new StringBuilder();
			for (byte mdbyte : mdbytes){
				String hex = Integer.toHexString(0xff & mdbyte);
				if (hex.length() == 1)
					sb.append('0');
				sb.append(hex);
			}
			return sb.toString();
		}
		catch (Exception ex){
			ex.printStackTrace();
			System.err.println("Failed to calculate checksum for " + path);
		}

		return null;
	}

}
