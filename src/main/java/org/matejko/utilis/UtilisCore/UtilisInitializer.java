package main.java.org.matejko.utilis.UtilisCore;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import com.earth2me.essentials.Essentials;
import main.java.org.matejko.utilis.Utilis;
import main.java.org.matejko.utilis.Commands.*;
import main.java.org.matejko.utilis.FileCreator.*;
import main.java.org.matejko.utilis.Listeners.*;
import main.java.org.matejko.utilis.Managers.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class UtilisInitializer {
    private final Utilis plugin;
	@SuppressWarnings("unused")
	private WhitelistCommand whitelistCommand;
    static ISeeManager iSeeManager;
    private final Messages messages;

    public UtilisInitializer(Utilis plugin, Messages messages) {
        this.plugin = plugin;
        this.messages = messages;
    }

    public void initialize() {
        plugin.getLogger().info("[Utilis] Initializing...");
        UtilisConfigUpdater configUpdater = new UtilisConfigUpdater(plugin);
        configUpdater.checkAndUpdateConfig();
        UtilisMessagesUpdater messagesUpdater = new UtilisMessagesUpdater(plugin);
        messagesUpdater.checkAndUpdateConfig();
        Config config = new Config(plugin);
        if (!config.isLoaded()) {
            plugin.getLogger().warning("[Utilis] Config was not loaded properly!");
            return;
        }
        Essentials essentials = (Essentials) Bukkit.getServer().getPluginManager().getPlugin("Essentials");
        if (essentials == null) {
            plugin.getLogger().warning("[Utilis] Essentials plugin not found!");
        } else {
            plugin.getLogger().info("[Utilis] Essentials plugin found!");
        }
        // Initialize RecoverManager
        File uuidFile = new File(plugin.getDataFolder(), "uuids.yml");
        if (!plugin.getDataFolder().exists()) plugin.getDataFolder().mkdirs();
        if (!uuidFile.exists()) {
            try {
                if (uuidFile.createNewFile()) {
                    try (FileWriter writer = new FileWriter(uuidFile)) {
                        writer.write("{}\n");
                    }
                }
            } catch (IOException e) {
                plugin.getLogger().warning("[Utilis] Could not create uuids.yml file: " + e.getMessage());
            }
        }
        RecoverManager recoverManager = new RecoverManager(plugin);
        plugin.getServer().getPluginManager().registerEvents(recoverManager, plugin);
        recoverManager.saveUUIDMap();
        plugin.getCommand("recover").setExecutor(new RecoverCommand(recoverManager, messages));
        if (config.isDebugEnabled()) plugin.getLogger().info("[Utilis] RecoverManager initialised.");
        // Initialize ISee
        ISeeManager iSeeManager = new ISeeManager(config);
        if (config.isDebugEnabled()) {
            plugin.getLogger().info("[Utilis] ISeeManager initialized.");
        }
        ISeeInventoryListener iSeeInventoryListener = new ISeeInventoryListener(plugin, iSeeManager);
        @SuppressWarnings("unused")
        ISeeArmorListener iSeeArmorListener = new ISeeArmorListener(plugin, iSeeManager);
        Bukkit.getPluginManager().registerEvents(new ISeeArmorRemover(iSeeManager), plugin);
        Bukkit.getPluginManager().registerEvents(iSeeInventoryListener, plugin);
        UtilisGetters.setISeeManager(iSeeManager);
        ISeeOfflineEditor iSeeOfflineEditor = new ISeeOfflineEditor(recoverManager);
        plugin.getServer().getPluginManager().registerEvents(iSeeOfflineEditor, plugin);
        UtilisGetters.setISeeOfflineEditor(iSeeOfflineEditor);
        if (config.isDebugEnabled()) {
            plugin.getLogger().info("[Utilis] ISee initialisation completed.");
        }
        // ChatFormattingManager setup
        ChatFormattingManager chatFormattingManager = new ChatFormattingManager(plugin);
        chatFormattingManager.loadConfiguration();
        Bukkit.getPluginManager().registerEvents(chatFormattingManager, plugin);
        if (config.isDebugEnabled()) {
            plugin.getLogger().info("[Utilis] ChatFormattingManager is enabled.");
        }
        // VanishedPlayersManager
        Set<VanishUserManager> vanishedPlayers = new HashSet<>();
        VanishedPlayersManager vanishedPlayersManager = new VanishedPlayersManager(plugin, config);
        vanishedPlayersManager.loadVanishedPlayers(vanishedPlayers);
        if (config.isDebugEnabled()) {
            plugin.getLogger().info("[Utilis] VanishedPlayersManager setup complete.");
        }
        // Register the VanishEntityEventListener
        new VanishEntityEventListener(plugin);
        if (config.isDebugEnabled()) {
            plugin.getLogger().info("[Utilis] VanishEntityEventListener registered.");
        }
        // NickManager and cooldown setup
        NickManager nickManager = new NickManager(plugin, config, messages);
        CooldownManager cooldownManager = new CooldownManager(plugin, 15);
        Bukkit.getPluginManager().registerEvents(nickManager, plugin);
        if (config.isDebugEnabled()) {
            plugin.getLogger().info("[Utilis] NickManager and CooldownManager initialised.");
        }
        // UtilisNotifier setup
        UtilisNotifier utilisNotifier = new UtilisNotifier(plugin, config, messages);
        Bukkit.getPluginManager().registerEvents(utilisNotifier, plugin);
        if (config.isDebugEnabled()) {
            plugin.getLogger().info("[Utilis] UtilisNotifier setup complete.");
        }
        // Command registration
        UtilisCommands utilisCommands = new UtilisCommands(plugin, config, nickManager, cooldownManager, messages);
        utilisCommands.registerCommands();
        if (config.isDebugEnabled()) {
            plugin.getLogger().info("[Utilis] Commands registered successfully.");
        }

        // MOTD Manager
        MOTDManager motdManager = null;
        if (config.isMOTDEnabled()) {
            motdManager = new MOTDManager(plugin, config);
            if (config.isDebugEnabled()) {
                plugin.getLogger().info("[Utilis] MOTD is enabled.");
            }
        } else {
            if (config.isDebugEnabled()) {
                plugin.getLogger().info("[Utilis] MOTD is in config disabled.");
            }
        }
        // Plugin Updater
        UtilisPluginUpdater pluginUpdater = new UtilisPluginUpdater(plugin, config);
        pluginUpdater.registerListener();
        if (config.isUpdateEnabled()) {
            pluginUpdater.checkForUpdates();
            if (config.isDebugEnabled()) {
                plugin.getLogger().info("[Utilis] Plugin Updater is enabled.");
            }
        } else {
            if (config.isDebugEnabled()) {
                plugin.getLogger().info("[Utilis] Plugin Updater is disabled in the config.");
            }
        }
        // Sleeping Manager
        SleepingManager sleepingManager = null;
        if (config.isSleepingEnabled()) {
            sleepingManager = new SleepingManager(plugin, config);
            sleepingManager.loadConfiguration();
            Bukkit.getPluginManager().registerEvents(sleepingManager, plugin);
            SleepingCommand sleepingCommand = new SleepingCommand(plugin, sleepingManager, messages);
            plugin.getCommand("sleepmessage").setExecutor(new SleepMessageCommand(plugin));
            plugin.getCommand("ns").setExecutor(sleepingCommand);
            plugin.getLogger().info("[Utilis] Sleeping is enabled.");
        } else {
            plugin.getLogger().info("[Utilis] Sleeping is disabled in the config.");
        }
        // QoL Manager
        if (config.isQoLEnabled()) {
            new SafeAfkManager(plugin);  // SafeAFK Listener
            Bukkit.getPluginManager().registerEvents(new QoLManager(), plugin);
            if (config.isDebugEnabled()) {
                plugin.getLogger().info("[Utilis] QoL is enabled.");
            }
        } else {
            if (config.isDebugEnabled()) {
                plugin.getLogger().info("[Utilis] QoL is disabled in config.");
            }
        }
        // MinecartFallDamageManager
        if (config.isMinecartdmgFixEnabled()) {
            MinecartFallDamageManager fallDamageManager = new MinecartFallDamageManager(plugin, config);
            Bukkit.getPluginManager().registerEvents(new MinecartEventListener(fallDamageManager), plugin);
            if (config.isDebugEnabled()) {
                plugin.getLogger().info("[Utilis] MinecartFallDamageManager is enabled.");
            }
        } else {
            if (config.isDebugEnabled()) {
                plugin.getLogger().info("[Utilis] MinecartFallDamageManager is disabled in config.");
            }
        }
        // Dynmap setup
        Plugin dynmapPlugin = Bukkit.getPluginManager().getPlugin("dynmap");
        if (dynmapPlugin == null) {
            plugin.getLogger().warning("[Utilis] Dynmap plugin not found!");
        } else {
            plugin.getLogger().info("[Utilis] Dynmap plugin found.");
        }
        DynmapManager dynmapManager = new DynmapManager(dynmapPlugin, plugin.getLogger());
        // Whitelist stuff
        whitelistCommand = new WhitelistCommand(plugin, config, messages);
        new WhitelistManager(plugin, config);
        WhitelistManager whitelistManager = new WhitelistManager(plugin, config);
        WhitelistJoinListener whitelistJoinManager = new WhitelistJoinListener(plugin, whitelistManager, plugin, config);
        plugin.getServer().getPluginManager().registerEvents(whitelistJoinManager, plugin);
        plugin.getLogger().info("[Utilis] Whitelist loaded sucessfully.");
        // Anti-Spam Manager setup
        if (config.isAntiSpamEnabled()) {
            AntiSpamManager antiSpamManager = new AntiSpamManager(plugin);
            Bukkit.getPluginManager().registerEvents(antiSpamManager, plugin);
            plugin.getLogger().info("[Utilis] Anti-Spam is enabled.");
        } else {
            plugin.getLogger().info("[Utilis] Anti-Spam is disabled in the config.");
        }
        UtilisGetters utilisGetters = new UtilisGetters(
                plugin.getLogger(), vanishedPlayers, vanishedPlayersManager,
                motdManager, dynmapManager, utilisNotifier,
                config, essentials, dynmapPlugin, sleepingManager, nickManager
        );
        plugin.setUtilisGetters(utilisGetters);
        plugin.getLogger().info("[Utilis] Initialization complete!");
    }
}
