package main.java.org.matejko.utilis.Commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import main.java.org.matejko.utilis.Utilis;
import main.java.org.matejko.utilis.FileCreator.Config;
import main.java.org.matejko.utilis.FileCreator.Messages;
import main.java.org.matejko.utilis.Managers.ColorUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ListCommand implements CommandExecutor {
    private final Utilis plugin;
    private final Map<String, String> messages;
	private Config config;

    public ListCommand(Utilis plugin, Config config) {
        this.plugin = plugin;
        this.config = config;
        this.messages = new HashMap<>();
        loadMessages();
    }
    private void loadMessages() {
        Messages textManager = new Messages(plugin);
        String playerCountMessage = textManager.getMessagesConfig().getString("list.playerCountMessage");
        String onlineMessage = textManager.getMessagesConfig().getString("list.onlineMessage");
        if (playerCountMessage != null) {
            messages.put("list.playerCountMessage", playerCountMessage);
        }
        if (onlineMessage != null) {
            messages.put("list.onlineMessage", onlineMessage);
        }
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        execute(sender);
        return true;
    }
    // Method to execute the /list command
    public void execute(CommandSender sender) {
        int playerHidden = 0;
        List<String> onlinePlayerNames = new ArrayList<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            boolean isVanished = plugin.getUtilisGetters().getVanishedPlayers().stream()
                    .anyMatch(vanishUser -> vanishUser.getPlayer().equals(player) && vanishUser.isVanished());
            if (isVanished && !(sender instanceof Player && ((Player) sender).hasPermission("utilis.vanish") && config.isOpSeeVanishEnabled())) {
                playerHidden++;
                continue;
            }
            // Add [AFK] if the player is AFK
            String playerName = player.getDisplayName();
            if (plugin.getUtilisGetters().isAFK(player)) {
                playerName = ChatColor.GRAY + "[AFK] " + playerName;
            }
            // If player has permission and config allows, show vanished players with [Vanished] prefix
            if (isVanished && sender instanceof Player && ((Player) sender).hasPermission("utilis.vanish") && config.isOpSeeVanishEnabled()) {
                playerName = ChatColor.GRAY + "[Vanished] " + playerName;
            }
            onlinePlayerNames.add(playerName);
        }
        // Calculate the visible player count
        int visiblePlayerCount = Bukkit.getOnlinePlayers().length - playerHidden;
        String playerCountMessage = messages.get("list.playerCountMessage")
                .replace("%playernumber%", String.valueOf(visiblePlayerCount))
                .replace("%max%", String.valueOf(Bukkit.getMaxPlayers()));
        playerCountMessage = ColorUtil.translateColorCodes(playerCountMessage);
        sender.sendMessage(playerCountMessage);
        String onlineMessage = messages.get("list.onlineMessage")
                .replace("%players%", String.join(", ", onlinePlayerNames));
        // Translate any color codes in the online players message
        onlineMessage = ColorUtil.translateColorCodes(onlineMessage);
        sender.sendMessage(onlineMessage);
    }
}