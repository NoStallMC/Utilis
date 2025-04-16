package main.java.org.matejko.utilis.Commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import main.java.org.matejko.utilis.Utilis;
import main.java.org.matejko.utilis.FileCreator.Messages;
import main.java.org.matejko.utilis.Managers.ColorUtil;

public class SleepMessageCommand implements CommandExecutor {
    private final Messages messages;
    private final HashMap<Player, String> editingPlayerMap = new HashMap<>();

    public SleepMessageCommand(Utilis plugin) {
        this.messages = new Messages(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        boolean isPlayer = sender instanceof Player;
        Player p = isPlayer ? (Player) sender : null;
        if (args.length < 1) {
            sendHelpMessage(sender);
            return false;
        }
        if (args[0].equalsIgnoreCase("reset")) {
            if (!isPlayer) {
                sender.sendMessage("§7[§2Utilis§7] " + ChatColor.RED + "Only players can reset their sleep message.");
                return true;
            }
            messages.removeCustomSleepMessage(p.getName());
            p.sendMessage("§7[§2Utilis§7] " + ChatColor.GREEN + "Your custom sleep message has been reset.");
            return true;
        }

        if (args[0].equalsIgnoreCase("preview")) {
            if (!isPlayer) {
                sender.sendMessage("§7[§2Utilis§7] " + ChatColor.RED + "Only players can preview their sleep message.");
                return true;
            }
            String raw = messages.getCustomSleepMessage(p.getName());
            if (raw == null || raw.isEmpty()) {
                p.sendMessage("§7[§2Utilis§7] " + ChatColor.RED + "You don't have a custom sleep message.");
            } else {
                String formatted = formatMessage(raw, p.getDisplayName());
                p.sendMessage("§7[§2Utilis§7] " + ChatColor.GREEN + "Your custom sleep message: " + ChatColor.WHITE + formatted);
            }
            return true;
        }
        if (args[0].equalsIgnoreCase("list")) {
            HashMap<String, String> allMessages = messages.getAllCustomSleepMessages();
            if (allMessages.isEmpty()) {
                sender.sendMessage("§7[§2Utilis§7] " + ChatColor.RED + "No custom sleep messages found.");
            } else {
                sender.sendMessage("§7[§2Utilis§7] " + ChatColor.GREEN + "Custom sleep messages:");
                for (String playerName : allMessages.keySet()) {
                    String message = allMessages.get(playerName);
                    sender.sendMessage("§7[§2Utilis§7] " + ChatColor.YELLOW + playerName + ": " + ChatColor.WHITE + message);
                }
            }
            return true;
        }
        if (args[0].equalsIgnoreCase("set")) {
            if (isPlayer) {
                if (!p.hasPermission("utilis.sm")) {
                    p.sendMessage("§7[§2Utilis§7] " + ChatColor.RED + "You do not have permission to set a custom sleep message.");
                    return false;
                }
                if (args.length < 2) {
                    p.sendMessage("§7[§2Utilis§7] " + ChatColor.RED + "You must provide a message to set.");
                    return false;
                }
                String message = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
                messages.setCustomSleepMessage(p, message);
                p.sendMessage("§7[§2Utilis§7] " + ChatColor.GREEN + "Your custom sleep message has been set to:");
                p.sendMessage(ChatColor.WHITE + formatMessage(message, p.getDisplayName()));
            } else {
                sender.sendMessage("§7[§2Utilis§7] " + ChatColor.RED + "The console cannot set a custom sleep message.");
            }
            return true;
        }
        if (args[0].equalsIgnoreCase("edit")) {
            if (isPlayer && !p.hasPermission("utilis.sm.admin")) {
                sender.sendMessage("§7[§2Utilis§7] " + ChatColor.RED + "You do not have permission to edit other players' messages.");
                return false;
            }
            if (args.length < 2) {
                sender.sendMessage("§7[§2Utilis§7] " + ChatColor.RED + "You must provide a player name.");
                return false;
            }
            String searchName = args[1];
            List<Player> matchedPlayers = getMatchedPlayers(searchName);
            if (matchedPlayers.isEmpty()) {
                sender.sendMessage("§7[§2Utilis§7] " + ChatColor.RED + "No players found matching the name '" + searchName + "'.");
                return false;
            }
            if (matchedPlayers.size() == 1) {
                Player targetPlayer = matchedPlayers.get(0);
                String message = messages.getCustomSleepMessage(targetPlayer.getName());
                if (message == null || message.isEmpty()) {
                    sender.sendMessage("§7[§2Utilis§7] " + ChatColor.RED + "No custom message found for " + targetPlayer.getName());
                } else {
                    editingPlayerMap.put(p, targetPlayer.getName()); 
                    sender.sendMessage("§7[§2Utilis§7] " + ChatColor.GREEN + targetPlayer.getName() + "'s current custom message: " + ChatColor.WHITE + message);
                    sender.sendMessage("§7[§2Utilis§7] " + ChatColor.YELLOW + "Use /sm new <new message> to set a new message for " + targetPlayer.getName());
                }
            } else {
                sender.sendMessage("§7[§2Utilis§7] " + ChatColor.YELLOW + "Multiple players match your input. Please be more specific.");
                for (Player matchedPlayer : matchedPlayers) {
                    sender.sendMessage(ChatColor.GOLD + "- " + matchedPlayer.getDisplayName());
                }
            }
            return true;
        }
        if (args[0].equalsIgnoreCase("new")) {
            if (isPlayer && !p.hasPermission("utilis.sm.admin")) {
                sender.sendMessage("§7[§2Utilis§7] " + ChatColor.RED + "You do not have permission to edit other players' messages.");
                return false;
            }
            if (!(sender instanceof ConsoleCommandSender) && !editingPlayerMap.containsKey(p)) {
                p.sendMessage("§7[§2Utilis§7] " + ChatColor.RED + "You are not editing any player's message. Use /sm edit <playername> first.");
                return false;
            }
            if (args.length < 2) {
                p.sendMessage("§7[§2Utilis§7] " + ChatColor.RED + "You must provide a new message.");
                return false;
            }
            String playerName;
            if (sender instanceof ConsoleCommandSender) {
                playerName = args[1]; 
                String message = messages.getCustomSleepMessage(playerName);
                if (message == null) {
                    sender.sendMessage("§7[§2Utilis§7] " + ChatColor.RED + "No custom sleep message found for " + playerName);
                    return false;
                }
            } else {
                playerName = editingPlayerMap.get(p); 
            }
            String newMessage = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
            messages.setCustomSleepMessage(playerName, newMessage);
            if (!(sender instanceof ConsoleCommandSender)) {
                editingPlayerMap.remove(p);
            }
            sender.sendMessage("§7[§2Utilis§7] " + ChatColor.GREEN + "Successfully changed " + playerName + "'s custom sleep message to: " + ChatColor.WHITE + newMessage);
            return true;
        }
        if (args[0].equalsIgnoreCase("remove")) {
            if (!p.hasPermission("utilis.sm.admin")) {
                sender.sendMessage("§7[§2Utilis§7] " + ChatColor.RED + "You do not have permission to remove other players' messages.");
                return false;
            }
            if (args.length < 2) {
                sender.sendMessage("§7[§2Utilis§7] " + ChatColor.RED + "You must provide a player name.");
                return false;
            }
            String playerName = args[1];
            if (messages.removeCustomSleepMessage(playerName)) {
                sender.sendMessage("§7[§2Utilis§7] " + ChatColor.GREEN + playerName + "'s custom message has been removed.");
            } else {
                sender.sendMessage("§7[§2Utilis§7] " + ChatColor.RED + "No custom message found for " + playerName);
            }
            return true;
        }
        return false;
    }
    private String formatMessage(String rawMessage, String playerName) {
        return ColorUtil.translateColorCodes(rawMessage.replace("%player%", playerName));
    }
    private List<Player> getMatchedPlayers(String searchName) {
        List<Player> matchedPlayers = new ArrayList<>();
        Player[] onlinePlayers = Bukkit.getServer().getOnlinePlayers();
        for (Player player : onlinePlayers) {
            if (player.getDisplayName().toLowerCase().contains(searchName.toLowerCase())) {
                matchedPlayers.add(player);
            }
        }
        return matchedPlayers;
    }
    private void sendHelpMessage(CommandSender sender) {
        if (sender instanceof Player) {
            Player p = (Player) sender;
            if (p.hasPermission("utilis.sm.admin")) {
                p.sendMessage("§7[§2Utilis§7] " + ChatColor.GREEN + "Correct usage:");
                p.sendMessage(ChatColor.GOLD + "/sm set <message>" + ChatColor.WHITE + " to set your custom message.");
                p.sendMessage(ChatColor.GOLD + "/sm preview" + ChatColor.WHITE + " to preview your sleep message.");
                p.sendMessage(ChatColor.GOLD + "/sm reset" + ChatColor.WHITE + " to reset your sleep message.");
                p.sendMessage(ChatColor.GOLD + "/sm edit <playername>" + ChatColor.WHITE + " to edit a player's custom message.");
                p.sendMessage(ChatColor.GOLD + "/sm list <playername>" + ChatColor.WHITE + " to view all custom messages.");     
                p.sendMessage(ChatColor.GOLD + "/sm new <new message>" + ChatColor.WHITE + " to set a new message for the player being edited.");
                p.sendMessage(ChatColor.GOLD + "/sm remove <playername>" + ChatColor.WHITE + " to remove a player's custom message.");
                p.sendMessage(ChatColor.GRAY + "BTW you can use %player% to add your name!");
            } else if (p.hasPermission("utilis.sm")) {
                p.sendMessage("§7[§2Utilis§7] " + ChatColor.GREEN + "Correct usage:");
                p.sendMessage(ChatColor.GOLD + "/sm set <message>" + ChatColor.WHITE + " to set your custom sleep message.");
                p.sendMessage(ChatColor.GOLD + "/sm preview" + ChatColor.WHITE + " to preview your sleep message.");
                p.sendMessage(ChatColor.GOLD + "/sm reset" + ChatColor.WHITE + " to reset your sleep message.");
                p.sendMessage(ChatColor.GRAY + "BTW you can use %player% to add your name!");
            } else {
                p.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
            }
        } else {
            sender.sendMessage("§7[§2Utilis§7] " + ChatColor.GREEN + "Correct usage:");
            sender.sendMessage(ChatColor.GOLD + "/sm edit <playername>" + ChatColor.WHITE + " to edit a player's custom message.");
            sender.sendMessage(ChatColor.GOLD + "/sm list <playername>" + ChatColor.WHITE + " to view all custom message.");     
            sender.sendMessage(ChatColor.GOLD + "/sm new <new message>" + ChatColor.WHITE + " to set a new message for the player being edited.");
            sender.sendMessage(ChatColor.GOLD + "/sm remove <playername>" + ChatColor.WHITE + " to remove a player's custom message.");
        }
    }
}
