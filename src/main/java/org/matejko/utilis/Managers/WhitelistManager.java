package main.java.org.matejko.utilis.Managers;

import main.java.org.matejko.utilis.FileCreator.Config;
import org.bukkit.plugin.java.JavaPlugin;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Logger;

@SuppressWarnings("unchecked")
public class WhitelistManager {
    private final Logger logger;
    private final File whitelistFile;
    private final Config conf;
    public final Yaml yaml;

    public WhitelistManager(JavaPlugin plugin, Config conf) {
        this.logger = Logger.getLogger("Utilis");
        this.conf = conf;
        this.whitelistFile = new File(plugin.getDataFolder(), "whitelist.yml");
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setWidth(80);
        yaml = new Yaml(options);
    }

    private void loadWhitelistConfig() {
        if (!whitelistFile.exists()) {
            try {
                if (whitelistFile.createNewFile()) {
                    saveWhitelist(new WhitelistConfig());
                    if (conf.isDebugEnabled()) {
                        logger.info("[Utilis] Created default whitelist.yml.");
                    }
                } else {
                    logger.warning("[Utilis] Failed to create whitelist.yml.");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public WhitelistConfig loadWhitelist() {
        loadWhitelistConfig();
        try (FileInputStream fis = new FileInputStream(whitelistFile)) {
            Object raw = yaml.load(fis);
            if (raw instanceof Map) {
                return WhitelistConfig.fromMap((Map<String, Object>) raw);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new WhitelistConfig();
    }

    public void saveWhitelist(WhitelistConfig config) {
        try (FileOutputStream fos = new FileOutputStream(whitelistFile);
             OutputStreamWriter writer = new OutputStreamWriter(fos, StandardCharsets.UTF_8)) {
            yaml.dump(config.toMap(), writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void reloadConfig() {
        loadWhitelistConfig();
        if (conf.isDebugEnabled()) {
            logger.info("[Utilis] Whitelist config reloaded.");
        }
    }

    public boolean addPlayerToWhitelist(String playerName) {
        WhitelistConfig config = loadWhitelist();
        String lowerName = playerName.toLowerCase();
        if (config.allowedPlayers.contains(lowerName)) {
            return false;
        }
        config.allowedPlayers.add(lowerName);
        saveWhitelist(config);
        return true;
    }

    public boolean removePlayerFromWhitelist(String playerName) {
        WhitelistConfig config = loadWhitelist();
        String lowerName = playerName.toLowerCase();
        if (!config.allowedPlayers.contains(lowerName)) {
            return false;
        }
        config.allowedPlayers.remove(lowerName);
        saveWhitelist(config);
        return true;
    }

    public boolean isPlayerWhitelisted(String playerName) {
        WhitelistConfig config = loadWhitelist();
        for (String allowed : config.allowedPlayers) {
            if (allowed.equalsIgnoreCase(playerName)) {
                return true;
            }
        }
        return false;
    }

    public boolean isWhitelistEnabled() {
        WhitelistConfig config = loadWhitelist();
        return config.whitelistEnabled;
    }

    public void enableWhitelist() {
        WhitelistConfig config = loadWhitelist();
        config.whitelistEnabled = true;
        saveWhitelist(config);
        if (conf.isDebugEnabled()) {
            logger.info("[Utilis] Whitelist enabled.");
        }
    }

    public void disableWhitelist() {
        WhitelistConfig config = loadWhitelist();
        config.whitelistEnabled = false;
        saveWhitelist(config);
        if (conf.isDebugEnabled()) {
            logger.info("[Utilis] Whitelist disabled.");
        }
    }

    // Inner static config class
    public static class WhitelistConfig {
        public boolean whitelistEnabled = false;
        public List<String> allowedPlayers = new ArrayList<>();
        public Map<String, Object> toMap() {
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("whitelist-enabled", whitelistEnabled);
            data.put("allowed-players", new ArrayList<>(allowedPlayers));
            return data;
        }
        public static WhitelistConfig fromMap(Map<String, Object> raw) {
            WhitelistConfig config = new WhitelistConfig();
            Object enabled = raw.get("whitelist-enabled");
            if (enabled instanceof Boolean) {
                config.whitelistEnabled = (Boolean) enabled;
            }
            Object players = raw.get("allowed-players");
            if (players instanceof List) {
                for (Object o : (List<?>) players) {
                    if (o instanceof String) {
                        config.allowedPlayers.add(((String) o).toLowerCase());
                    }
                }
            }
            return config;
        }
    }
}
