package main.java.org.matejko.utilis.Managers;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import main.java.org.matejko.utilis.FileCreator.Messages;

public class SudoManager implements CommandExecutor {
    private final Messages messages;

    public SudoManager(Messages messages) {
        this.messages = messages;
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!label.equalsIgnoreCase("sudo")) {
            return false;
        }
        if (!sender.hasPermission("utilis.sudo")) {
            sendError(sender, messages.getMessage("commands-prefix") + ChatColor.RED + "You do not have permission to use this command.");
            return true;
        }
        if (args.length < 2) {
            sender.sendMessage(ColorUtil.translateColorCodes(messages.getMessage("commands-prefix") + ChatColor.RED + "Usage: /sudo <player> <command/message>"));
            return true;
        }
        Player target = findPlayer(args[0]);
        if (target == null) {
            sendError(sender,  messages.getMessage("commands-prefix")+ "Player not found: " + ChatColor.WHITE + args[0]);
            return true;
        }
        StringBuilder messageBuilder = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            messageBuilder.append(args[i]).append(" ");
        }
        String message = messageBuilder.toString().trim();
        // Check if it's a command or a chat message
        if (message.startsWith("/")) {
            boolean success = Bukkit.dispatchCommand(target, message.substring(1));
            if (success) {
                sendSuccess(sender, messages.getMessage("commands-prefix")+ ChatColor.GRAY + "Forced " + target.getDisplayName() + ChatColor.GRAY + " to execute command: " + ChatColor.WHITE + message);
            } else {
                sendError(sender, messages.getMessage("commands-prefix")+ ChatColor.RED + "Failed to execute command: " + message);
            }
        } else {
            target.chat(ColorUtil.translateColorCodes(message));
            sendSuccess(sender, messages.getMessage("commands-prefix")+  ChatColor.GRAY + "Forced " + target.getDisplayName() + ChatColor.GRAY + " to send message: "+ ChatColor.WHITE + message);
        }
        return true;
    }
    private Player findPlayer(String partialName) {
        String lowerPartialName = partialName.toLowerCase();
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getName().toLowerCase().contains(lowerPartialName)) {
                return player;
            }
        }
        return null;
    }
    public static void sendError(CommandSender sender, String message) {
        sender.sendMessage(ColorUtil.translateColorCodes(ChatColor.RED + message));
    }
    public static void sendSuccess(CommandSender sender, String message) {
        sender.sendMessage(ColorUtil.translateColorCodes(message));
    }
}
