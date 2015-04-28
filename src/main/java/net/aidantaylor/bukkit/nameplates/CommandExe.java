package net.aidantaylor.bukkit.nameplates;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import net.aidantaylor.bukkit.core.Content;

public class CommandExe implements CommandExecutor {
	private PluginDescriptionFile info;
	private Content Content;
	private NamePlates np;
	private String help;

	public CommandExe(NamePlates plugin) {
		info = plugin.getDescription();
		np = plugin;
		
		help = ChatColor.WHITE + "Version" + ChatColor.GREEN
				+ ": Display current version and information\n";
		help += ChatColor.WHITE + "Reload" + ChatColor.GREEN
				+ ": Reload configuration files\n";
		help += ChatColor.WHITE + "Refresh" + ChatColor.GREEN
				+ ": Update player colours\n";
		
		Content = new Content(info);
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equalsIgnoreCase("nameplates")) {
			if (args.length < 1) {
				if (sender instanceof Player && !sender.hasPermission("nameplates.version") && !sender.isOp()) {
					sender.sendMessage(ChatColor.RED + "You do not have permission to access this command.");
				} else {
					Content.versionInfo(sender, info);
					sender.sendMessage(ChatColor.WHITE + "Reload Command: " + ChatColor.GREEN + "/nameplates reload");
				}
			} else if (args[0].toLowerCase().equals("reload")) {
				if (sender instanceof Player && !sender.hasPermission("nameplates.reload") && !sender.isOp()) {
					sender.sendMessage(ChatColor.RED + "You do not have permission to access this command.");
				} else {
					sender.sendMessage(ChatColor.DARK_GREEN + "Reloading NamePlates...");
					np.saveDefaultConfig();
					np.reload();
					sender.sendMessage(ChatColor.DARK_GREEN + "Done.");
				}
			} else if (args[0].toLowerCase().equals("refresh")) {
				if (sender instanceof Player && !sender.hasPermission("nameplates.refresh") && !sender.isOp()) {
					sender.sendMessage(ChatColor.RED + "You do not have permission to access this command.");
				} else {
					sender.sendMessage(ChatColor.DARK_GREEN + "Refreshing NamePlates...");
					np.refreshPlates();
					sender.sendMessage(ChatColor.DARK_GREEN + "Done.");
				}
			} else if (args[0].toLowerCase().equals("version") || args[0].toLowerCase().equals("v")) {
				if (sender instanceof Player && !sender.hasPermission("nameplates.version") && !sender.isOp()) {
					sender.sendMessage(ChatColor.RED + "You do not have permission to access this command.");
				} else {
					Content.versionInfo(sender, info);
					sender.sendMessage(ChatColor.WHITE + "Reload Command: " + ChatColor.GREEN + "/nplates reload");
				}
			} else if (args[0].toLowerCase().equals("help") || args[0].toLowerCase().equals("?") || args[0].toLowerCase().equals("h")) {
				if (sender instanceof Player && !sender.hasPermission("nameplates.reload") && !sender.isOp()) {
					sender.sendMessage(ChatColor.RED + "You do not have permission to access this command.");
				} else {
					Content.showPaginated((Player) sender, "nameplates help", help, "Help");
				}
			} else {
				sender.sendMessage(ChatColor.DARK_RED + "That NamePlates command was not found, for a list of NamePlates commands do /nplates for help");
			}
		}

		return false;
	}
}