package main.java.org.matejko.utilis.Commands;

import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import main.java.org.matejko.utilis.Utilis;
import main.java.org.matejko.utilis.Listeners.ISeeArmorListener;
import main.java.org.matejko.utilis.Listeners.ISeeInventoryListener;
import main.java.org.matejko.utilis.Managers.ISeeManager;

public class ISeeCommand implements CommandExecutor {
    private final ISeeManager iSeeManager;
    private final ISeeInventoryListener iSeeInventoryListener;
    private final ISeeArmorListener iSeeArmorListener;
    private final Logger logger;
    
    public ISeeCommand(ISeeManager iSeeManager, ISeeInventoryListener iSeeInventoryListener, ISeeArmorListener iSeeArmorListener, Utilis plugin) {
        this.iSeeManager = iSeeManager;
        this.iSeeInventoryListener = iSeeInventoryListener;
        this.iSeeArmorListener = iSeeArmorListener;
        this.logger = Logger.getLogger("Utilis");
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Check if the sender is a player
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command!");
            return true;
        }
        Player player = (Player) sender;
        // Check if iSeeManager is null
        if (iSeeManager == null) {
            player.sendMessage("§7[§2Utilis§7] " + "§cInternal error: iSeeManager is not initialized.");
            logger.warning("[Utilis] Error: iSeeManager is null. Check plugin initialization.");
            return true;
        }
        // Check if the player is already in inventory viewing mode
        Player currentTarget = iSeeManager.getCurrentTarget(player);
        if (currentTarget != null) {
            // Restore the player's original inventory and exit viewing mode
            try {
                iSeeManager.restoreInventoryAndArmor(player);
                iSeeManager.clearTarget(player);
                player.sendMessage("§7[§2Utilis§7] " + "§aYou have exited inventory viewing mode and your inventory and armor have been restored.");
                // Stop inventory and armor syncing
                iSeeInventoryListener.stopInventorySync(player);
                iSeeArmorListener.stopArmorSync(player);
            } catch (Exception e) {
                player.sendMessage("§7[§2Utilis§7] " + "§cError while restoring your inventory.");
                e.printStackTrace();
            }
            return true;
        }
        // Ensure a target player is specified
        if (args.length < 1) {
            player.sendMessage("§7[§2Utilis§7] " + "§cUsage: /isee <player>");
            return true;
        }
        // Get target player by partial name
        Player target = getTargetPlayer(args[0]);
        if (target == null || !target.isOnline()) {
            player.sendMessage("§7[§2Utilis§7] " + "§cPlayer not found or offline.");
            return true;
        }
        // Save the viewer's current inventory to restore later
        try {
            iSeeManager.saveInventoryAndArmor(player);
        } catch (Exception e) {
            player.sendMessage("§7[§2Utilis§7] " + "§cError saving your inventory. Please try again.");
            e.printStackTrace();
            return true;
        }
        // Clone the target player's inventory
        try {
            PlayerInventory targetInventory = target.getInventory();
            PlayerInventory viewerInventory = player.getInventory();
            viewerInventory.clear();
            ItemStack[] targetContents = targetInventory.getContents();
            if (targetContents != null) {
                viewerInventory.setContents(targetContents);
            }
            player.sendMessage("§7[§2Utilis§7] " + "§aNow viewing and editing " + target.getName() + "'s inventory. Type /isee again to exit and restore your inventory.");
            iSeeManager.setCurrentTarget(player, target);
            iSeeInventoryListener.startInventorySync(player);
            iSeeArmorListener.startArmorSync(player);
        } catch (Exception e) {
            player.sendMessage("§7[§2Utilis§7] " + "§cError accessing the target player's inventory.");
            e.printStackTrace();
            return true;
        }
        return true;
    }
    private Player getTargetPlayer(String targetName) {
        Player targetPlayer = null;
        String nickname = targetName.toLowerCase();
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (onlinePlayer.getName().toLowerCase().contains(nickname)) {
                if (targetPlayer != null) {
                    // More than one player matches, return null
                    return null;
                }
                targetPlayer = onlinePlayer;
            }
        }
        return targetPlayer;
    }
}
