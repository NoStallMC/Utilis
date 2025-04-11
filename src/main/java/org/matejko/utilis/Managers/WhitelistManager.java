package main.java.org.matejko.utilis.Managers;

import org.bukkit.plugin.java.JavaPlugin;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import main.java.org.matejko.utilis.FileCreator.Config;
import java.io.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.logging.Logger;
@SuppressWarnings("unchecked")
public class WhitelistManager {

    private DumperOptions options;
    private final Logger logger;
    private File whitelistFile;
    private Config conf;
    public Yaml yaml;

    public WhitelistManager(JavaPlugin plugin, Config conf) {
        this.logger = Logger.getLogger("Utilis");
        this.conf = conf;
        this.whitelistFile = new File(plugin.getDataFolder(), "whitelist.yml");
        options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        yaml = new Yaml(options);
    }
    private void loadWhitelistConfig() {
        if (!whitelistFile.exists()) {
            try {
                if (!whitelistFile.createNewFile()) {
                    logger.info("[Utilis] Could not create whitelist.yml.");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public Object loadYamlData() {
        try (FileInputStream fis = new FileInputStream(whitelistFile)) {
            return yaml.load(fis);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    public void saveYamlData(Object data) {
        try (FileOutputStream fos = new FileOutputStream(whitelistFile)) {
            yaml.dump(data, new OutputStreamWriter(fos));
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
        Object data = loadYamlData();
        if (data == null) {
            return false;
        }
        if (!(data instanceof Map)) {
            return false;
        }
        Map<String, Object> config = (Map<String, Object>) data;
		List<String> allowedPlayers = (List<String>) config.get("allowed-players");
        if (allowedPlayers == null) {
            allowedPlayers = new ArrayList<>();
        }
        if (allowedPlayers.contains(playerName.toLowerCase())) {
            return false;
        }
        allowedPlayers.add(playerName.toLowerCase());
        config.put("allowed-players", allowedPlayers);
        saveYamlData(config);

        return true;
    }
    public boolean removePlayerFromWhitelist(String playerName) {
        Object data = loadYamlData();
        if (data == null) {
            return false;
        }
        if (!(data instanceof Map)) {
            return false;
        }
        Map<String, Object> config = (Map<String, Object>) data;
        List<String> allowedPlayers = (List<String>) config.get("allowed-players");
        if (allowedPlayers == null || !allowedPlayers.contains(playerName.toLowerCase())) {
            return false;
        }
        allowedPlayers.remove(playerName.toLowerCase());
        config.put("allowed-players", allowedPlayers);
        saveYamlData(config);
        return true;
    }

    // Check if player is whitelisted
    public boolean isPlayerWhitelisted(String playerName) {
        Object data = loadYamlData();
        if (data == null) {
            return false;
        }
        if (!(data instanceof Map)) {
            return false;
        }
        Map<String, Object> config = (Map<String, Object>) data;
        List<String> allowedPlayers = (List<String>) config.get("allowed-players");
        if (allowedPlayers != null) {
            for (String allowedPlayer : allowedPlayers) {
                if (allowedPlayer.equalsIgnoreCase(playerName)) {
                    return true;
                }
            }
        }
        return false;
    }
    public boolean isWhitelistEnabled() {
        Object data = loadYamlData();
        if (data == null) {
            return false;
        }
        if (!(data instanceof Map)) {
            return false;
        }
        Map<String, Object> config = (Map<String, Object>) data;
        return (Boolean) config.getOrDefault("whitelist-enabled", false);
    }
    public void enableWhitelist() {
        Object data = loadYamlData();
        if (data == null) {
            return;
        }
        if (!(data instanceof Map)) {
            return;
        }
        Map<String, Object> config = (Map<String, Object>) data;
        config.put("whitelist-enabled", true);
        saveYamlData(config);
        if (conf.isDebugEnabled()) {
        	logger.info("[Utilis] Whitelist enabled and saved.");
        }
    }
    public void disableWhitelist() {
        Object data = loadYamlData();
        if (data == null) {
            return;
        }
        if (!(data instanceof Map)) {
            return;
        }
        Map<String, Object> config = (Map<String, Object>) data;
        config.put("whitelist-enabled", false);
        saveYamlData(config);
        if (conf.isDebugEnabled()) {
        	logger.info("[Utilis] Whitelist disabled and saved.");
        }
    }
}
