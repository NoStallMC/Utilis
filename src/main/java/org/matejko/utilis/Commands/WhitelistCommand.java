package main.java.org.matejko.utilis.Commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import main.java.org.matejko.utilis.Utilis;
import main.java.org.matejko.utilis.FileCreator.Config;
import main.java.org.matejko.utilis.FileCreator.Messages;
import main.java.org.matejko.utilis.Managers.ColorUtil;
import main.java.org.matejko.utilis.Managers.WhitelistManager;

public class WhitelistCommand {
    private final WhitelistManager whitelistManager;
    private final Messages messages;

    public WhitelistCommand(Utilis plugin,Config conf, Messages messages) {
    	this.messages = messages;
        this.whitelistManager = new WhitelistManager(plugin, conf);
    }
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        whitelistManager.reloadConfig();   
        if (!sender.hasPermission("utilis.whitelist.use")) {
            sender.sendMessage("§cYou don't have permission to use this command.");
            return false;
        }
        if (args.length == 0) {
            return handleStatus(sender);
        }
        if (args[0].equalsIgnoreCase("add") && args.length == 2) {
            return handleAdd(sender, args[1]);
        } else if (args[0].equalsIgnoreCase("remove") && args.length == 2) {
            return handleRemove(sender, args[1]);
        } else if (args[0].equalsIgnoreCase("t")) {
            return handleToggleStatus(sender);
        } else if (args[0].equalsIgnoreCase("on")) {
            return handleEnable(sender);
        } else if (args[0].equalsIgnoreCase("off")) {
            return handleDisable(sender);
        }
        return false;
    }
    private boolean handleStatus(CommandSender sender) {
        boolean isEnabled = whitelistManager.isWhitelistEnabled();
        sender.sendMessage("§7Whitelist status: " + (isEnabled ? "§aEnabled" : "§4Disabled"));
        sender.sendMessage("§7Available commands:");
        sender.sendMessage("§6/wl add <player> §7- Add a player to the whitelist");
        sender.sendMessage("§6/wl remove <player> §7- Remove a player from the whitelist");
        sender.sendMessage("§6/wl t §7- Toggle the whitelist status");
        sender.sendMessage("§6/wl on §7- Enable the whitelist");
        sender.sendMessage("§6/wl off §7- Disable the whitelist");
        return true;
    }
    public boolean handleAdd(CommandSender sender, String playerName) {
        if (sender instanceof org.bukkit.entity.Player && !sender.hasPermission("utilis.whitelist.add")) {
            sender.sendMessage(ColorUtil.translateColorCodes(messages.getMessage("commands-prefix") + "You don't have permission to add players to the whitelist."));
            return false;
        }
        if (whitelistManager.isPlayerWhitelisted(playerName)) {
            sender.sendMessage(ColorUtil.translateColorCodes(messages.getMessage("commands-prefix") + " Player §7" + playerName + " is already on the whitelist."));
            return true;
        }
        if (whitelistManager.addPlayerToWhitelist(playerName)) {
            sender.sendMessage(ColorUtil.translateColorCodes(messages.getMessage("commands-prefix") + " §7" + playerName + " has been added to the whitelist."));
            return true;
        } else {
            sender.sendMessage(ColorUtil.translateColorCodes(messages.getMessage("commands-prefix") + " §7" + playerName + " could not be added to the whitelist."));
            return false;
        }
    }
    public boolean handleRemove(CommandSender sender, String playerName) {
        if (sender instanceof Player && !sender.hasPermission("utilis.whitelist.remove")) {
            sender.sendMessage("You don't have permission to remove players from the whitelist.");
            return false;
        }
        if (whitelistManager.removePlayerFromWhitelist(playerName)) {
            sender.sendMessage(ColorUtil.translateColorCodes(messages.getMessage("commands-prefix") + " §7Player " + playerName + " has been removed from the whitelist."));
            return true;
        } else {
            sender.sendMessage(ColorUtil.translateColorCodes(messages.getMessage("commands-prefix") + " §7Player " + playerName + " is not in the whitelist."));
            return false;
        }
    }
    private boolean handleToggleStatus(CommandSender sender) {
        boolean currentStatus = whitelistManager.isWhitelistEnabled();
        if (currentStatus) {
            whitelistManager.disableWhitelist();
            sender.sendMessage(ColorUtil.translateColorCodes(messages.getMessage("commands-prefix") + " §7The whitelist has been §4disabled§7. All players can now join."));
        } else {
            whitelistManager.enableWhitelist();
            sender.sendMessage(ColorUtil.translateColorCodes(messages.getMessage("commands-prefix") + " §7The whitelist has been §aenabled§7. Now only whitelisted players can join."));
        }
        return true;
    }
    private boolean handleEnable(CommandSender sender) {
        if (whitelistManager.isWhitelistEnabled()) {
            sender.sendMessage(ColorUtil.translateColorCodes(messages.getMessage("commands-prefix") + " §7The whitelist is already §aenabled§7. No changes were made."));
            return true;
        } else {
            whitelistManager.enableWhitelist();
            sender.sendMessage(ColorUtil.translateColorCodes(messages.getMessage("commands-prefix") + " §7The whitelist has been §aenabled§7. Only whitelisted players can join."));
            return true;
        }
    }
    private boolean handleDisable(CommandSender sender) {
        if (!whitelistManager.isWhitelistEnabled()) {
            sender.sendMessage(ColorUtil.translateColorCodes(messages.getMessage("commands-prefix") + " §7The whitelist is already §4disabled§7. No changes were made."));
            return true;
        } else {
            whitelistManager.disableWhitelist();
            sender.sendMessage(ColorUtil.translateColorCodes(messages.getMessage("commands-prefix") + " §7The whitelist has been §4disabled§7. All players can now join."));
            return true;
        }
    }
}
