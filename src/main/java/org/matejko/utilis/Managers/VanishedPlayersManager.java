package main.java.org.matejko.utilis.Managers;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import main.java.org.matejko.utilis.Utilis;
import main.java.org.matejko.utilis.FileCreator.Config;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class VanishedPlayersManager {
    private final File filePath;
	private final Utilis plugin;
	private final Config config;
	
    public VanishedPlayersManager(Utilis plugin, Config config) {
        this.plugin = plugin;
        this.filePath = new File(plugin.getDataFolder(), "VanishedPlayers.txt");
		this.config = config;
    }
    public void loadVanishedPlayers(Set<VanishUserManager> vanishedPlayers) {
        if (!filePath.exists()) {
            try {
                filePath.createNewFile();
                plugin.getLogger().info("[Utilis] VanishedPlayers.txt file created.");
            } catch (IOException e) {
                plugin.getLogger().warning("[Utilis] Failed to create VanishedPlayers.txt file: " + e.getMessage());
                return;
            }
        }
        try {
            List<String> lines = Files.readAllLines(filePath.toPath());
            for (String name : lines) {
                Player player = Bukkit.getPlayer(name);
                if (player != null && player.isOnline()) {
                    VanishUserManager vanishUser = new VanishUserManager(player, true);
                    vanishedPlayers.add(vanishUser);
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        if (p != player) {
                            p.hidePlayer(player);
                        }
                    }
                }
            }
            if(config.isDebugEnabled()) {
            	plugin.getLogger().info("[Utilis] [DEBUG] Loaded vanished players: " + vanishedPlayers.size());}
        } catch (IOException e) {
            plugin.getLogger().warning("[Utilis] Could not load vanished players: " + e.getMessage());
        }
    }
    public void saveVanishedPlayers(Set<VanishUserManager> vanishedPlayers) {
        try {
            List<String> playerNames = new ArrayList<>();
            for (VanishUserManager vanishUser : vanishedPlayers) {
                playerNames.add(vanishUser.getName());
            }
            Files.write(filePath.toPath(), playerNames);
            if(config.isDebugEnabled()) {
            	plugin.getLogger().info("[Utilis] [DEBUG] Saved vanished players: " + vanishedPlayers.size());}
        } catch (IOException e) {
            plugin.getLogger().warning("[Utilis] Could not save vanished players: " + e.getMessage());
        }
    }
}
