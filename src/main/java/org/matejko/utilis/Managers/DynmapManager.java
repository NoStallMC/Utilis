package main.java.org.matejko.utilis.Managers;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.logging.Logger;

public class DynmapManager {
    private final String HIDDEN_PLAYERS_PATH = "plugins/dynmap/hiddenplayers.txt";
    private Plugin dynmapPlugin;
    private Logger logger;

    public DynmapManager(Plugin dynmapPlugin, Logger logger) {
        this.dynmapPlugin = dynmapPlugin;
        this.logger = logger;
    }
    public void addToHiddenPlayersFile(String playerName) {
        try {
            if (dynmapPlugin != null) {
            List<String> lines = Files.readAllLines(Paths.get(HIDDEN_PLAYERS_PATH));
            if (!lines.contains(playerName)) {
                Files.write(Paths.get(HIDDEN_PLAYERS_PATH), (playerName + System.lineSeparator()).getBytes(),
                            java.nio.file.StandardOpenOption.APPEND, java.nio.file.StandardOpenOption.CREATE);
                logger.info("[Utilis]" + playerName + " added to hidden players.");
                reloadDynmap();}
            }
        } catch (IOException e) {
           logger.warning("[Utilis] Could not add " + playerName + " to hidden players: " + e.getMessage());
        }
    }
    public void removeFromHiddenPlayersFile(String playerName) {
        try {
            if (dynmapPlugin != null) {
            List<String> lines = Files.readAllLines(Paths.get(HIDDEN_PLAYERS_PATH));
            lines.remove(playerName);
            Files.write(Paths.get(HIDDEN_PLAYERS_PATH), lines);
            logger.info("[Utilis]" + playerName + " removed from hidden players.");
            reloadDynmap();}
        } catch (IOException e) {
            logger.warning("[Utilis] Could not remove " + playerName + " from hidden players: " + e.getMessage());
        }
    }
    private void reloadDynmap() {
        if (dynmapPlugin != null && dynmapPlugin.isEnabled()) {
            Bukkit.getPluginManager().disablePlugin(dynmapPlugin);
            Bukkit.getPluginManager().enablePlugin(dynmapPlugin);
            logger.info("[Utilis] Dynmap reloaded.");
        } else {
            logger.warning("[Utilis] Dynmap plugin is not available or not enabled.");
        }
    }
    public void reload() {
        reloadDynmap();
        logger.info("[Utilis] Dynmap reloaded.");
    }
}
