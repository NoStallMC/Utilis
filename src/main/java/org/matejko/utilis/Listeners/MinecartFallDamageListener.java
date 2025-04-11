package main.java.org.matejko.utilis.Listeners;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import main.java.org.matejko.utilis.Utilis;
import main.java.org.matejko.utilis.FileCreator.Config;

public class MinecartFallDamageListener {
    private static Config config;
    
    public static void handleFallDamage(Player player, Utilis plugin) {
    	config = new Config(plugin);
        Entity vehicle = player.getVehicle();
        if (vehicle instanceof Minecart) {
            Minecart minecart = (Minecart) vehicle;
            if (config.isDebugEnabled()) {
                plugin.getLogger().info("[Utilis] Player " + player.getName() + " is in a minecart. Minecart fall distance: " + minecart.getFallDistance());
            }
            if (minecart.getFallDistance() > 0.0F) {
                resetFallDistance(minecart, plugin);
            }
        } else {
            if (config.isDebugEnabled()) {
            plugin.getLogger().info("[Utilis] Player " + player.getName() + " is not in a minecart.");
            }
        }
    }
    private static void resetFallDistance(Minecart minecart, Utilis plugin) {
        minecart.setFallDistance(0.0F);
        if (config.isDebugEnabled()) {
        	plugin.getLogger().info("[Utilis] Fall damage cancelled for minecart with player.");
        }
    }
}
