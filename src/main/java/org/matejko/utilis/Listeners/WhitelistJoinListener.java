package main.java.org.matejko.utilis.Listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import main.java.org.matejko.utilis.Utilis;
import main.java.org.matejko.utilis.FileCreator.Config;
import main.java.org.matejko.utilis.Managers.WhitelistManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
@SuppressWarnings("unchecked")
public class WhitelistJoinListener implements Listener {
    @SuppressWarnings("unused")
	private JavaPlugin plugin;
    private Utilis p;
    private Config conf;
    private File whitelistFile;
    private WhitelistManager whitelistManager;

    public WhitelistJoinListener(JavaPlugin plugin, WhitelistManager whitelistManager, Utilis p, Config conf) {
        this.plugin = plugin;
        this.conf = conf;
        this.p = p;
        this.whitelistManager = whitelistManager;
        this.whitelistFile = new File(plugin.getDataFolder(), "whitelist.yml");
    }
    private Object loadWhitelistData() { 	 // Load whitelist data from the YAML file
        try (FileInputStream fis = new FileInputStream(whitelistFile)) {
            return whitelistManager.yaml.load(fis);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    private boolean isWhitelistEnabled() {
        Object data = loadWhitelistData();
        if (data == null) {
            return false;
        }
        if (!(data instanceof Map)) {
            return false;
        }
        Map<String, Object> config = (Map<String, Object>) data;
        Boolean whitelistEnabled = (Boolean) config.get("whitelist-enabled");
        return whitelistEnabled != null && whitelistEnabled;
    }
    private boolean isPlayerWhitelisted(String playerName) {
        Object data = loadWhitelistData();
        if (data == null) {
            return false;
        }
        if (!(data instanceof Map)) {
            return false;
        }
        Map<String, Object> config = (Map<String, Object>) data;
        List<String> allowedPlayers = (List<String>) config.get("allowed-players");
        return allowedPlayers != null && allowedPlayers.contains(playerName.toLowerCase());  // Convert player name to lowercase before comparing
    }
    private void reloadWhitelistData() {
        whitelistManager.reloadConfig();
    }
	@EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String playerName = player.getName();
        reloadWhitelistData();
        // Check if the whitelist is enabled
        boolean isWhitelistEnabled = isWhitelistEnabled();
        if(conf.isDebugEnabled()) {
        	p.getLogger().info("Whitelist enabled: " + isWhitelistEnabled);
        }
        if (isWhitelistEnabled) {
            boolean isWhitelisted = isPlayerWhitelisted(playerName);
            // If the player is not whitelisted, kick them! :D
            if (!isWhitelisted) {
                // Load kick message from whitelist.yml
                Object data = loadWhitelistData();
                if (data instanceof Map) {
                    Map<String, Object> config = (Map<String, Object>) data;
                    String kickMessage = (String) config.getOrDefault("kick-message", "[Utilis] Join Discord to get Whitelisted!");
                    if (conf.isDebugEnabled()) {
                    	p.getLogger().info("[Utilis] Kicking player " + playerName + " with message: " + kickMessage);
                    }
                    player.kickPlayer(kickMessage);
                }
            } else {
            	if (conf.isDebugEnabled()) {
            		p.getLogger().info("[Utilis] Player " + playerName + " is whitelisted.");
            	}
            }
        } else {
        	if (conf.isDebugEnabled()) {
        		p.getLogger().info("[Utilis] Whitelist is not enabled. Player " + playerName + " is allowed to join.");
        	}
        }
    }
}
