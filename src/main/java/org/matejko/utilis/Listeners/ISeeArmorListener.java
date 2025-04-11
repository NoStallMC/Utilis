package main.java.org.matejko.utilis.Listeners;

import java.util.logging.Logger;

import org.bukkit.entity.Player;
import main.java.org.matejko.utilis.Utilis;
import main.java.org.matejko.utilis.Managers.ISeeManager;

public class ISeeArmorListener {
    private final Utilis plugin;
    private final ISeeManager iSeeManager;
    private boolean isSyncing = false;
    private boolean alive = true;
    private final Logger logger;
    private int taskId = -1;

    public ISeeArmorListener(Utilis plugin, ISeeManager iSeeManager) {
        this.logger = Logger.getLogger("Utilis");
        this.iSeeManager = iSeeManager;
        this.plugin = plugin;
    }
    public void startArmorSync(final Player viewer) {
        if (isSyncing) return;
        alive = true;
        taskId = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
            @Override
            public void run() {
                // Check if viewer is valid before continuing
                if (viewer == null || !viewer.isOnline()) {
                    if (iSeeManager.isDebugEnabled()) {
                        logger.info("[Utilis] [DEBUG] Viewer is invalid or offline. Stopping armor sync.");
                    }
                    stopArmorSync(viewer);
                    return;
                }
                if (!alive) {
                    if (iSeeManager.isDebugEnabled()) {
                        logger.info("[Utilis] [DEBUG] Stopping armor sync for viewer " + viewer.getName());
                    }
                    plugin.getServer().getScheduler().cancelTask(taskId);
                    isSyncing = false;
                    taskId = -1;
                    return;
                }
                Player target = iSeeManager.getCurrentTarget(viewer);
                if (target != null && target.isOnline()) {
                    syncArmorFromTargetToViewer(viewer, target);
                } else {
                    if (iSeeManager.isDebugEnabled()) {
                        logger.info("[Utilis] [DEBUG] Target is either null or offline. Stopping sync.");
                    }
                    stopArmorSync(viewer);
                }
            }
        }, 0L, 20L); // Sync armor every 20 ticks (1 second)
        isSyncing = true;
    }
    public void stopArmorSync(Player viewer) {
        alive = false;
        if (taskId != -1) {
            plugin.getServer().getScheduler().cancelTask(taskId);
            taskId = -1;
        }
        isSyncing = false;
    }
    private void syncArmorFromTargetToViewer(Player viewer, Player target) {
        if (viewer == null || target == null || !viewer.isOnline() || !target.isOnline()) {
            if (iSeeManager.isDebugEnabled()) {
                logger.info("[Utilis] [DEBUG] Viewer or target is invalid or offline. Stopping sync.");
            }
            stopArmorSync(viewer);
            return;
        }
        try {
            // Assign each piece of armor, with null and AIR checks
            if (target.getInventory().getHelmet() != null && target.getInventory().getHelmet().getType() != null && !target.getInventory().getHelmet().getType().equals(org.bukkit.Material.AIR)) {
                viewer.getInventory().setHelmet(target.getInventory().getHelmet());
            } else {
                viewer.getInventory().setHelmet(null);
            }
            if (target.getInventory().getChestplate() != null && target.getInventory().getChestplate().getType() != null && !target.getInventory().getChestplate().getType().equals(org.bukkit.Material.AIR)) {
                viewer.getInventory().setChestplate(target.getInventory().getChestplate());
            } else {
                viewer.getInventory().setChestplate(null);
            }
            if (target.getInventory().getLeggings() != null && target.getInventory().getLeggings().getType() != null && !target.getInventory().getLeggings().getType().equals(org.bukkit.Material.AIR)) {
                viewer.getInventory().setLeggings(target.getInventory().getLeggings());
            } else {
                viewer.getInventory().setLeggings(null);
            }
            if (target.getInventory().getBoots() != null && target.getInventory().getBoots().getType() != null && !target.getInventory().getBoots().getType().equals(org.bukkit.Material.AIR)) {
                viewer.getInventory().setBoots(target.getInventory().getBoots());
            } else {
                viewer.getInventory().setBoots(null);
            }
            if (iSeeManager.isDebugEnabled()) {
                logger.info("[Utilis] [DEBUG] Armor synced from " + target.getName() + " to " + viewer.getName());
            }
        } catch (Exception e) {
            if (iSeeManager.isDebugEnabled()) {
                logger.warning("[Utilis] [ERROR] Exception occurred while syncing armor: " + e.getMessage());
                e.printStackTrace();
            }
            stopArmorSync(viewer);
        }
    }
}
