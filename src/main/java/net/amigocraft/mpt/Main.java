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
package net.amigocraft.mpt;

import com.google.gson.*;
import net.amigocraft.mpt.command.CommandManager;
import net.amigocraft.mpt.util.MPTException;
import net.amigocraft.mpt.util.MiscUtil;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.util.logging.Logger;

public class Main extends JavaPlugin {

	public static Main plugin; // plugin instance
	public static Logger log; // logger instance

	public static Gson gson = null;
	public static JsonObject repoStore = null; // repo store
	public static JsonObject packageStore = null; // package store

	public static boolean LOCKED = false;

	@Override
	public void onEnable(){
		plugin = this;
		log = this.getLogger();

		gson = new GsonBuilder().setPrettyPrinting().create(); // so the stores look decent when saved to disk

		File rStoreFile = new File(getDataFolder(), "repositories.json");
		JsonParser parser = new JsonParser();
		if (rStoreFile.exists()){ // repo store has been initialized
			log.info("Loading local repository store...");
			try {
				repoStore = parser.parse(new FileReader(rStoreFile)).getAsJsonObject();
			}
			catch (IOException ex){
				ex.printStackTrace();
				log.severe("Failed to load repository store!");
			}
		}
		else { // we need to initialize the repo store
			initializeRepoStore(rStoreFile);
		}

		File pStoreFile = new File(getDataFolder(), "packages.json");
		if (pStoreFile.exists()){ // package store has been initialized
			log.info("Loading local package store...");
			try {
				packageStore = parser.parse(new FileReader(pStoreFile)).getAsJsonObject();
			}
			catch (IOException ex){
				ex.printStackTrace();
				log.severe("Failed to load package store!");
			}
		}
		else { // we need to initialize the package store
			initializePackageStore(pStoreFile);
		}

		this.getCommand("mpt").setExecutor(new CommandManager()); // register the CommandManager class

		log.info(this + " has been enabled!");
	}

	@Override
	public void onDisable(){
		gson = null;
		repoStore = null;
		packageStore = null;
		LOCKED = false;
		log = null;
		plugin = null;
		getLogger().info(this + " has been disabled!");
	}

	public static void initializeRepoStore(File file){
		log.info("Initializing local repository store...");
		try {
			MiscUtil.lockStores();
			JsonObject repos = new JsonObject(); // create an empty array
			repoStore = new JsonObject(); // create an empty object
			repoStore.add("repositories", repos); // add the array to it
			if (!file.getParentFile().exists())
				file.getParentFile().mkdir();
			file.createNewFile(); // create the file
			BufferedWriter writer = new BufferedWriter(new FileWriter(file)); // get a writer
			writer.write(gson.toJson(repoStore)); // convert the JSON object to a string and write it
			writer.flush();
		}
		catch (IOException ex){
			ex.printStackTrace();
			log.severe("Failed to initialize repository store!");
		}
		catch(MPTException ex){
			log.info(ex.getMessage());
		}
		MiscUtil.unlockStores();
	}

	public static void initializePackageStore(File file){
		log.info("Initializing local package store...");
		try {
			MiscUtil.lockStores();
			JsonObject packages = new JsonObject(); // create an empty array
			packageStore = new JsonObject(); // create an empty object
			packageStore.add("packages", packages); // add the array to it
			try {
				if (!file.getParentFile().exists())
					file.getParentFile().mkdir();
				file.createNewFile(); // create the file
				BufferedWriter writer = new BufferedWriter(new FileWriter(file)); // get a writer
				writer.write(gson.toJson(packageStore)); // convert the JSON object to a string and write it
				writer.flush();
			}
			catch (IOException ex){
				ex.printStackTrace();
				log.severe("Failed to initialize package store!");
			}
			MiscUtil.unlockStores();
		}
		catch (MPTException ex){
			log.info(ex.getMessage());
		}
	}

}
