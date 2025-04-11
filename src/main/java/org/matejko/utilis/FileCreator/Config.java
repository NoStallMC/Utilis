package main.java.org.matejko.utilis.FileCreator;

import org.bukkit.util.config.Configuration;
import main.java.org.matejko.utilis.Utilis;
import java.io.*;

public class Config {
	private final Utilis plugin;
    private final File configFile;
    private Configuration config;
    private boolean loaded = false;

    public Config(Utilis plugin) {
        this.plugin = plugin;
        configFile = new File(plugin.getDataFolder(), "config.yml");
        loadConfig();
    }
    public void loadConfig() {
        try {
            config = new Configuration(configFile);
            config.load();
            loaded = true;
        } catch (Exception e) {
        	plugin.getLogger().severe("[Utilis] Error loading config file: " + e.getMessage());
            e.printStackTrace();
            loaded = false;
        }
    }
    // Check if the config was loaded successfully
    public boolean isLoaded() {
        return loaded;
    }
    public File getConfigFile() {
        return configFile;
    }

    // Getter methods to access config values
    public boolean isListEnabled() {
        return config.getBoolean("commands.list", true);  // Default to true if not set
    }
    
    public boolean isVanishEnabled() {
        return config.getBoolean("commands.vanish", true);  // Default to true if not set
    }
    
    public boolean isDynmapHideEnabled() {
        return config.getBoolean("features.dynmap-hide", true);  // Default to true if not set
    }
    
    public boolean isOpSeeVanishEnabled() {
        return config.getBoolean("features.op-see-vanish", true);  // Default to true if not set
    }
    
    public boolean isMOTDEnabled() {
        return config.getBoolean("features.motd", true);  // Default to true if not set
    }

    public boolean isQoLEnabled() {
        return config.getBoolean("features.qol", true);  // Default to true if not set
    }
    
    public boolean isMinecartdmgFixEnabled() {
        return config.getBoolean("features.minecartdmg-fix", true);  // Default to true if not set
    }
    
    public boolean isNickEnabled() {
        return config.getBoolean("commands.nickname", true);  // Default to true if not set
    }

    public boolean isRenameEnabled() {
        return config.getBoolean("commands.rename", true);  // Default to true if not set
    }

    public boolean isColorEnabled() {
        return config.getBoolean("commands.color", true);  // Default to true if not set
    }

    public boolean isNickResetEnabled() {
        return config.getBoolean("commands.nickreset", true);  // Default to true if not set
    }

    public boolean isRealNameEnabled() {
        return config.getBoolean("commands.realname", true);  // Default to true if not set
    }

    public boolean isSleepingEnabled() {
        return config.getBoolean("features.night-skip", true);  // Default to true if not set
    }
    
    public boolean isCSMEnabled() {
        return config.getBoolean("features.custom-sleep-message", true);  // Default to true if not set
    }
    
    public boolean isChatFormattingEnabled() {
        return config.getBoolean("features.chat-formatting", true);  // Default to true if not set
    }
    
    public String getWorldFolder() {
        return config.getString("world-folder", "./world"); // Default to "./world" if not set
    }
    
    public int maxNicknameLength() {
        return config.getInt("features.nickname-length", 32);  // Default to 32 if not set
    }
    
	public boolean isUpdateEnabled() {
        return config.getBoolean("features.update-check", true);  // Default to true if not set
	}
	
	public boolean isJoinleaveEnabled() {
        return config.getBoolean("features.join-leave", true);  // Default to true if not set
	}
	
    public boolean isAntiSpamEnabled() {
        return config.getBoolean("anti-spam.enabled", true);  // Default to true if not set
    }

    public int getMessageLimit() {
        return config.getInt("anti-spam.message-limit", 10);  // Default to 10 messages if not set
    }

    public int getTimeWindow() {
        return config.getInt("anti-spam.time-window", 10);  // Default to 10 seconds if not set
    }
	
	public boolean isDebugEnabled() {
        return config.getBoolean("features.debug", false);  // Default to false if not set
	}
}
