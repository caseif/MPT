/*
 * MPT (Map Packaging Tool)
 *
 * Copyright (c) 2014 Maxim Roncace
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package net.amigocraft.mpt;

import com.google.gson.*;
import net.amigocraft.mpt.command.CommandManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.util.logging.Logger;

public class Main extends JavaPlugin {

	public static Main plugin; // plugin instance
	public static Logger log; // logger instance

	public static Gson gson = null;
	public static JsonObject repos = null; // repo store
	public static JsonObject packages = null; // package store

	@Override
	public void onEnable(){
		plugin = this;
		log = this.getLogger();

		gson = new GsonBuilder().setPrettyPrinting().create(); // so the stores look decent when saved to disk

		File reposFile = new File(getDataFolder(), "repositories.yml");
		if (reposFile.exists()){ // repo store has been initialized
			log.info("Loading local repository store...");
			try {
				String line;
				StringBuilder builder = new StringBuilder(); // create a builder
				while ((line = new BufferedReader(new FileReader(reposFile)).readLine()) != null){ // read line-by-line
					builder.append(line); // build
				}
				String content = builder.toString();
				JsonParser parser = new JsonParser(); // create a new parser
				repos = parser.parse(content).getAsJsonObject(); // parse the read string into a JSON object
			}
			catch (IOException ex){
				ex.printStackTrace();
				log.severe("Failed to load repository store!");
			}
		}
		else { // we need to initialize the repo store
			log.info("Initializing local repository store...");

			JsonArray repoArray = new JsonArray(); // create an empty array
			repos = new JsonObject(); // create an empty object
			repos.add("repositories", repoArray); // add the array to it
			try {
				reposFile.createNewFile(); // create the file
				BufferedWriter writer = new BufferedWriter(new FileWriter(reposFile)); // get a writer
				writer.write(repos.toString()); // convert the JSON object to a string and write it
			}
			catch (IOException ex){
				ex.printStackTrace();
				log.severe("Failed to initialize repository store!");
			}
		}

		File packagesFile = new File(getDataFolder(), "maps.yml");
		if (packagesFile.exists()){ // package store has been initialized
			log.info("Loading local package store...");
			try {
				String line;
				StringBuilder builder = new StringBuilder(); // create a builder
				while ((line = new BufferedReader(new FileReader(packagesFile)).readLine()) != null){ // read
					builder.append(line); // build
				}
				String content = builder.toString();
				JsonParser parser = new JsonParser(); // create a new parser
				packages = parser.parse(content).getAsJsonObject(); // parse the read string into a JSON object
			}
			catch (IOException ex){
				ex.printStackTrace();
				log.severe("Failed to load package store!");
			}
		}
		else { // we need to initialize the package store
			log.info("Initializing local package store...");
			JsonArray packageArray = new JsonArray(); // create an empty array
			packages = new JsonObject(); // create an empty object
			packages.add("packages", packageArray); // add the array to it
			try {
				packagesFile.createNewFile(); // create the file
				BufferedWriter writer = new BufferedWriter(new FileWriter(packagesFile)); // get a writer
				writer.write(packages.toString()); // convert the JSON object to a string and write it
			}
			catch (IOException ex){
				ex.printStackTrace();
				log.severe("Failed to initialize package store!");
			}
		}

		this.getCommand("mpt").setExecutor(new CommandManager()); // register the CommandManager class

		log.info(this + " has been enabled!");
	}

	@Override
	public void onDisable(){
		log.info(this + " has been disabled!");
		log = null;
		plugin = null;
	}

}
