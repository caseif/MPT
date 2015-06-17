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
package net.caseif.mpt;

import net.caseif.mpt.command.CommandManager;
import net.caseif.mpt.json.JSONPrettyPrinter;
import net.caseif.mpt.util.Config;
import net.caseif.mpt.util.MPTException;
import net.caseif.mpt.util.MiscUtil;

import net.gravitydevelopment.updater.Updater;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Logger;

public class Main extends JavaPlugin {

    public static Main plugin; // plugin instance
    public static Logger log; // logger instance

    public static long mainThreadId;

    public static JSONObject repoStore = null; // repo store
    public static JSONObject packageStore = null; // package store

    public static boolean LOCKED = false;

    @Override
    public void onEnable() {
        plugin = this;
        log = this.getLogger();

        mainThreadId = Thread.currentThread().getId();

        saveDefaultConfig();

        File rStoreFile = new File(getDataFolder(), "repositories.json");
        JSONParser parser = new JSONParser();
        if (rStoreFile.exists()) { // repo store has been initialized
            log.info("Loading local repository store...");
            try {
                repoStore = (JSONObject)parser.parse(new FileReader(rStoreFile));
            } catch (IOException | ParseException ex) {
                ex.printStackTrace();
                log.severe("Failed to load repository store!");
            }
        } else { // we need to initialize the repo store
            initializeRepoStore(rStoreFile);
        }

        File pStoreFile = new File(getDataFolder(), "packages.json");
        if (pStoreFile.exists()) { // package store has been initialized
            log.info("Loading local package store...");
            try {
                packageStore = ((JSONObject)parser.parse(new FileReader(pStoreFile)));
            } catch (IOException | ParseException ex) {
                ex.printStackTrace();
                log.severe("Failed to load package store!");
            }
        } else { // we need to initialize the package store
            initializePackageStore(pStoreFile);
        }

        this.getCommand("mpt").setExecutor(new CommandManager()); // register the CommandManager class

        // initialize auto-updater
        if (Config.AUTO_UPDATE)
            new Updater(this, 88254, this.getFile(), Updater.UpdateType.DEFAULT, true);

        // initialize plugin metrics
        if (Config.METRICS) {
            try {
                Metrics metrics = new Metrics(this);
                metrics.start();
            } catch (IOException ex) {
                log.warning("Failed to initialize plugin metrics!");
            }
        }

        log.info(this + " has been enabled!");
    }

    @Override
    public void onDisable() {
        repoStore = null;
        packageStore = null;
        LOCKED = false;
        log = null;
        plugin = null;
        getLogger().info(this + " has been disabled!");
    }

    public static void initializeRepoStore(File file) {
        log.info("Initializing local repository store...");
        try {
            MiscUtil.lockStores();
            JSONObject repos = new JSONObject(); // create an empty array
            repoStore = new JSONObject(); // create an empty object
            repoStore.put("repositories", repos); // add the array to it
            if (!file.getParentFile().exists())
                file.getParentFile().mkdir();
            file.createNewFile(); // create the file
            BufferedWriter writer = new BufferedWriter(new FileWriter(file)); // get a writer
            writer.write(JSONPrettyPrinter.toJSONString(repoStore)); // convert the JSON object to a string and write it
            writer.flush();
        } catch (IOException ex) {
            ex.printStackTrace();
            log.severe("Failed to initialize repository store!");
        } catch (MPTException ex) {
            log.severe(ex.getMessage());
        }
        MiscUtil.unlockStores();
    }

    public static void initializePackageStore(File file) {
        log.info("Initializing local package store...");
        try {
            MiscUtil.lockStores();
            JSONObject packages = new JSONObject(); // create an empty array
            packageStore = new JSONObject(); // create an empty object
            packageStore.put("packages", packages); // add the array to it
            try {
                if (!file.getParentFile().exists())
                    file.getParentFile().mkdir();
                file.createNewFile(); // create the file
                BufferedWriter writer = new BufferedWriter(new FileWriter(file)); // get a writer
                writer.write(JSONPrettyPrinter.toJSONString(packageStore)); // convert the JSON object to a string and write it
                writer.flush();
            } catch (IOException ex) {
                ex.printStackTrace();
                log.severe("Failed to initialize package store!");
            }
            MiscUtil.unlockStores();
        } catch (MPTException ex) {
            log.severe(ex.getMessage());
        }
    }

}
