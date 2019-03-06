package org.sandcast.canopy.command;

import com.google.common.base.Joiner;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.util.ChatPaginator;

public class CanopyCommand implements CommandExecutor {

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, String[] args) {
        if (!sender.hasPermission("canopy.command")) {
            sender.sendMessage(ChatColor.RED + "You do not have the permission to execute this command.");
            return true;
        }

        final PluginDescriptionFile description = Bukkit.getPluginManager().getPlugin("CanopyPlugin").getDescription();
        sender.sendMessage(ChatColor.GREEN + description.getName() + " v" + description.getVersion() + ChatColor.GOLD + " by " + Joiner.on(' ').join(description.getAuthors()));

        final StringBuilder builder = new StringBuilder();
        for (int i = 0; i != ChatPaginator.GUARANTEED_NO_WRAP_CHAT_PAGE_WIDTH - 2; i++) {
            builder.append("=");
        }
        final String line = builder.toString();
        sender.sendMessage(ChatColor.RESET + line);
        sender.sendMessage(ChatColor.GOLD + "SCHEMATICS : ");
        sender.sendMessage(line);
        sender.sendMessage(ChatColor.GOLD + "PERMISSIONS : ");

        for (final Permission permission : description.getPermissions()) {
            sender.sendMessage(sender.hasPermission(permission) ?
                    (ChatColor.GREEN + "- You have the permission " + ChatColor.BOLD + permission.getName() + ChatColor.RESET + ChatColor.GREEN + ".") :
                    (ChatColor.RED + "- You do not have the permission " + ChatColor.BOLD + permission.getName() + ChatColor.RESET + ChatColor.RED + ".")
            );
        }
        return true;
    }

}