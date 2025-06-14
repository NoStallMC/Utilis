package main.java.org.matejko.utilis;

import org.bukkit.plugin.java.JavaPlugin;
import main.java.org.matejko.utilis.FileCreator.*;
import main.java.org.matejko.utilis.Managers.SleepingManager;
import main.java.org.matejko.utilis.UtilisCore.*;
import org.bukkit.event.Listener;
import java.util.logging.Logger;

public class Utilis extends JavaPlugin implements Listener {
    private Logger logger;
    private Config config;
    private UtilisGetters utilisGetters;

    @Override
    public void onEnable() {
        this.logger = Logger.getLogger("Utilis");
        getLogger().info("[Utilis] is starting up!");
        Messages messages = new Messages(this);
        UtilisInitializer initializer = new UtilisInitializer(this, messages);
        initializer.initialize();
        getLogger().info("[Utilis] has been enabled!");
    }
    @Override
    public void onDisable() {
        getLogger().info("[Utilis] is shutting down...");
    }
    // Getter methods for accessing the plugin's components
    public Logger getLogger() {
        return logger;
    }
    public Config getConfig() {
        return config;
    }
    public void setConfig(Config config) {
        this.config = config;
    }
    public UtilisGetters getUtilisGetters() {
        return utilisGetters;
    }
    public void setUtilisGetters(UtilisGetters utilisGetters) {
        this.utilisGetters = utilisGetters;
    }
    public SleepingManager getSleepingManager() {
        return utilisGetters != null ? utilisGetters.getSleepingManager() : null;
    }
}
