package main.java.org.matejko.utilis.UtilisCore;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import main.java.org.matejko.utilis.Utilis;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@SuppressWarnings("unused")
public class UtilisConfigUpdater {
    private static final String CONFIG_FILE_PATH = "plugins/Utilis/config.yml";
    private static final String DEFAULT_CONFIG_FILE_PATH = "/config.yml";
    private static final String OLD_CONFIG_FILE_PATH = "plugins/Utilis/oldconfig.yml";
    private final Logger logger;
	private static Utilis plugin;
	
    public UtilisConfigUpdater(Utilis plugin) {
        UtilisConfigUpdater.plugin = plugin;
        this.logger = plugin.getLogger();
    }
    public void checkAndUpdateConfig() {
        boolean requiresRestart = false;
        try {
            // Step 1: Check if the config exists in the plugin folder
            File configFile = new File(CONFIG_FILE_PATH);
            boolean configExists = configFile.exists();
            if (!configExists) {
                copyDefaultConfigToServer();
                return;
            }
            // Step 2: Load the current config
            Map<String, Object> currentConfig = loadConfig(CONFIG_FILE_PATH);

            // Step 3: Get current version from config.yml on the server
            String currentVersion = getCurrentConfigVersion(currentConfig);

            // Step 4: Check if the current config version is outdated or missing
            if (currentVersion == null || isVersionOutdated(currentVersion)) {
            	plugin.getLogger().info("[Utilis] Config is outdated or missing version. Merging will begin.");
            	
                // Step 5: Backup old config
                backupOldConfig();

                // Step 6: Load default config from JAR
                Map<String, Object> defaultConfig = loadDefaultConfig();

                // Step 7: Merge old config values into the new config
                Map<String, Object> oldConfig = loadConfig(OLD_CONFIG_FILE_PATH);
                boolean mergeSuccess = mergeConfigs(defaultConfig, oldConfig);

                // Step 8: Save the updated config back to the server
                if (mergeSuccess) {
                    saveConfig(CONFIG_FILE_PATH, defaultConfig);
                    Bukkit.getScheduler().scheduleAsyncDelayedTask(plugin, new Runnable() {
                        @Override
                        public void run() {
                        	plugin.getLogger().info("[Utilis] Config merger is done!");
                        	plugin.getLogger().severe("[Utilis] Server restart is needed!");
                        }
                    }, 40L);
                } else {
                	plugin.getLogger().severe("[Utilis] Config merger failed: Could not merge old config.");
                }
            } else {
                String configVersion = currentVersion != null ? currentVersion : "unknown";
                plugin.getLogger().info("[Utilis] Config is up to date! (v" + configVersion + ")");
            }

        } catch (Exception e) {
            //logger.severe("An error occurred while checking and updating the config: " + e.getMessage()); //FIX THIS IN FUTURE !!!
            //e.printStackTrace();
        }
    }
    @SuppressWarnings("unchecked")
    private static Map<String, Object> loadConfig(String filePath) throws FileNotFoundException {
        Yaml yaml = new Yaml();
        FileInputStream fileInputStream = new FileInputStream(filePath);
        return (Map<String, Object>) yaml.load(fileInputStream);
    }
    @SuppressWarnings("unchecked")
    private static Map<String, Object> loadDefaultConfig() throws IOException {
        Yaml yaml = new Yaml();
        InputStream inputStream = UtilisConfigUpdater.class.getResourceAsStream(DEFAULT_CONFIG_FILE_PATH);
        if (inputStream == null) {
            throw new FileNotFoundException("[Utilis] Default config (config.yml) not found in JAR.");
        }
        return (Map<String, Object>) yaml.load(inputStream);
    }
    private static String getCurrentConfigVersion(Map<String, Object> currentConfig) {
        Object versionObject = currentConfig.get("Version");
        if (versionObject != null) {
            return versionObject.toString();
        }
        return null;
    }
    private static boolean isVersionOutdated(String currentVersion) throws IOException {
        String serverVersion = getServerVersionFromJar();
        if (serverVersion == null) {
        	plugin.getLogger().warning("[Utilis] config version missing, cannot compare.");
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
    private static String getServerVersionFromJar() throws FileNotFoundException {
        Yaml yaml = new Yaml();
        InputStream inputStream = UtilisConfigUpdater.class.getResourceAsStream(DEFAULT_CONFIG_FILE_PATH);
        if (inputStream == null) {
            throw new FileNotFoundException("[Utilis] Default config (config.yml) not found in JAR.");
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> defaultConfig = (Map<String, Object>) yaml.load(inputStream);
        Object versionObject = defaultConfig.get("Version");
        if (versionObject != null) {
            return versionObject.toString();
        }
        return null;
    }
    private static void saveConfig(String filePath, Map<String, Object> config) throws IOException {
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        Yaml yaml = new Yaml(options);
        FileWriter writer = new FileWriter(filePath);
        yaml.dump(config, writer);
    }
    private static void backupOldConfig() throws IOException {
        File configFile = new File(CONFIG_FILE_PATH);
        File backupFile = new File(OLD_CONFIG_FILE_PATH);
        if (configFile.exists()) {
            try {
                try (InputStream inputStream = new FileInputStream(configFile);
                     OutputStream outputStream = new FileOutputStream(backupFile)) {
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }
                }
                plugin.getLogger().info("[Utilis] Old config successfully copied to oldconfig.yml.");
            } catch (IOException e) {
            	plugin.getLogger().severe("[Utilis] Failed to copy old config file: " + e.getMessage());
                throw e;
            }
        }
    }
    private static void copyDefaultConfigToServer() throws IOException {
        File configDirectory = new File("plugins/Utilis");
        if (!configDirectory.exists()) {
            if (configDirectory.mkdirs()) {
            	plugin.getLogger().info("[Utilis] Created directory: " + configDirectory.getPath());
            } else {
            	plugin.getLogger().severe("[Utilis] Failed to create directory: " + configDirectory.getPath());
                throw new IOException("[Utilis] Failed to create directory: " + configDirectory.getPath());
            }
        }
        InputStream inputStream = UtilisConfigUpdater.class.getResourceAsStream(DEFAULT_CONFIG_FILE_PATH);
        if (inputStream == null) {
            throw new FileNotFoundException("[Utilis] Default config (config.yml) not found in JAR.");
        }
        File outputFile = new File(CONFIG_FILE_PATH);
        try (OutputStream outputStream = new FileOutputStream(outputFile)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        }
        plugin.getLogger().info("[Utilis] Default config copied from JAR.");
    }
    @SuppressWarnings("unchecked")
    private static boolean mergeConfigs(Map<String, Object> newConfig, Map<String, Object> oldConfig) {
        try {
            for (Map.Entry<String, Object> entry : oldConfig.entrySet()) {
                String key = entry.getKey();
                Object oldValue = entry.getValue();
                if ("Version".equalsIgnoreCase(key)) {
                    continue;
                }
                if (newConfig.containsKey(key)) {
                    if (newConfig.get(key) instanceof Map && oldValue instanceof Map) {
                        Map<String, Object> newSubConfig = (Map<String, Object>) newConfig.get(key);
                        Map<String, Object> oldSubConfig = (Map<String, Object>) oldValue;
                        mergeConfigs(newSubConfig, oldSubConfig);
                    }
                    else {
                        mergeValue(newConfig, key, oldValue);
                    }
                } else {
                    newConfig.put(key, oldValue);
                }
            }
            return true;
        } catch (Exception e) {
        	plugin.getLogger().severe("[Utilis] Merge failed: " + e.getMessage());
            return false;
        }
    }
    @SuppressWarnings("unchecked")
    private static void mergeValue(Map<String, Object> newConfig, String key, Object oldValue) {
        if (oldValue instanceof Boolean) {
            newConfig.put(key, ((Boolean) oldValue));
        } else if (oldValue instanceof String) {
            newConfig.put(key, ((String) oldValue));
        } else if (oldValue instanceof Integer) {
            newConfig.put(key, ((Integer) oldValue));
        } else if (oldValue instanceof Double) {
            newConfig.put(key, ((Double) oldValue));
        } else if (oldValue instanceof List) {
            newConfig.put(key, ((List<?>) oldValue));
        } else if (oldValue instanceof Map) {
            newConfig.put(key, ((Map<String, Object>) oldValue));
        } else {
            newConfig.put(key, oldValue);
        }
    }
}
