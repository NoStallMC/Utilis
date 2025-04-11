package main.java.org.matejko.utilis.Managers;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import main.java.org.matejko.utilis.FileCreator.Config;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class ISeeManager {
    private final Map<Player, ItemStack[]> savedInventories = new HashMap<>();
    private final Map<Player, ItemStack[]> savedArmor = new HashMap<>();
    private final Map<Player, Player> currentTargets = new HashMap<>();
    private final Config config;
    private final Logger logger;
    
    public ISeeManager(Config config) {
        this.logger = Logger.getLogger("Utilis");
        this.config = config;
    }
    public void saveInventoryAndArmor(Player player) {
        savedInventories.put(player, player.getInventory().getContents());
        ItemStack[] armorContents = new ItemStack[4];
        armorContents[0] = player.getInventory().getHelmet() != null ? player.getInventory().getHelmet() : new ItemStack(0);  // 0 is an invalid item ID (AIR)
        armorContents[1] = player.getInventory().getChestplate() != null ? player.getInventory().getChestplate() : new ItemStack(0);
        armorContents[2] = player.getInventory().getLeggings() != null ? player.getInventory().getLeggings() : new ItemStack(0);
        armorContents[3] = player.getInventory().getBoots() != null ? player.getInventory().getBoots() : new ItemStack(0);
        savedArmor.put(player, armorContents);
    }
    public void restoreInventoryAndArmor(Player player) {
        restoreInventoryStep(player);
        restoreArmorStep(player);
    }
    private void restoreInventoryStep(final Player player) {
        ItemStack[] contents = savedInventories.remove(player);
        if (contents != null) {
            try {
                if (config.isDebugEnabled()) { 
                    logger.info("[DEBUG] Restoring inventory for player: " + player.getName());
                }
                player.getInventory().setContents(contents);
            } catch (Exception e) {
                if (config.isDebugEnabled()) { 
                    logger.warning("[ERROR] Failed to restore inventory for player " + player.getName() + ": " + e.getMessage());
                }
                e.printStackTrace();
                resetInventory(player); // Fallback if inventory restoration fails
            }
        } else {
            if (config.isDebugEnabled()) { 
                logger.info("[DEBUG] No saved inventory for player: " + player.getName());
            }
            resetInventory(player); // Reset inventory if no saved inventory is found
        }
    }
    public void restoreArmorStep(final Player player) {
        ItemStack[] armorContents = savedArmor.remove(player);
        if (armorContents != null) {
            if (armorContents.length == 4) {
                try {
                    if (config.isDebugEnabled()) { 
                        logger.info("[DEBUG] Restoring armor for player: " + player.getName());
                    }
                    PlayerInventory inv = player.getInventory();
                    inv.setHelmet(armorContents[0].getTypeId() != 0 ? armorContents[0] : null);
                    inv.setChestplate(armorContents[1].getTypeId() != 0 ? armorContents[1] : null);
                    inv.setLeggings(armorContents[2].getTypeId() != 0 ? armorContents[2] : null);
                    inv.setBoots(armorContents[3].getTypeId() != 0 ? armorContents[3] : null);
                    if (config.isDebugEnabled()) {
                        logger.info("[DEBUG] Armor restored for player: " + player.getName());
                    }
                } catch (Exception e) {
                    if (config.isDebugEnabled()) { 
                        logger.warning("[ERROR] Failed to restore armor for player " + player.getName() + ": " + e.getMessage());
                    }
                    e.printStackTrace();
                    resetArmor(player); // Fallback if armor restoration fails
                }
            } else {
                if (config.isDebugEnabled()) { 
                    logger.warning("[ERROR] Saved armor data for player " + player.getName() + " is invalid (length mismatch). Resetting armor.");
                }
                resetArmor(player);
            }
        } else {
            if (config.isDebugEnabled()) { 
                logger.info("[DEBUG] No saved armor for player: " + player.getName());
            }
            resetArmor(player); // Reset armor if no saved armor is found
        }
    }
    private void resetInventory(Player player) {
        try {
            player.getInventory().clear();
            if (config.isDebugEnabled()) { 
                logger.info("[DEBUG] Inventory reset for player: " + player.getName());
            }
        } catch (Exception e) {
            if (config.isDebugEnabled()) { 
                logger.warning("[ERROR] Failed to reset inventory for player " + player.getName() + ": " + e.getMessage());
            }
            e.printStackTrace();
        }
    }
    private void resetArmor(Player player) {
        try {
            player.getInventory().setHelmet(new ItemStack(0));  // AIR is ID 0
            player.getInventory().setChestplate(new ItemStack(0));  // AIR is ID 0
            player.getInventory().setLeggings(new ItemStack(0));  // AIR is ID 0
            player.getInventory().setBoots(new ItemStack(0));  // AIR is ID 0
            if (config.isDebugEnabled()) { 
                logger.info("[DEBUG] Armor reset for player: " + player.getName());
            }
        } catch (Exception e) {
            if (config.isDebugEnabled()) { 
                logger.warning("[ERROR] Failed to reset armor for player " + player.getName() + ": " + e.getMessage());
            }
            e.printStackTrace();
        }
    }
    public void setCurrentTarget(Player viewer, Player target) {
        currentTargets.put(viewer, target);
    }
    public Player getCurrentTarget(Player viewer) {
        return currentTargets.get(viewer);
    }
    public void clearTarget(Player viewer) {
        currentTargets.remove(viewer);
    }
    public Map<Player, Player> getCurrentTargets() {
        return currentTargets;
    }
    public boolean isDebugEnabled() {
        return config.isDebugEnabled();
    }
}
