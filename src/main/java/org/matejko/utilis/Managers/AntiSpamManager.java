package main.java.org.matejko.utilis.Managers;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChatEvent;
import main.java.org.matejko.utilis.Utilis;
import main.java.org.matejko.utilis.FileCreator.Config;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class AntiSpamManager implements Listener {
    private final Config config;
    private int messageLimit;
    private int timeWindow;
    private final Logger logger;
    private Map<Player, MessageTracker> playerMessages = new HashMap<>();

    public AntiSpamManager(Utilis plugin) {
        this.logger = plugin.getLogger();
        this.config = new Config(plugin);
        loadConfig();
    }
    private void loadConfig() {
        if (config.isLoaded()) {
            messageLimit = config.getMessageLimit();
            timeWindow = config.getTimeWindow();
            logger.info("[Utilis] Anti-Spam maximum of " + messageLimit + " messages in " + timeWindow + " seconds.");
        } else {
            messageLimit = 10;
            timeWindow = 10;
            logger.warning("[Utilis] Using default config: messageLimit=" + messageLimit + ", timeWindow=" + timeWindow + " seconds.");
        }
    }
    @EventHandler
    public void onPlayerChat(PlayerChatEvent event) {
        Player player = event.getPlayer();
        MessageTracker tracker = playerMessages.getOrDefault(player, new MessageTracker());
        if (config.isDebugEnabled()) {
        	logger.info("[Utilis] Player " + player.getName() + " sent a message at " + System.currentTimeMillis());
        }
        if (tracker.isSpamming()) {
            logger.info("Player " + player.getName() + " is spamming and has been kicked.");
            player.kickPlayer("[Utilis] You have been kicked for spamming.");
            event.setCancelled(true);
            return;
        }
        playerMessages.put(player, tracker.update());
    }
    private class MessageTracker {
        private final long[] timestamps = new long[messageLimit];
        private int currentIndex = 0;
        public MessageTracker update() {
            long currentTime = System.currentTimeMillis();
            timestamps[currentIndex] = currentTime;
            currentIndex = (currentIndex + 1) % messageLimit;
            StringBuilder timestampsLog = new StringBuilder("[Utilis] Updated Timestamps: ");
            for (int i = 0; i < messageLimit; i++) {
                timestampsLog.append(timestamps[i]).append(" ");
            }
            //logger.info(timestampsLog.toString());
            return this;
        }
        public boolean isSpamming() {
            long currentTime = System.currentTimeMillis();
            int messageCount = 0;
            for (int i = 0; i < messageLimit; i++) {
                if (timestamps[i] != 0 && currentTime - timestamps[i] <= timeWindow * 1000) {
                    messageCount++;
                }
            }
            if (config.isDebugEnabled()) {
            	logger.info("[Utilis] Messages in the last " + timeWindow + " seconds: " + messageCount);
            }
            return messageCount >= messageLimit;
        }
    }
}
