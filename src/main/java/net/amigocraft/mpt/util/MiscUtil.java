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

import net.amigocraft.mpt.Main;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

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

}
