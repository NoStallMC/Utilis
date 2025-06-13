package main.java.org.matejko.utilis.Managers;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBedLeaveEvent;
import org.bukkit.scheduler.BukkitScheduler;
import main.java.org.matejko.utilis.Utilis;
import main.java.org.matejko.utilis.FileCreator.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class SleepingManager implements Listener {
    private final SleepingWorldConfig worldConfig;
    private final ConcurrentHashMap<World, ArrayList<Player>> sleepingPlayers;
    private final ConcurrentHashMap<World, Boolean> nightSkipped;
    private final Messages messages;
    private Utilis plugin;
    private Config config;
    
    // Listener interface to notify when sleep message is sent
    public interface SleepMessageListener {
    	void onSleepMessage(Player sleeper, String message);
    }
    // Registered listeners list
    private final List<SleepMessageListener> sleepMessageListeners = new ArrayList<>();

    public SleepingManager(Utilis plugin, Config config) {
        this.plugin = plugin;
        this.config = config;
        this.worldConfig = new SleepingWorldConfig();
        this.sleepingPlayers = new ConcurrentHashMap<>();
        this.nightSkipped = new ConcurrentHashMap<>();
        this.messages = new Messages(plugin);
    }
    public boolean isSleepingEnabled(World world) {
        return worldConfig.isSleepingEnabled(world);
    }
    public void toggleSleeping(World world) {
        boolean currentStatus = isSleepingEnabled(world);
        worldConfig.setSleepingStatus(world, !currentStatus);  // Toggle sleeping status
        plugin.getLogger().info("[Utilis] Sleeping in world '" + world.getName() + "' has been " + (currentStatus ? "disabled" : "enabled"));
    }
    @EventHandler
    public void onPlayerBedEnter(PlayerBedEnterEvent event) {
        Player player = event.getPlayer();
        World world = player.getWorld();
        plugin.getLogger().info("[Utilis] " + player.getName() + " entered bed in world: " + world.getName());
        if (!isSleepingEnabled(world)) {
            return;
        }
        sleepingPlayers.computeIfAbsent(world, k -> new ArrayList<>()).add(player);
        plugin.getLogger().info("[Utilis] Sleeping players in " + world.getName() + ": " + sleepingPlayers.get(world).size());
        checkForAtLeastOnePlayerSleeping(world, player);
    }
    @EventHandler
    public void onPlayerBedLeave(PlayerBedLeaveEvent event) {
        Player player = event.getPlayer();
        World world = player.getWorld();
        if (!isSleepingEnabled(world)) {
            return;
        }
        ArrayList<Player> worldSleepingPlayers = sleepingPlayers.get(world);
        if (worldSleepingPlayers != null) {
            worldSleepingPlayers.remove(player);
        }
        // Reset the nightSkipped flag when no players are sleeping
        if (worldSleepingPlayers == null || worldSleepingPlayers.isEmpty()) {
            nightSkipped.put(world, false);
        }
    }
    private void checkForAtLeastOnePlayerSleeping(World world, Player player) {
        if (!isSleepingEnabled(world)) {
            return;
        }
        // Ensure night is skipped only once per cycle
        if (nightSkipped.getOrDefault(world, false)) {
            return;
        }
        List<Player> sleepingPlayersInWorld = sleepingPlayers.getOrDefault(world, new ArrayList<>());
        if (!sleepingPlayersInWorld.isEmpty()) {
            world.setStorm(false);
            world.setThundering(false);
            nightSkipped.put(world, true); // Mark the night as skipped
            BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
            scheduler.scheduleSyncDelayedTask(Bukkit.getPluginManager().getPlugin("Utilis"), () -> {
                world.setTime(23500);  // Set time to dawn
                messages.load();
                String messageTemplate;
                if (config.isCSMEnabled()) { 
                    messageTemplate = messages.getCustomSleepMessage(player);
                } else {
                    messageTemplate = null; // Skip custom message when CSM is disabled
                }
                if (messageTemplate == null || messageTemplate.isEmpty()) {
                    messageTemplate = messages.getMessage("sleeping.night-skip");
                }
                messageTemplate = messageTemplate.replace("%player%", player.getDisplayName());
                messageTemplate = ColorUtil.translateColorCodes(messageTemplate);
                
                for (SleepMessageListener listener : sleepMessageListeners) {
                    listener.onSleepMessage(player, messageTemplate);
                }
                for (Player p : world.getPlayers()) {
                    p.sendMessage(messageTemplate);
                }
                plugin.getLogger().info("[Utilis] At least one player is asleep. Skipping the night in '" + world.getName() + "'.");
            }, 50L);  // 50 ticks (2.5 seconds)
        }
    }
    public void cleanupPlayer(Player player) {
        for (World world : sleepingPlayers.keySet()) {
            ArrayList<Player> worldSleepingPlayers = sleepingPlayers.get(world);
            if (worldSleepingPlayers != null) {
                worldSleepingPlayers.remove(player);
            }
        }
    }
    public void loadConfiguration() {
        worldConfig.loadConfig();
    }
    public void setCustomSleepMessage(Player player, String message) {
        messages.setCustomSleepMessage(player, message);
    }
    public void addSleepMessageListener(SleepMessageListener listener) {
        if (!sleepMessageListeners.contains(listener)) {
            sleepMessageListeners.add(listener);
        }
    }
    public void removeSleepMessageListener(SleepMessageListener listener) {
        sleepMessageListeners.remove(listener);
    }
}
