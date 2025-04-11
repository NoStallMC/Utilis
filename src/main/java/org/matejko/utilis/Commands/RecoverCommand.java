package main.java.org.matejko.utilis.Commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import main.java.org.matejko.utilis.Managers.RecoverManager;
import java.util.UUID;

public class RecoverCommand implements CommandExecutor {
    private final RecoverManager recoverManager;

    public RecoverCommand(RecoverManager recoverManager) {
        this.recoverManager = recoverManager;
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length != 1) {
            sender.sendMessage("§7[§2Utilis§7] " + ChatColor.RED + "Usage: /recover <playername>");
            return true;
        }
        String playerName = args[0].toLowerCase();
        Player matchedPlayer = null;
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getName().toLowerCase().contains(playerName)) {
                matchedPlayer = player;
                break;
            }
        }
        if (matchedPlayer == null) {
            sender.sendMessage("§7[§2Utilis§7] " + ChatColor.RED + "Player not found!");
            return true;
        }
        UUID playerUUID = matchedPlayer.getUniqueId();
        if (!recoverManager.hasSavedInventory(playerUUID)) {
            sender.sendMessage("§7[§2Utilis§7] " + ChatColor.YELLOW + "No saved inventory found for " + matchedPlayer.getName());
            return true;
        }
        // Restore the player's inventory from the saved data
        matchedPlayer.getInventory().setContents(recoverManager.recoverPlayerInventory(playerUUID));
        sender.sendMessage("§7[§2Utilis§7] " + ChatColor.GREEN + "Recovered inventory for " + matchedPlayer.getName());
        if (sender instanceof Player) {
            Player playerSender = (Player) sender;
            matchedPlayer.sendMessage("§7[§2Utilis§7] " + ChatColor.GREEN + "Your inventory has been recovered by " + playerSender.getDisplayName());
        } else {
            matchedPlayer.sendMessage("§7[§2Utilis§7] " + ChatColor.GREEN + "Your inventory has been recovered by " + sender.getName());
        }
        return true;
    }
}
