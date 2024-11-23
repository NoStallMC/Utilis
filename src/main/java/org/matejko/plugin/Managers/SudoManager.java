package main.java.org.matejko.plugin.Managers;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SudoManager implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!label.equalsIgnoreCase("sudo")) {
            return false;
        }

        if (!sender.hasPermission("utilis.sudo")) {
            sendError(sender, "You do not have permission to use this command.");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage("Usage: /sudo <player> <command/message>");
            return true;
        }

        Player target = findPlayer(args[0]);
        if (target == null) {
            sendError(sender, "Player not found: " + args[0]);
            return true;
        }

        // Construct the message from the arguments after the player's name
        StringBuilder messageBuilder = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            messageBuilder.append(args[i]).append(" ");
        }
        String message = messageBuilder.toString().trim();

        // Check if it's a command or a chat message
        if (message.startsWith("/")) {
            boolean success = Bukkit.dispatchCommand(target, message.substring(1));
            if (success) {
                sendSuccess(sender,ChatColor.GRAY + "Forced " + target.getDisplayName() + ChatColor.GRAY + " to execute command: " + ChatColor.WHITE + message);
            } else {
                sendError(sender, "Failed to execute command: " + message);
            }
        } else {
            target.chat(message);
            sendSuccess(sender, ChatColor.GRAY + "Forced " + target.getDisplayName() + ChatColor.GRAY + " to send message: "+ ChatColor.WHITE + message);
        }

        return true;
    }

    private Player findPlayer(String partialName) {
        String lowerPartialName = partialName.toLowerCase();

        // Check for players whose names contain the partial name
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getName().toLowerCase().contains(lowerPartialName)) {
                return player;
            }
        }
        return null;
    }

    public static void sendError(CommandSender sender, String message) {
        sender.sendMessage(ChatColor.RED + message);
    }

    public static void sendSuccess(CommandSender sender, String message) {
        sender.sendMessage(message);
    }
}