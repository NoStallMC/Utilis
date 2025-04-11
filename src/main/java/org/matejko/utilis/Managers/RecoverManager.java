package main.java.org.matejko.utilis.Managers;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import main.java.org.matejko.utilis.Utilis;
import main.java.org.matejko.utilis.FileCreator.Config;
import java.util.HashMap;
import java.util.UUID;

public class RecoverManager implements Listener {
    private final HashMap<UUID, ItemStack[]> deathInventories = new HashMap<>();
    private Utilis plugin;
	private Config config;
	
    public void savePlayerInventory(Player player) {
        if (player != null && player.getInventory() != null) {
            deathInventories.put(player.getUniqueId(), player.getInventory().getContents());
        }
    }
    public RecoverManager(Utilis plugin) {
        this.plugin = plugin;
        this.config = new Config(plugin);
    }
    public ItemStack[] recoverPlayerInventory(UUID playerUUID) {
        return deathInventories.remove(playerUUID);
    }
    public boolean hasSavedInventory(UUID playerUUID) {
        return deathInventories.containsKey(playerUUID);
    }
    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof Player) {
            Player player = (Player) entity;
            savePlayerInventory(player);
            if (config.isDebugEnabled()) {
                plugin.getLogger().info("[Utilis] Inventory saved for " + player.getName() + " on death.");
            }
        }
    }
}
