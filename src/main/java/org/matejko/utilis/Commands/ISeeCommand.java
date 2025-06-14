package main.java.org.matejko.utilis.Commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;
import main.java.org.matejko.utilis.Listeners.ISeeArmorListener;
import main.java.org.matejko.utilis.Listeners.ISeeInventoryListener;
import main.java.org.matejko.utilis.Managers.ISeeManager;
import main.java.org.matejko.utilis.Managers.ISeeOfflineEditor;
import main.java.org.matejko.utilis.Managers.RecoverManager;
import java.io.IOException;
import java.util.UUID;

public class ISeeCommand implements CommandExecutor {
    private final ISeeManager iSeeManager;
    private final ISeeInventoryListener iSeeInventoryListener;
    private final ISeeArmorListener iSeeArmorListener;
    private final ISeeOfflineEditor editor;
    private final RecoverManager recoverManager;

    public ISeeCommand(ISeeManager iSeeManager,
                       ISeeInventoryListener iSeeInventoryListener,
                       ISeeArmorListener iSeeArmorListener,
                       ISeeOfflineEditor editor,
                       RecoverManager recoverManager) {
        this.iSeeManager = iSeeManager;
        this.iSeeInventoryListener = iSeeInventoryListener;
        this.iSeeArmorListener = iSeeArmorListener;
        this.editor = editor;
        this.recoverManager = recoverManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("[Utilis] Only players can use this command.");
            return true;
        }
        Player player = (Player) sender;
        if (!player.hasPermission("utilis.isee")) {
            player.sendMessage("§7[§2Utilis§7] §cYou do not have permission to use this command.");
            return true;
        }
        if (editor.isInOfflineEditMode(player)) {
            try {
                editor.saveOfflineInventory(player);
                player.sendMessage("§7[§2Utilis§7] §aOffline inventory updated and saved.");
            } catch (IOException e) {
                player.sendMessage("§7[§2Utilis§7] §cFailed to save edited offline inventory.");
                e.printStackTrace();
            }
            return true;
        }
        Player currentTarget = iSeeManager.getCurrentTarget(player);
        if (currentTarget != null) {
            try {
                iSeeManager.restoreInventoryAndArmor(player);
                iSeeManager.clearTarget(player);
                iSeeInventoryListener.stopInventorySync(player);
                iSeeArmorListener.stopArmorSync(player);
                player.sendMessage("§7[§2Utilis§7] §aYou have exited inventory viewing mode and your inventory has been restored.");
            } catch (Exception e) {
                player.sendMessage("§7[§2Utilis§7] §cError while restoring your inventory.");
                e.printStackTrace();
            }
            return true;
        }
        if (args.length < 1) {
            player.sendMessage("§7[§2Utilis§7] §cUsage: /isee <player>");
            return true;
        }
        String inputName = args[0];
        String normalized = inputName.toLowerCase();
        Player target = getTargetPlayer(normalized);
        if (target != null && target.isOnline()) {
            try {
                iSeeManager.saveInventoryAndArmor(player);
                PlayerInventory targetInv = target.getInventory();
                PlayerInventory viewerInv = player.getInventory();
                viewerInv.clear();
                viewerInv.setContents(targetInv.getContents());
                viewerInv.setArmorContents(targetInv.getArmorContents());
                iSeeManager.setCurrentTarget(player, target);
                iSeeInventoryListener.startInventorySync(player);
                iSeeArmorListener.startArmorSync(player);
                player.sendMessage("§7[§2Utilis§7] §aNow viewing and editing " + target.getName() + "'s inventory. Type /isee again to exit.");
            } catch (Exception e) {
                player.sendMessage("§7[§2Utilis§7] §cError syncing inventory.");
                e.printStackTrace();
            }
            return true;
        }
        UUID uuid = recoverManager.getUUIDFromSavedName(normalized);
        if (uuid == null) {
            player.sendMessage("§7[§2Utilis§7] §cUnknown player: " + inputName);
            return true;
        }
        try {
            editor.storeViewerInventory(player);
            editor.openOfflineInventory(player, inputName, uuid);
            player.sendMessage("§7[§2Utilis§7] §aNow editing " + inputName + "'s saved inventory. Type /isee to save changes.");
        } catch (IOException e) {
            player.sendMessage("§7[§2Utilis§7] §cFailed to load inventory for " + inputName);
            e.printStackTrace();
        }
        return true;
    }
    private Player getTargetPlayer(String lowercaseName) {
        Player match = null;
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.getName().toLowerCase().contains(lowercaseName)) {
                if (match != null) return null;
                match = p;
            }
        }
        return match;
    }
}
