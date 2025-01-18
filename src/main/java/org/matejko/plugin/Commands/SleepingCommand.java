package main.java.org.matejko.plugin.Commands;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import main.java.org.matejko.plugin.Utilis;
import main.java.org.matejko.plugin.Managers.SleepingManager;
import org.bukkit.ChatColor;

public class SleepingCommand implements CommandExecutor {
    @SuppressWarnings("unused")
	private final Utilis plugin;
    private final SleepingManager sleepingManager;

    // Constructor that accepts both plugin and sleepingManager
    public SleepingCommand(Utilis plugin, SleepingManager sleepingManager) {
        this.plugin = plugin;
        this.sleepingManager = sleepingManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (!player.hasPermission("utilis.ns")) {
                player.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
                return true;
            }
            return handlePlayerCommand(player, args);
        } else if (sender instanceof ConsoleCommandSender) {
            return handleConsoleCommand(sender, args);
        } else {
            sender.sendMessage(ChatColor.RED + "This command can only be executed by a player or the console.");
            return false;
        }
    }

    private boolean handlePlayerCommand(Player player, String[] args) {
        if (args.length == 0) {
            // Show the sleeping status for the player's current world
            if (sleepingManager.isSleepingEnabled(player.getWorld())) {
                player.sendMessage("Sleeping is currently enabled in this world!");
            } else {
                player.sendMessage("Sleeping is currently disabled in this world!");
            }
        } else if (args.length == 1 && (args[0].equalsIgnoreCase("toggle") || args[0].equalsIgnoreCase("t"))) {
            // Toggle sleeping in the player's world
            sleepingManager.toggleSleeping(player.getWorld());
            if (sleepingManager.isSleepingEnabled(player.getWorld())) {
                player.sendMessage("Sleeping is now enabled in this world!");
            } else {
                player.sendMessage("Sleeping is now disabled in this world!");
            }
        } else {
            player.sendMessage(ChatColor.RED + "Invalid command syntax! Use '/ns' to check the sleeping status or '/ns toggle' to toggle sleeping.");
        }
        return true;
    }

    private boolean handleConsoleCommand(CommandSender sender, String[] args) {
        if (args.length == 0) {
            // Show the sleeping status for all worlds
            for (World world : Bukkit.getWorlds()) {
                if (sleepingManager.isSleepingEnabled(world)) {
                    sender.sendMessage("Sleeping is enabled in world '" + world.getName() + "'.");
                } else {
                    sender.sendMessage("Sleeping is disabled in world '" + world.getName() + "'.");
                }
            }
        } else if (args.length == 1) {
            // Check the sleeping status of a specific world (ns worldname)
            String worldName = args[0];
            World world = Bukkit.getWorld(worldName);
            if (world != null) {
                if (sleepingManager.isSleepingEnabled(world)) {
                    sender.sendMessage("Sleeping is currently enabled in world '" + worldName + "'.");
                } else {
                    sender.sendMessage("Sleeping is currently disabled in world '" + worldName + "'.");
                }
            } else {
                sender.sendMessage(ChatColor.RED + "World not found: " + worldName);
            }
        } else if (args.length == 2 && (args[0].equalsIgnoreCase("toggle") || args[0].equalsIgnoreCase("t"))) {
            // Toggle sleeping in the specified world
            String worldName = args[1];
            World world = Bukkit.getWorld(worldName);
            if (world != null) {
                sleepingManager.toggleSleeping(world);
                //sender.sendMessage("Sleeping in world '" + worldName + "' has been toggled.");
            } else {
                sender.sendMessage(ChatColor.RED + "World not found: " + worldName);
            }
        } else {
            sender.sendMessage(ChatColor.RED + "Invalid command! Use '/ns' to check all worlds, '/ns worldname' to check a specific world, or '/ns toggle worldname' to toggle sleeping.");
        }
        return true;
    }
}
