package main.java.org.matejko.plugin.Managers;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.util.config.Configuration;
import org.bukkit.event.EventHandler;
import main.java.org.matejko.plugin.Utilis;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class NickManager implements Listener {
    private final Utilis plugin;
    private final File playerDataFile;
    private final Configuration config;
    private final Map<String, String[]> playerData;

    public NickManager(Utilis plugin) {
        this.plugin = plugin;
        this.playerData = new HashMap<>();
        // Ensure the data folder exists
        File nicksFolder = plugin.getDataFolder();
        if (!nicksFolder.exists()) {
            nicksFolder.mkdirs();
        }
        // Initialize player data file
        this.playerDataFile = new File(nicksFolder, "playerData.yml");

        // Check if the file exists, if not create it
        if (!playerDataFile.exists()) {
            try {
                playerDataFile.createNewFile();
                plugin.getLogger().warning("[Utilis] playerData.yml not found! Creating a new one.");
            } catch (Exception e) {
            	plugin.getLogger().severe("[Utilis] Could not create playerData.yml file.");
                e.printStackTrace();
            }
        }

        this.config = new Configuration(playerDataFile);
        loadPlayerData();
    }

    private void loadPlayerData() {
        if (playerDataFile.exists()) {
            config.load();
            playerData.clear();
            for (String playerName : config.getKeys()) {
                String nickname = config.getString(playerName + ".nickname");
                String color = config.getString(playerName + ".color");
                playerData.put(playerName, new String[]{nickname, color});
            }
        } else {
        	plugin.getLogger().warning("[Utilis] Player data file does not exist.");
        }
    }
    
    private void savePlayerData() {
        config.load();
        for (Map.Entry<String, String[]> entry : playerData.entrySet()) {
            String playerName = entry.getKey();
            String[] data = entry.getValue();
            config.setProperty(playerName + ".nickname", data[0]);
            config.setProperty(playerName + ".color", data[1]);
            plugin.getLogger().info("[Utilis] Saving data for " + playerName + ": " + data[0] + " " + data[1]);
        }
        config.save();
    }

    public boolean isValidColor(String color) {
        try {
            ChatColor.valueOf(color.toUpperCase());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public boolean isNicknameUsed(String nickname) {
        for (String[] data : playerData.values()) {
            if (data[0].equals(nickname)) {
                return true;
            }
        }
        return false;
    }

    public boolean isValidNickname(String nickname) {
        return !isNicknameUsed("~" + nickname);
    }

    public void setNickname(Player player, String nickname) {
        String playerName = player.getName();
        if (!isValidNickname(nickname)) {
            player.sendMessage(ChatColor.RED + "The nickname " + "~" + nickname + " is already in use.");
            return;
        }
        String currentColor = getPlayerColor(player);
        String nickWithTilde = "~" + nickname;
        String translatedNickname = ColorUtil.translateColorCodes(nickWithTilde);
        playerData.put(playerName, new String[]{translatedNickname, currentColor});
        player.setDisplayName(ChatColor.valueOf(currentColor.toUpperCase()) + translatedNickname + ChatColor.WHITE);
        savePlayerData();
    }

    public void setNicknameColor(Player player, String color) {
        String playerName = player.getName();
        String[] data = playerData.get(playerName);
        String currentNickname = (data != null) ? data[0] : playerName;  // Fallback to player name if no nickname
        try {
            player.setDisplayName(ChatColor.valueOf(color.toUpperCase()) + currentNickname + ChatColor.WHITE);
        } catch (IllegalArgumentException e) {
        	plugin.getLogger().warning("Invalid color: " + color);
            player.sendMessage(ChatColor.RED + "Invalid color specified.");
            return;
        }
        playerData.put(playerName, new String[]{currentNickname, color});
        savePlayerData();
    }

    public void resetNickname(Player player) {
        String playerName = player.getName();
        player.setDisplayName(playerName);
        playerData.put(playerName, new String[]{playerName, "WHITE"});
        savePlayerData();
    }

    public String getPlayerNickname(Player player) {
        String[] data = playerData.get(player.getName());
        return (data != null && data[0] != null) ? data[0] : player.getName();
    }

    public String getPlayerColor(Player player) {
        String[] data = playerData.get(player.getName());
        return (data != null && data[1] != null) ? data[1] : "WHITE";
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String playerName = player.getName();
        loadPlayerData();
        String[] data = playerData.get(playerName);
        if (data != null && data[0] != null && data[1] != null) {
            String nickname = data[0];  // Get nickname
            String color = data[1];     // Get color
            try {
                // Apply the nickname with color when player joins
                ChatColor chatColor = ChatColor.valueOf(color.toUpperCase());
                player.setDisplayName(chatColor + nickname + ChatColor.WHITE);
            } catch (IllegalArgumentException e) {
                // Fallback to default color (WHITE) in case of an invalid color
                player.setDisplayName(ChatColor.WHITE + nickname);
                plugin.getLogger().warning("Invalid color for player " + playerName + ". Defaulting to white.");
            }
        } else {
            player.setDisplayName(playerName);  // Default to player name if no nickname exists
        }
    }

    public Map<String, String[]> getPlayerData() {
        return playerData;
    }

    public org.bukkit.Server getServer() {
        return plugin.getServer();
    }
}
