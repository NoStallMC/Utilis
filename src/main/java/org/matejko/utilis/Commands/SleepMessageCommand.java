package main.java.org.matejko.utilis.Commands;

import java.util.Arrays;
import java.util.HashMap;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import main.java.org.matejko.utilis.Utilis;
import main.java.org.matejko.utilis.FileCreator.Messages;
import org.bukkit.ChatColor;

public class SleepMessageCommand implements CommandExecutor {
    private final Messages messages;
    private final HashMap<Player, String> editingPlayerMap = new HashMap<>();

    public SleepMessageCommand(Utilis plugin) {
        this.messages = new Messages(plugin);
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player p = (sender instanceof Player) ? (Player) sender : null;
        if (p == null || command == null) return true;
        String commandName = command.getName();
        if (commandName == null) return true;
        if (commandName.equalsIgnoreCase("sleepmessage")) {
            if (args.length < 1) {
                sendHelpMessage(p);
                return false;
            }
            if (args[0].equalsIgnoreCase("set")) {
                if (p.hasPermission("utilis.sm")) {
                    if (args.length < 2) {
                        p.sendMessage("§7[§2Utilis§7] " + ChatColor.RED + "You must provide a message to set.");
                        return false;
                    }
                    String message = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
                    messages.setCustomSleepMessage(p, message);
                    p.sendMessage("§7[§2Utilis§7] " + ChatColor.GREEN + "Your custom sleep message has been set to:");
                    p.sendMessage(message);
                } else {
                    p.sendMessage("§7[§2Utilis§7] " + ChatColor.RED + "You do not have permission to set a custom sleep message.");
                }
                return true;
            } else if (args[0].equalsIgnoreCase("edit")) {
                if (!p.hasPermission("utilis.sm.admin")) {
                    p.sendMessage("§7[§2Utilis§7] " + ChatColor.RED + "You do not have permission to edit other players' messages.");
                    return false;
                }
                if (args.length < 2) {
                    p.sendMessage("§7[§2Utilis§7] " + ChatColor.RED + "You must provide a player name.");
                    return false;
                }
                String playerName = args[1];
                String message = messages.getCustomSleepMessage(playerName);
                if (message == null || message.isEmpty()) {
                    p.sendMessage("§7[§2Utilis§7] " + ChatColor.RED + "No custom message found for " + playerName);
                } else {
                    editingPlayerMap.put(p, playerName); // Store the player being edited
                    p.sendMessage("§7[§2Utilis§7] " + ChatColor.GREEN + playerName + "'s current custom message: " + ChatColor.WHITE + message);
                    p.sendMessage("§7[§2Utilis§7] " + ChatColor.YELLOW + "Use /sleepmessage new <new message> to set a new message for " + playerName);
                }
                return true;
            } else if (args[0].equalsIgnoreCase("new")) {
                if (!editingPlayerMap.containsKey(p)) {
                    p.sendMessage("§7[§2Utilis§7] " + ChatColor.RED + "You are not editing any player's message. Use /sleepmessage edit <playername> first.");
                    return false;
                }
                if (args.length < 2) {
                    p.sendMessage("§7[§2Utilis§7] " + ChatColor.RED + "You must provide a new message.");
                    return false;
                }
                String playerName = editingPlayerMap.get(p);
                String newMessage = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
                messages.setCustomSleepMessage(playerName, newMessage);
                editingPlayerMap.remove(p); // Clear the editing session
                p.sendMessage("§7[§2Utilis§7] " + ChatColor.GREEN + "Successfully changed " + playerName + "'s custom sleep message to:  " + ChatColor.WHITE + newMessage);
                return true;
            } else if (args[0].equalsIgnoreCase("remove")) {
                if (!p.hasPermission("utilis.sm.admin")) {
                    p.sendMessage("§7[§2Utilis§7] " + ChatColor.RED + "You do not have permission to remove other players' messages.");
                    return false;
                }
                if (args.length < 2) {
                    p.sendMessage("§7[§2Utilis§7] " + ChatColor.RED + "You must provide a player name.");
                    return false;
                }
                String playerName = args[1];
                if (messages.removeCustomSleepMessage(playerName)) {
                    p.sendMessage("§7[§2Utilis§7] " + ChatColor.GREEN + playerName + "'s custom message has been removed.");
                } else {
                    p.sendMessage("§7[§2Utilis§7] " + ChatColor.RED + "No custom message found for " + playerName);
                }
                return true;
            } else {
                p.sendMessage("§7[§2Utilis§7] " + ChatColor.RED + "Invalid command syntax!");
                return false;
            }
        }
        return true;
    }
    private void sendHelpMessage(Player p) {
        if (p.hasPermission("utilis.sm.admin")) {
            p.sendMessage(ChatColor.GREEN + "Correct usage:");
            p.sendMessage(ChatColor.GOLD + "/sleepmessage set <message>" + ChatColor.WHITE + " to set your custom message.");
            p.sendMessage(ChatColor.GOLD + "/sleepmessage edit <playername>" + ChatColor.WHITE + " to edit a player's custom message.");
            p.sendMessage(ChatColor.GOLD + "/sleepmessage new <new message>" + ChatColor.WHITE + " to set a new message for the player being edited.");
            p.sendMessage(ChatColor.GOLD + "/sleepmessage remove <playername>" + ChatColor.WHITE + " to remove a player's custom message.");
        } else if (p.hasPermission("utilis.sm")) {
            p.sendMessage(ChatColor.GREEN + "Correct usage:");
            p.sendMessage(ChatColor.GOLD + "/sleepmessage set <message>" + ChatColor.WHITE + " to set your custom sleep message.");
        } else {
            p.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
        }
    }
}
