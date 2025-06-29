package main.java.org.matejko.utilis.Managers;

import main.java.org.matejko.utilis.Utilis;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTargetEvent;

public class SafeAfkManager implements Listener {
    private final Utilis plugin;

    public SafeAfkManager(Utilis plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
        startMobCheckTask();
    }

    @EventHandler
    public void onEntityTarget(EntityTargetEvent event) {
        if (event.getTarget() instanceof Player) {
            Player target = (Player) event.getTarget();
            if (plugin.getUtilisGetters().isAFK(target)) {
                // Cancel the event if the player is AFK
                event.setCancelled(true);
                Entity entity = event.getEntity();
                if (entity instanceof Monster) {
                    ((Monster) entity).setTarget(null);
                }
            }
        }
    }

    private void startMobCheckTask() {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (plugin.getUtilisGetters().isAFK(player)) {
                        for (Entity entity : Bukkit.getWorlds().get(0).getEntities()) {
                            if (entity instanceof Monster) {
                                Monster monster = (Monster) entity;
                                if (monster.getTarget() != null && monster.getTarget().equals(player)) {
                                    monster.setTarget(null);
                                }
                            }
                        }
                    }
                }
            }
        }, 0L, 1L); // Runs every tick
    }
}
