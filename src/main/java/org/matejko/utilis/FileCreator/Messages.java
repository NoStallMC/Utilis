package main.java.org.matejko.utilis.FileCreator;

import org.bukkit.entity.Player;
import org.bukkit.util.config.Configuration;
import main.java.org.matejko.utilis.Utilis;
import java.io.*;

public class Messages {
    private Utilis plugin;
    private File messagesFile;
    private File whitelistFile;
    private Configuration messagesConfig;

    public Messages(Utilis plugin) {
        this.plugin = plugin;
        setup();
    }

    private void setup() {
        messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        whitelistFile = new File(plugin.getDataFolder(), "whitelist.yml");

        if (!messagesFile.exists()) {
            try {
                copyFromJar("messages.yml", messagesFile);
                plugin.getLogger().info("[Utilis] messages.yml copied from JAR.");
            } catch (IOException e) {
                plugin.getLogger().warning("[Utilis] Error copying messages.yml from JAR: " + e.getMessage());
            }
        }

        if (!whitelistFile.exists()) {
            try {
                copyFromJar("whitelist.yml", whitelistFile);
                plugin.getLogger().info("[Utilis] whitelist.yml copied from JAR.");
            } catch (IOException e) {
                plugin.getLogger().warning("[Utilis] Error copying whitelist.yml from JAR: " + e.getMessage());
            }
        }

        // Load the messages config
        messagesConfig = new Configuration(messagesFile);
        messagesConfig.load();
    }

    private void copyFromJar(String resourceName, File outputFile) throws IOException {
        InputStream inputStream = plugin.getClass().getClassLoader().getResourceAsStream(resourceName);
        if (inputStream == null) {
            throw new IOException("Resource " + resourceName + " not found in JAR.");
        }
        // Ensure the file's parent directory exists
        if (!outputFile.exists()) {
            outputFile.getParentFile().mkdirs();
            outputFile.createNewFile();
        }
        // Copy the resource from the JAR to the plugin's data folder
        try (OutputStream outputStream = new FileOutputStream(outputFile)) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
        } finally {
            inputStream.close();
        }
    }

    public String getMessage(String path) {
        return messagesConfig.getString(path);
    }

    public String getCustomSleepMessage(Player player) {
        String path = "custom-messages." + player.getName();
        return messagesConfig.getString(path, null);  // Return null if no custom message is found
    }

    public String getCustomSleepMessage(String playerName) {
        String path = "custom-messages." + playerName;
        return messagesConfig.getString(path, null);  // Return null if no custom message is found
    }

    public void save() {
        messagesConfig.save();
    }

    public void load() {
        messagesConfig.load();
    }

    public void setCustomSleepMessage(Player player, String message) {
        setCustomSleepMessage(player.getName(), message); // Delegate to the String version
    }

    public void setCustomSleepMessage(String playerName, String message) {
        String path = "custom-messages." + playerName;
        messagesConfig.setProperty(path, message);
        save();  // Save immediately after setting the message
        messagesConfig.load();
    }

    public boolean removeCustomSleepMessage(String playerName) {
        String path = "custom-messages." + playerName;
        if (messagesConfig.getString(path) != null) {
            messagesConfig.setProperty(path, null);  // Remove the entry
            save();  // Save immediately after removing the message
            messagesConfig.load();
            return true;
        }
        return false;
    }

    public File getMessagesFile() {
        return messagesFile;
    }

    public Configuration getMessagesConfig() {
        return messagesConfig;
    }
}
