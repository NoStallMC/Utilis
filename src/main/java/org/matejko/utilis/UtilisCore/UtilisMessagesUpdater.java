package main.java.org.matejko.utilis.UtilisCore;

import org.bukkit.Bukkit;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import main.java.org.matejko.utilis.Utilis;
import java.io.*;
import java.util.Map;

public class UtilisMessagesUpdater {
    private static final String MESSAGES_FILE_PATH = "plugins/Utilis/messages.yml";
    private static final String DEFAULT_MESSAGES_FILE_PATH = "/messages.yml";
    private static final String OLD_MESSAGES_FILE_PATH = "plugins/Utilis/oldmessages.yml";
    private static Utilis plugin;

    public UtilisMessagesUpdater(Utilis plugin) {
        UtilisMessagesUpdater.plugin = plugin;
    }
    public void checkAndUpdateConfig() {
        try {
            File configFile = new File(MESSAGES_FILE_PATH);
            if (!configFile.exists()) {
                copyDefaultConfigToServer();
                return;
            }
            Map<String, Object> currentConfig = loadConfig(MESSAGES_FILE_PATH);
            String currentVersion = getCurrentConfigVersion(currentConfig);
            if (currentVersion == null || isVersionOutdated(currentVersion)) {
                plugin.getLogger().info("[Utilis] messages.yml is outdated or missing version. Merging will begin.");
                backupOldConfig();
                Map<String, Object> defaultConfig = loadDefaultConfig();
                Map<String, Object> oldConfig = loadConfig(OLD_MESSAGES_FILE_PATH);
                boolean mergeSuccess = mergeConfigs(defaultConfig, oldConfig);
                if (mergeSuccess) {
                    saveConfigWithComments(MESSAGES_FILE_PATH, defaultConfig);
                    Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                        plugin.getLogger().info("[Utilis] Messages merger is done!");
                        plugin.getLogger().severe("[Utilis] Server restart is needed!");
                    }, 40L);
                } else {
                    plugin.getLogger().severe("[Utilis] messages.yml merger failed: Could not merge old messages.yml.");
                }
            } else {
                plugin.getLogger().info("[Utilis] messages.yml is up to date! (v" + currentVersion + ")");
            }
        } catch (Exception e) {
          //  plugin.getLogger().severe("An error occurred while checking and updating the messages.yml: " + e.getMessage());
          //  e.printStackTrace();  :P -this is junk error
        }
    }
    @SuppressWarnings("unchecked")
    private static Map<String, Object> loadConfig(String filePath) throws IOException {
        try (FileInputStream fileInputStream = new FileInputStream(filePath)) {
            Yaml yaml = new Yaml();
            return (Map<String, Object>) yaml.load(fileInputStream);
        }
    }
    @SuppressWarnings("unchecked")
    private static Map<String, Object> loadDefaultConfig() throws IOException {
        try (InputStream inputStream = UtilisMessagesUpdater.class.getResourceAsStream(DEFAULT_MESSAGES_FILE_PATH)) {
            if (inputStream == null) {
               throw new FileNotFoundException();
            }
            Yaml yaml = new Yaml();
            return (Map<String, Object>) yaml.load(inputStream);
        }
    }
    private static String getCurrentConfigVersion(Map<String, Object> currentConfig) {
        Object versionObject = currentConfig.get("Version");
        return versionObject != null ? versionObject.toString() : null;
    }
    private static boolean isVersionOutdated(String currentVersion) throws IOException {
        String serverVersion = getServerVersionFromJar();
        if (serverVersion == null) {
            plugin.getLogger().warning("[Utilis] messages.yml version missing, cannot compare.");
            return false;
        }
        String[] currentParts = currentVersion.split("\\.");
        String[] serverParts = serverVersion.split("\\.");
        for (int i = 0; i < Math.min(currentParts.length, serverParts.length); i++) {
            int current = Integer.parseInt(currentParts[i]);
            int server = Integer.parseInt(serverParts[i]);
            if (current < server) return true;
            if (current > server) return false;
        }
        return false;
    }
    private static String getServerVersionFromJar() throws IOException {
        try (InputStream inputStream = UtilisMessagesUpdater.class.getResourceAsStream(DEFAULT_MESSAGES_FILE_PATH)) {
            if (inputStream == null) {
                throw new FileNotFoundException();
            }
            Yaml yaml = new Yaml();
            @SuppressWarnings("unchecked")
            Map<String, Object> defaultConfig = (Map<String, Object>) yaml.load(inputStream);
            Object versionObject = defaultConfig.get("Version");
            return versionObject != null ? versionObject.toString() : null;
        }
    }
    private static void saveConfigWithComments(String filePath, Map<String, Object> config) throws IOException {
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        Yaml yaml = new Yaml(options);
        String yamlContent = yaml.dump(config);
        try (InputStream inputStream = UtilisMessagesUpdater.class.getResourceAsStream(DEFAULT_MESSAGES_FILE_PATH);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
             FileWriter writer = new FileWriter(filePath)) {
            if (inputStream == null) {
                throw new FileNotFoundException();
            }
            StringBuilder commentsBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("#")) {
                    commentsBuilder.append(line).append("\n");
                }
            }
            writer.write(yamlContent + "\n" + commentsBuilder.toString());
        }
    }
    private static void backupOldConfig() throws IOException {
        File configFile = new File(MESSAGES_FILE_PATH);
        File backupFile = new File(OLD_MESSAGES_FILE_PATH);
        if (configFile.exists()) {
            try (InputStream inputStream = new FileInputStream(configFile);
                 OutputStream outputStream = new FileOutputStream(backupFile)) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                plugin.getLogger().info("[Utilis] Old messages.yml successfully backed up.");
            } catch (IOException e) {
                plugin.getLogger().severe("[Utilis] Failed to backup old messages.yml: " + e.getMessage());
                throw e;
            }
        }
    }
    private static void copyDefaultConfigToServer() throws IOException {
        File configDirectory = new File("plugins/Utilis");
        if (!configDirectory.exists() && !configDirectory.mkdirs()) {
            throw new IOException("[Utilis] Failed to create directory: " + configDirectory.getPath());
        }
        try (InputStream inputStream = UtilisMessagesUpdater.class.getResourceAsStream(DEFAULT_MESSAGES_FILE_PATH);
             OutputStream outputStream = new FileOutputStream(new File(MESSAGES_FILE_PATH))) {
            if (inputStream == null) {
                throw new FileNotFoundException();
            }
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            plugin.getLogger().info("[Utilis] Default messages.yml copied from JAR.");
        }
    }
    @SuppressWarnings("unchecked")
    private static boolean mergeConfigs(Map<String, Object> newConfig, Map<String, Object> oldConfig) {
        try {
            for (Map.Entry<String, Object> entry : oldConfig.entrySet()) {
                String key = entry.getKey();
                Object oldValue = entry.getValue();
                if ("Version".equalsIgnoreCase(key)) continue;

                if (newConfig.containsKey(key)) {
                    if (newConfig.get(key) instanceof Map && oldValue instanceof Map) {
                        Map<String, Object> newSubConfig = (Map<String, Object>) newConfig.get(key);
                        Map<String, Object> oldSubConfig = (Map<String, Object>) oldValue;
                        mergeConfigs(newSubConfig, oldSubConfig);
                    } else {
                        newConfig.put(key, oldValue);
                    }
                } else {
                    newConfig.put(key, oldValue);
                }
            }
            return true;
        } catch (Exception e) {
            plugin.getLogger().severe("[Utilis] Messages Merger failed: " + e.getMessage());
            return false;
        }
    }
}
