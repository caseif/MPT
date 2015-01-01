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
package net.amigocraft.mpt.util;

import net.amigocraft.mpt.Main;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

public class Config {

	public static final ChatColor INFO_COLOR = ChatColor.DARK_PURPLE;
	public static final ChatColor ERROR_COLOR = ChatColor.RED;
	public static final ChatColor COMMAND_COLOR = ChatColor.GOLD;
	public static final ChatColor ID_COLOR = ChatColor.AQUA;

	public static final boolean VERBOSE;
	public static final boolean KEEP_ARCHIVES;
	public static final boolean AUTO_UPDATE;
	public static final boolean METRICS;
	public static final boolean ENFORCE_CHECKSUM;
	public static final boolean DISALLOW_OVERWRITE;
	public static final boolean DISALLOW_MERGE;
	public static final List<String> DISALLOWED_EXTENSIONS;
	public static final List<String> DISALLOWED_DIRECTORIES;

	static {
		FileConfiguration cfg = Main.plugin.getConfig();
		VERBOSE = cfg.getBoolean("verbose");
		KEEP_ARCHIVES = cfg.getBoolean("keep-archives");
		AUTO_UPDATE = cfg.getBoolean("auto-update");
		METRICS = cfg.getBoolean("metrics");
		ENFORCE_CHECKSUM = cfg.getBoolean("enforce-checksum");
		DISALLOW_OVERWRITE = cfg.getBoolean("disallow-overwrite");
		DISALLOW_MERGE = cfg.getBoolean("disallow-merge");
		DISALLOWED_EXTENSIONS = cfg.getStringList("disallowed-extensions");
		DISALLOWED_DIRECTORIES = cfg.getStringList("disalllowed-directories");
	}

}
