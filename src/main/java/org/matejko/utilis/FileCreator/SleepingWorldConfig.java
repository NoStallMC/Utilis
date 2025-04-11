package main.java.org.matejko.utilis.FileCreator;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.util.config.Configuration;
import java.io.File;

public class SleepingWorldConfig {
    private final File file;
    private Configuration config;
    
    public SleepingWorldConfig() {
        file = new File(Bukkit.getServer().getPluginManager().getPlugin("Utilis").getDataFolder(), "SleepingWorlds.yml");
        loadConfig();
    }
    // Load the configuration
    public void loadConfig() {
        if (!file.exists()) {  // Create the file if it doesn't exist
            createDefaultConfig();
        }
        config = new Configuration(file);
        config.load();
    }
    // Create the default configuration
    private void createDefaultConfig() {
        config = new Configuration(file);
        for (World world : Bukkit.getWorlds()) {
                config.setProperty(world.getName(), "disabled");
            }
        saveConfig();
    }
    public void saveConfig() {
        try {
            config.save();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    // Get the sleeping status for a world
    public boolean isSleepingEnabled(World world) {
        String status = config.getString(world.getName(), "disabled");
        return status.equalsIgnoreCase("enabled");
    }
    // Set the sleeping status for a world
    public void setSleepingStatus(World world, boolean enabled) {
        config.setProperty(world.getName(), enabled ? "enabled" : "disabled");
        saveConfig();
    }
}
