package main.java.org.matejko.utilis.Listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTargetEvent;
import main.java.org.matejko.utilis.Utilis;
import main.java.org.matejko.utilis.Managers.VanishUserManager;

public class VanishEntityEventListener implements Listener {
    private final Utilis plugin;

    public VanishEntityEventListener(Utilis plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
        startMobCheckTask();
    }
    @EventHandler(priority = Priority.High)
    public void onEntityTarget(EntityTargetEvent event) {
        if (event.getTarget() instanceof Player) {
            Player target = (Player) event.getTarget();
            for (VanishUserManager vanishUser : plugin.getUtilisGetters().getVanishedPlayers()) {
                if (vanishUser.getPlayer().equals(target) && vanishUser.isVanished()) {
                    // Cancel the event
                    event.setCancelled(true);
                    Entity entity = event.getEntity();
                    if (entity instanceof Monster) {
                        Monster monster = (Monster) entity;
                        monster.setTarget(null);
                    }
                    break;
                }
            }
        }
    }
    private void startMobCheckTask() {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
            @Override
            public void run() {
                for (VanishUserManager vanishUser : plugin.getUtilisGetters().getVanishedPlayers()) {
                    Player vanishedPlayer = vanishUser.getPlayer();
                    if (vanishedPlayer.isOnline() && vanishUser.isVanished()) {
                        for (Entity entity : Bukkit.getWorlds().get(0).getEntities()) {
                            if (entity instanceof Monster) {
                                Monster monster = (Monster) entity;
                                if (monster.getTarget() != null && monster.getTarget().equals(vanishedPlayer)) {
                                    monster.setTarget(null);
                                }
                            }
                        }
                    }
                }
            }
        }, 0L, 1L); // Check every tick
    }
}
