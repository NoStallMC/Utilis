package main.java.org.matejko.utilis.Managers;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class ChatFormattingManager implements Listener {
    private final JavaPlugin plugin;
    private boolean chatFormattingEnabled;

    public ChatFormattingManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.chatFormattingEnabled = plugin.getConfiguration().getBoolean("features.chat-formatting", true);
    }
    @EventHandler
    public void onChat(PlayerChatEvent event) {
        if (chatFormattingEnabled) {
            String message = event.getMessage();
            message = ColorUtil.translateColorCodes(message);
            event.setMessage(message);
        }
    }
    public void loadConfiguration() {
        this.chatFormattingEnabled = plugin.getConfiguration().getBoolean("features.chat-formatting", true);
    }
}
