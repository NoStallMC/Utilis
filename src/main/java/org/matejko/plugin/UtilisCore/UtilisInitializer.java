package main.java.org.matejko.plugin.UtilisCore;

import main.java.org.matejko.plugin.Managers.*;
import main.java.org.matejko.plugin.Commands.*;
import main.java.org.matejko.plugin.FileCreator.*;
import main.java.org.matejko.plugin.Utilis;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import com.earth2me.essentials.Essentials;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

public class UtilisInitializer {
    private final Utilis plugin;
    private final Logger logger;

    public UtilisInitializer(Utilis plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
    }

    @SuppressWarnings("static-access")
	public void initialize() {
        logger.info("[Utilis] Initializing...");

        // Initialize config updater
        UtilisConfigUpdater configUpdater = new UtilisConfigUpdater(plugin);
        configUpdater.checkAndUpdateConfig();

        // Initialize config
        Config config = new Config(plugin);
        if (!config.isLoaded()) {
            logger.warning("[Utilis] Config was not loaded properly!");
            return; // Stop execution if config is not loaded properly
        }

        // Essentials plugin setup
        Essentials essentials = (Essentials) Bukkit.getServer().getPluginManager().getPlugin("Essentials");
        if (essentials == null) {
            logger.warning("[Utilis] Essentials plugin not found!");
        } else {
            logger.info("[Utilis] Essentials plugin found!");
        }

        // ChatFormattingManager setup
        ChatFormattingManager chatFormattingManager = new ChatFormattingManager(plugin);
        chatFormattingManager.loadConfiguration();
        Bukkit.getPluginManager().registerEvents(chatFormattingManager, plugin);

        // MOTD Manager
        MOTDManager motdManager = null;
        if (config.isMOTDEnabled()) {
            motdManager = new MOTDManager(plugin);
        }

        // Plugin Updater
        UtilisPluginUpdater pluginUpdater = new UtilisPluginUpdater(plugin, config);
        pluginUpdater.registerListener();
        if (config.isUpdateEnabled()) {
            pluginUpdater.checkForUpdates();
        } else {
            logger.info("[Utilis] Update check is disabled in the config.");
        }

        // Sleeping Manager
        SleepingManager sleepingManager = null;
        if (config.isSleepingEnabled()) {
            sleepingManager = new SleepingManager(plugin);
            sleepingManager.loadConfiguration();
            Bukkit.getPluginManager().registerEvents(sleepingManager, plugin);

            // Register /as command
            SleepingCommand sleepingCommand = new SleepingCommand(plugin);
            plugin.getCommand("as").setExecutor(sleepingCommand);
        } else {
            logger.info("[Utilis] Sleeping is disabled in the config.");
        }

        // VanishedPlayersManager
        Set<VanishUserManager> vanishedPlayers = new HashSet<>(); // Use Set<VanishUserManager>
        VanishedPlayersManager vanishedPlayersManager = new VanishedPlayersManager(plugin);
        vanishedPlayersManager.loadVanishedPlayers(vanishedPlayers); // Populate VanishUserManager instances

        // NickManager and cooldown setup
        NickManager nickManager = new NickManager(plugin);
        Messages messages = new Messages(plugin);
        CooldownManager cooldownManager = new CooldownManager(plugin, 60);

        Bukkit.getPluginManager().registerEvents(nickManager, plugin);

        // Command registration
        UtilisCommands utilisCommands = new UtilisCommands(plugin, config, nickManager, cooldownManager, messages);
        utilisCommands.registerCommands();

        // QoL Manager
        if (config.isQoLEnabled()) {
            Bukkit.getPluginManager().registerEvents(new QoLManager(), plugin);
        }

        // Dynmap setup
        Plugin dynmapPlugin = Bukkit.getPluginManager().getPlugin("dynmap");
        if (dynmapPlugin == null) {
            logger.warning("[Utilis] Dynmap plugin not found!");
        }
        DynmapManager dynmapManager = new DynmapManager(dynmapPlugin, logger);

        // UtilisNotifier setup
        UtilisNotifier utilisNotifier = new UtilisNotifier(plugin);
        Bukkit.getPluginManager().registerEvents(utilisNotifier, plugin);

        // Create UtilisGetters instance
        UtilisGetters utilisGetters = new UtilisGetters(
                logger, vanishedPlayers, vanishedPlayersManager,
                motdManager, dynmapManager, utilisNotifier,
                config, essentials, dynmapPlugin, sleepingManager, nickManager
        );

        // Store the UtilisGetters in the plugin for later access
        plugin.setUtilisGetters(utilisGetters);

        logger.info("[Utilis] Initialization complete!");
    }
}
