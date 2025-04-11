package main.java.org.matejko.utilis.Listeners;

import java.util.Map;
import java.util.logging.Logger;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import main.java.org.matejko.utilis.Utilis;
import main.java.org.matejko.utilis.Managers.ISeeManager;

public class ISeeInventoryListener implements Listener {
    private final ISeeManager iSeeManager;
    private final Utilis plugin;
    private boolean isSyncing = false;
    private boolean isSyncingInventory = false;
    private boolean alive = true;
    private ItemStack[] lastViewerInventory;
    private ItemStack[] lastTargetInventory;
    private long lastViewerSyncTime = 0L;
    private long lastTargetSyncTime = 0L;
    private final Logger logger;
    private int taskId = -1;

    public ISeeInventoryListener(Utilis plugin, ISeeManager iSeeManager) {
        this.logger = Logger.getLogger("Utilis");
        this.iSeeManager = iSeeManager;
        this.plugin = plugin;
    }
    public void startInventorySync(final Player viewer) {
        if (isSyncing) return; // Prevent multiple sync tasks
        alive = true; // Ensure sync task is alive when starting
        taskId = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
        	@Override
        	public void run() {
        	    if (!alive) {
        	        if (isDebugEnabled()) { 
        	            logger.info("[Utilis] [DEBUG] Stopping inventory sync for viewer " + viewer.getName());
        	        }
        	        plugin.getServer().getScheduler().cancelTask(taskId);
        	        isSyncing = false;
        	        taskId = -1;
        	        return;
        	    }
        	    Player target = iSeeManager.getCurrentTarget(viewer);
        	    if (target != null && target.isOnline()) {
        	        syncInventory(viewer, target);
        	    } else {
        	        if (isDebugEnabled()) { 
        	            logger.info("[Utilis] [DEBUG] Target is either null or offline. (inv)");
        	        }
        	        // If target is null or offline, restore the viewer's inventory and stop syncing
        	        iSeeManager.restoreInventoryAndArmor(viewer);
        	        iSeeManager.clearTarget(viewer);
        	        logger.info("[Utilis] Viewer " + viewer.getName() + "'s inventory and armor restored due to target being null or offline.");
        	        plugin.getServer().getScheduler().cancelTask(taskId);
        	        isSyncing = false;
        	        taskId = -1;
        	    }
            }
        }, 0L, 1L); // Run every tick for real-time updates
        isSyncing = true;
    }
    public void onCancelSync(Player viewer) {
        alive = false;
        if (taskId != -1) {
            plugin.getServer().getScheduler().cancelTask(taskId);
            taskId = -1;
        }
        iSeeManager.restoreInventoryAndArmor(viewer);
        iSeeManager.clearTarget(viewer);
        if (isDebugEnabled()) { 
            logger.info("[Utilis] [DEBUG] Viewer " + viewer.getName() + "'s inventory and armor restored.");
        }
        isSyncing = false;
    }
    public void stopInventorySync(Player viewer) {
        alive = false;
        if (taskId != -1) {
            plugin.getServer().getScheduler().cancelTask(taskId);
            taskId = -1;
        }
        isSyncing = false;
    }
    private void syncInventory(Player viewer, Player target) {
        if (isSyncingInventory) return;
        isSyncingInventory = true;
        try {
            // Get the current inventories
            ItemStack[] viewerContents = viewer.getInventory().getContents();
            ItemStack[] targetContents = target.getInventory().getContents();
            long currentTime = System.currentTimeMillis();
            boolean viewerInventoryChanged = false;
            boolean targetInventoryChanged = false;
            // Detect if viewer's inventory has changed
            if (lastViewerInventory == null || !inventoryContentsMatch(viewerContents, lastViewerInventory)) {
                viewerInventoryChanged = true;
                lastViewerInventory = viewerContents.clone();
            }
            // Detect if target's inventory has changed
            if (lastTargetInventory == null || !inventoryContentsMatch(targetContents, lastTargetInventory)) {
                targetInventoryChanged = true;
                lastTargetInventory = targetContents.clone();
            }
            // Sync logic
            if (viewerInventoryChanged && currentTime - lastTargetSyncTime > 50) {
                suppressSyncCycle(target);
                target.getInventory().setContents(viewerContents.clone());
                lastViewerSyncTime = currentTime;
                if (isDebugEnabled()) { 
                    logger.info("[Utilis] [DEBUG] Viewer edited inventory, syncing Target.");
                }
            }
            if (targetInventoryChanged && currentTime - lastViewerSyncTime > 50) {
                suppressSyncCycle(viewer);
                viewer.getInventory().setContents(targetContents.clone());
                lastTargetSyncTime = currentTime;
                if (isDebugEnabled()) { 
                    logger.info("[Utilis] [DEBUG] Target edited inventory, syncing Viewer.");
                }
            }

        } finally {
            isSyncingInventory = false;
        }
    }
    private boolean inventoryContentsMatch(ItemStack[] inventory1, ItemStack[] inventory2) {
        if (inventory1.length != inventory2.length) return false;

        for (int i = 0; i < inventory1.length; i++) {
            ItemStack item1 = inventory1[i];
            ItemStack item2 = inventory2[i];
            // Compare item type, amount, and durability
            if (item1 == null && item2 != null || item1 != null && !itemsAreEqual(item1, item2)) {
                return false;
            }
        }
        return true;
    }
    private boolean itemsAreEqual(ItemStack item1, ItemStack item2) {
        if (item1 == null || item2 == null) return item1 == item2;
        return item1.getType().equals(item2.getType()) &&
               item1.getAmount() == item2.getAmount() &&
               item1.getDurability() == item2.getDurability();
    }
    private void suppressSyncCycle(Player player) {
        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            // This just allows one tick delay to avoid feedback loop
        }, 1L);
    }
    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        syncAfterItemChange(player);
    }
    @EventHandler
    public void onPlayerPickupItem(PlayerPickupItemEvent event) {
        Player player = event.getPlayer();
        syncAfterItemChange(player);
    }
    private void syncAfterItemChange(Player player) {
        for (Map.Entry<Player, Player> entry : iSeeManager.getCurrentTargets().entrySet()) {
            if (entry.getKey().equals(player) || entry.getValue().equals(player)) {
                final Player viewer = entry.getKey();
                final Player target = entry.getValue();
                if (viewer.isOnline() && target.isOnline()) {
                    plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                        viewer.getInventory().setContents(viewer.getInventory().getContents());
                        target.getInventory().setContents(target.getInventory().getContents());
                        syncInventory(viewer, target);
                        if (isDebugEnabled()) { 
                            logger.info("[Utilis] [DEBUG] Item drop/pickup by " + player.getName() + ". Inventories synced.");
                        }
                    }, 1L);
                    break;
                }
            }
        }
    }
    private boolean isDebugEnabled() {
        return iSeeManager.isDebugEnabled();
    }
}
