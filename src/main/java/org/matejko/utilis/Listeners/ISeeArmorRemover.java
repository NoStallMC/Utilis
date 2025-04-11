package main.java.org.matejko.utilis.Listeners;

import java.util.logging.Logger;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import main.java.org.matejko.utilis.Managers.ISeeManager;

public class ISeeArmorRemover implements Listener {
    private final ISeeManager iSeeManager;
    private final Logger logger;
    public ISeeArmorRemover(ISeeManager iSeeManager) {
        this.logger = Logger.getLogger("Utilis");
        this.iSeeManager = iSeeManager;
    }
    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        ItemStack droppedItem = event.getItemDrop().getItemStack();
        if (droppedItem == null || droppedItem.getTypeId() == 0) {
            return;
        }
        Player target = iSeeManager.getCurrentTarget(player);  // Fetch the target player from ISeeManager
        if (target != null && isMatchingArmor(droppedItem, target)) {
            // Remove the matching armor piece from the target's inventory
            removeArmorPieceFromTarget(target, droppedItem);
            if (isDebugEnabled()) {
                logger.info("[Utilis] [DEBUG] Removed matching armor piece from target: " + target.getName());
            }
        }
    }
    // Compare the dropped item with an armor piece from the target based on material and durability.
    private boolean isMatchingArmor(ItemStack droppedItem, Player target) {
        if (droppedItem == null || droppedItem.getTypeId() == 0) {  // Ensure the dropped item and target armor are not null
            return false;
        }
        // Compare each piece of the target's armor
        if (target.getInventory().getHelmet() != null && isSameItem(droppedItem, target.getInventory().getHelmet())) {
            return true;
        } else if (target.getInventory().getChestplate() != null && isSameItem(droppedItem, target.getInventory().getChestplate())) {
            return true;
        } else if (target.getInventory().getLeggings() != null && isSameItem(droppedItem, target.getInventory().getLeggings())) {
            return true;
        } else if (target.getInventory().getBoots() != null && isSameItem(droppedItem, target.getInventory().getBoots())) {
            return true;
        }
        return false;
    }
    // Compare the dropped item with an armor piece from the target based on material and durability.
    private boolean isSameItem(ItemStack droppedItem, ItemStack targetItem) {
        if (droppedItem.getTypeId() != targetItem.getTypeId()) {
            return false;
        }
        if (droppedItem.getDurability() != targetItem.getDurability()) {
            return false;
        }
        return true;
    }
    // Remove the matching armor piece from the target's inventory
    private void removeArmorPieceFromTarget(Player target, ItemStack droppedItem) {
        // Ensure armor removal is handled correctly, with proper null checks
        boolean armorChanged = false;
        if (target.getInventory().getHelmet() != null && isSameItem(droppedItem, target.getInventory().getHelmet())) {
            target.getInventory().setHelmet(null);  // Use `null` to remove helmet
            armorChanged = true;
        } else if (target.getInventory().getChestplate() != null && isSameItem(droppedItem, target.getInventory().getChestplate())) {
            target.getInventory().setChestplate(null);  // Use `null` to remove chestplate
            armorChanged = true;
        } else if (target.getInventory().getLeggings() != null && isSameItem(droppedItem, target.getInventory().getLeggings())) {
            target.getInventory().setLeggings(null);  // Use `null` to remove leggings
            armorChanged = true;
        } else if (target.getInventory().getBoots() != null && isSameItem(droppedItem, target.getInventory().getBoots())) {
            target.getInventory().setBoots(null);  // Use `null` to remove boots
            armorChanged = true;
        }
        if (armorChanged) {
        	iSeeManager.saveInventoryAndArmor(target);
            if (iSeeManager.isDebugEnabled()) {
                logger.info("[Utilis] [DEBUG] Updated armor for player: " + target.getName());
            }
        }
    }
    private boolean isDebugEnabled() {
        return iSeeManager.isDebugEnabled();
    }
}
