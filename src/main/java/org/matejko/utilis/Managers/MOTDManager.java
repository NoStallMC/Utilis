package main.java.org.matejko.utilis.Managers;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import com.earth2me.essentials.User;
import main.java.org.matejko.utilis.Utilis;
import main.java.org.matejko.utilis.FileCreator.Config;
import main.java.org.matejko.utilis.FileCreator.Messages;
import java.util.ArrayList;
import java.util.List;

public class MOTDManager {
    private final List<String> motdLines;
    private final Utilis plugin;
	private Config config;

    public MOTDManager(Utilis plugin, Config config) {
        this.plugin = plugin;
        this.config = config;
        Messages textManager = new Messages(plugin);
        this.motdLines = new ArrayList<>();
        loadMOTD(textManager);
    }
    private void loadMOTD(Messages textManager) {
        String[] motdMessages = {
            textManager.getMessage("motd.1"),
            textManager.getMessage("motd.2"),
            textManager.getMessage("motd.3")
        };
        for (String message : motdMessages) {
            if (message != null) {
                motdLines.add(message);
            }
        }
    }
    public void sendMOTD(Player player) {
        User user = plugin.getUtilisGetters().getEssentials().getUser(player);
        if (user != null && user.isAfk()) {
            user.setAfk(false);
        }
        List<String> formattedMOTD = getMOTD(player);
        for (String line : formattedMOTD) {
            player.sendMessage(line);
        }
    }
    public List<String> getMOTD(Player player) {
        List<String> formattedMOTD = new ArrayList<>();
        String playerDisplayName = player.getDisplayName();
        for (String line : motdLines) {
            String formattedLine = line.replace("%player%", playerDisplayName);
            formattedLine = formattedLine.replace("%players%", getOnlinePlayerList(player));
            formattedMOTD.add(ColorUtil.translateColorCodes(formattedLine));
        }
        return formattedMOTD;
    }
    private String getOnlinePlayerList(Player sender) {
        StringBuilder playerList = new StringBuilder();
        boolean canSeeVanished = sender.hasPermission("utilis.vanish") && config.isOpSeeVanishEnabled();

        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (!isPlayerVanished(onlinePlayer) || canSeeVanished) {
                String playerName = onlinePlayer.getDisplayName();
                if (plugin.getUtilisGetters().isAFK(onlinePlayer)) {
                    playerName = ChatColor.GRAY + "[AFK] " + playerName;
                }
                if (isPlayerVanished(onlinePlayer) && canSeeVanished) {
                    playerName = ChatColor.GRAY + "[Vanished] " + playerName;
                }
                if (playerList.length() > 0) {
                    playerList.append(", ");
                }
                playerList.append(playerName);
            }
        }
        return playerList.toString();
    }
    private boolean isPlayerVanished(Player player) {
        return plugin.getUtilisGetters().getVanishedPlayers().stream().anyMatch(vanishUser -> vanishUser.getPlayer().equals(player));
    }
}
