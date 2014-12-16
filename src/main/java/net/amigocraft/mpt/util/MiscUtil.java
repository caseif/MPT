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

}
