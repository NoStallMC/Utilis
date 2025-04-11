package main.java.org.matejko.utilis.UtilisCore;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import main.java.org.matejko.utilis.FileCreator.Config;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Logger;

public class UtilisPluginUpdater implements Listener {
    private static final String UTILIS_PLUGIN_NAME = "Utilis";
    private static final String GITHUB_RELEASE_URL = "https://api.github.com/repos/NoStallMC/Utilis/releases/latest";
    private static final String DOWNLOAD_URL = "https://github.com/NoStallMC/Utilis/releases/download/%s/Utilis.jar";
    private final Logger logger;
    private Plugin plugin;
    private Config config;

    public UtilisPluginUpdater(Plugin plugin, Config config) {
        this.logger = Logger.getLogger("Utilis");
        this.plugin = plugin;
        this.config = config;
    }
    public void checkForUpdates() {
        logger.info("[Utilis] Checking for updates...");
        Bukkit.getScheduler().scheduleAsyncDelayedTask(plugin, this::checkAndUpdatePlugin, 100L);
    }
    private void checkAndUpdatePlugin() {
        Bukkit.getScheduler().scheduleAsyncDelayedTask(plugin, () -> {
            try {
                String latestVersion = getLatestVersionFromGitHub();
                String currentVersion = getCurrentPluginVersion();
                if (latestVersion == null) {
                    logger.severe("[Utilis] Failed to fetch the latest version from GitHub.");
                    return;
                }
                logger.info("[Utilis] Current Version: " + currentVersion);
                logger.info("[Utilis] Latest Version on GitHub: " + latestVersion);
                if (isUpdateAvailable(currentVersion, latestVersion)) {
                    logger.warning("[Utilis] is outdated!");
                    downloadAndNotifyUpdate(latestVersion);
                } else {
                    logger.info("[Utilis] is up to date!");
                }
            } catch (Exception e) {
                logger.severe("[Utilis] Error during update check: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
    private String getLatestVersionFromGitHub() throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(GITHUB_RELEASE_URL).openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("User-Agent", "Mozilla/5.0");
        if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                String jsonResponse = reader.lines().reduce("", (acc, line) -> acc + line);
                return jsonResponse.split("\"tag_name\":\"")[1].split("\"")[0];
            }
        }
        throw new IOException("[Utilis] Failed to fetch version. Response code: " + connection.getResponseCode());
    }
    private String getCurrentPluginVersion() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin(UTILIS_PLUGIN_NAME);
        return plugin != null ? plugin.getDescription().getVersion() : null;
    }
    private boolean isUpdateAvailable(String currentVersion, String latestVersion) {
        return currentVersion != null && !currentVersion.equals(latestVersion);
    }
    private void downloadAndNotifyUpdate(String latestVersion) throws IOException {
        String downloadUrl = String.format(DOWNLOAD_URL, latestVersion);
        HttpURLConnection connection = (HttpURLConnection) new URL(downloadUrl).openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("User-Agent", "Mozilla/5.0");
        if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
            File pluginFolder = new File("plugins/Utilis");
            if (!pluginFolder.exists()) {
                pluginFolder.mkdirs();
            }
            Path tempPluginFile = new File(pluginFolder, "Utilis.jar").toPath();
            try (InputStream inputStream = connection.getInputStream();
                 OutputStream outputStream = Files.newOutputStream(tempPluginFile)) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }
            logger.info("[Utilis] New update downloaded to Utilis folder.");
            if (isUpdateAvailable(getCurrentPluginVersion(), latestVersion)) {
                warnOPs(ChatColor.RED + "[Utilis] is outdated! A new update has been downloaded to the Utilis folder.");
            }
        } else {
            throw new IOException("[Utilis] Failed to download new plugin. Response code: " + connection.getResponseCode());
        }
    }
    private void warnOPs(String message) {
        for (org.bukkit.entity.Player player : Bukkit.getOnlinePlayers()) {
            if (player.isOp()) {
                player.sendMessage(message);
            }
        }
    }
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (event.getPlayer().isOp() && config.isUpdateEnabled()) {
            try {
                String latestVersion = getLatestVersionFromGitHub();
                String currentVersion = getCurrentPluginVersion();
                if (latestVersion != null && isUpdateAvailable(currentVersion, latestVersion)) {
                    warnOPs(ChatColor.RED + "[Utilis] is outdated! A new update has been downloaded to the Utilis folder.");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public void registerListener() {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }
}
