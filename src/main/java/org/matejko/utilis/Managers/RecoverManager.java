package main.java.org.matejko.utilis.Managers;

import main.java.org.matejko.utilis.Utilis;
import main.java.org.matejko.utilis.FileCreator.Config;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.yaml.snakeyaml.Yaml;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class RecoverManager implements Listener {
    private final Utilis plugin;
    private final Config config;
    private final File saveDir;
    private final File uuidFile;
    final Map<UUID, String> uuidToName = new HashMap<>();

    public RecoverManager(Utilis plugin) {
        this.plugin = plugin;
        this.config = new Config(plugin);
        this.saveDir = new File(plugin.getDataFolder(), "recinv");
        this.uuidFile = new File(plugin.getDataFolder(), "uuids.yml");
        if (!saveDir.exists() && !saveDir.mkdirs()) {
            plugin.getLogger().warning("[Utilis] Could not create recinv directory!");
        }
        loadUUIDMap();
    }
    @SuppressWarnings("unchecked")
    private void loadUUIDMap() {
        if (!uuidFile.exists()) {
            if (config.isDebugEnabled()) plugin.getLogger().info("[Utilis] No uuids.yml found, starting fresh.");
            return;
        }
        try (InputStream input = new FileInputStream(uuidFile)) {
            Yaml yaml = new Yaml();
            Object loaded = yaml.load(input);
            if (loaded instanceof Map) {
                Map<String, String> data = (Map<String, String>) loaded;
                for (Map.Entry<String, String> e : data.entrySet()) {
                    try {
                        uuidToName.put(UUID.fromString(e.getKey()), e.getValue().toLowerCase());
                    } catch (IllegalArgumentException ignored) {}
                }
                if (config.isDebugEnabled()) {
                    plugin.getLogger().info("[Utilis] Loaded " + uuidToName.size() + " UUID mappings.");
                }
            }
        } catch (IOException e) {
            plugin.getLogger().warning("[Utilis] Failed to load uuids.yml: " + e.getMessage());
        }
    }
    public void saveUUIDMap() {
        Yaml yaml = new Yaml();
        Map<String, String> data = new LinkedHashMap<>();
        for (Map.Entry<UUID, String> e : uuidToName.entrySet()) {
            data.put(e.getKey().toString(), e.getValue());
        }
        try (Writer writer = new FileWriter(uuidFile)) {
            yaml.dump(data, writer);
            if (config.isDebugEnabled()) {
                plugin.getLogger().info("[Utilis] Saved uuids.yml with " + data.size() + " entries.");
            }
        } catch (IOException e) {
            plugin.getLogger().warning("[Utilis] Failed to save uuids.yml: " + e.getMessage());
        }
    }
    private File getInventoryFile(UUID uuid) {
        String playerName = uuidToName.get(uuid);
        if (playerName == null) return null;
        return new File(saveDir, playerName.toLowerCase() + ".inv");
    }
    public boolean hasSavedInventory(UUID uuid) {
        File file = getInventoryFile(uuid);
        return file != null && file.exists();
    }
    public void savePlayerInventory(Player player) {
        if (player == null || player.getInventory() == null) return;
        UUID uuid = player.getUniqueId();
        String playerName = player.getName().toLowerCase();
        if (!playerName.equals(uuidToName.get(uuid))) {
            uuidToName.put(uuid, playerName);
            saveUUIDMap();
        }
        ItemStack[] contents = player.getInventory().getContents();
        ItemStack[] armor = player.getInventory().getArmorContents();
        saveInventoryToFile(playerName, uuid, contents, armor);
    }
    void saveInventoryToFile(String playerName, UUID uuid, ItemStack[] contents, ItemStack[] armor) {
        File file = new File(saveDir, playerName.toLowerCase() + ".inv");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            String readableTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            writer.write("LastUpdate=" + readableTime + "\n");
            writeItems(writer, "inv", contents);
            writeItems(writer, "armor", armor);
        } catch (IOException e) {
            if (config.isDebugEnabled()) {
                plugin.getLogger().warning("[Utilis] Failed to save inventory for " + playerName + ": " + e.getMessage());
            }
        }
    }
    private void writeItems(BufferedWriter writer, String prefix, ItemStack[] items) throws IOException {
        for (int i = 0; i < items.length; i++) {
            ItemStack item = items[i];
            if (item != null) {
                writer.write(prefix + "." + i + "=" + item.getTypeId() + "," + item.getAmount() + "," + item.getDurability() + "\n");
            }
        }
    }
    public ItemStack[] recoverPlayerInventory(UUID uuid) {
        File file = getInventoryFile(uuid);
        if (file == null || !file.exists()) return null;
        ItemStack[] loaded = loadInventoryFromFile(file);
        if (loaded == null) return null;
        ItemStack[] contents = Arrays.copyOfRange(loaded, 0, 36);
        ItemStack[] armor = Arrays.copyOfRange(loaded, 36, 40);
        return concat(contents, armor);
    }
    ItemStack[] loadInventoryFromFile(File file) {
        Properties props = new Properties();
        try (FileReader reader = new FileReader(file)) {
            props.load(reader);
        } catch (IOException e) {
            if (config.isDebugEnabled()) {
                plugin.getLogger().warning("[Utilis] Failed to load inventory from file " + file.getName() + ": " + e.getMessage());
            }
            return null;
        }
        ItemStack[] contents = new ItemStack[36];
        ItemStack[] armor = new ItemStack[4];
        for (int i = 0; i < 36; i++) {
            String raw = props.getProperty("inv." + i);
            if (raw != null) {
                ItemStack item = parseItemStack(raw);
                if (item != null) contents[i] = item;
            }
        }
        for (int i = 0; i < 4; i++) {
            String raw = props.getProperty("armor." + i);
            if (raw != null) {
                ItemStack item = parseItemStack(raw);
                if (item != null) armor[i] = item;
            }
        }
        return concat(contents, armor);
    }
    private ItemStack parseItemStack(String raw) {
        try {
            String[] split = raw.split(",");
            int id = Integer.parseInt(split[0]);
            int amount = Integer.parseInt(split[1]);
            short durability = Short.parseShort(split[2]);
            return new ItemStack(id, amount, durability);
        } catch (Exception ignored) {}
        return null;
    }
    private ItemStack[] concat(ItemStack[] contents, ItemStack[] armor) {
        ItemStack[] combined = new ItemStack[contents.length + armor.length];
        System.arraycopy(contents, 0, combined, 0, contents.length);
        System.arraycopy(armor, 0, combined, contents.length, armor.length);
        return combined;
    }
    public UUID getUUIDFromSavedName(String name) {
        String lookup = name.toLowerCase();
        for (Map.Entry<UUID, String> entry : uuidToName.entrySet()) {
            if (entry.getValue().equalsIgnoreCase(lookup)) {
                return entry.getKey();
            }
        }
        return null;
    }
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        String currentName = player.getName().toLowerCase();
        String mappedName = uuidToName.get(uuid);
        if (mappedName == null) {
            uuidToName.put(uuid, currentName);
            saveUUIDMap();
        } else if (!mappedName.equals(currentName)) {
            File oldFile = new File(saveDir, mappedName.toLowerCase() + ".inv");
            File newFile = new File(saveDir, currentName.toLowerCase() + ".inv");
            if (oldFile.exists() && !newFile.exists()) {
                if (oldFile.renameTo(newFile)) {
                    if (config.isDebugEnabled()) {
                        plugin.getLogger().info("[Utilis] Renamed inventory file from " + mappedName + " to " + currentName);
                    }
                } else if (config.isDebugEnabled()) {
                    plugin.getLogger().warning("[Utilis] Failed to rename inventory file " + oldFile.getName());
                }
            }
            uuidToName.put(uuid, currentName);
            saveUUIDMap();
        }
        File file = new File(saveDir, currentName.toLowerCase() + ".inv");
        if (file.exists()) {
            ItemStack[] loaded = loadInventoryFromFile(file);
            if (loaded != null) {
                ItemStack[] contents = Arrays.copyOfRange(loaded, 0, 36);
                ItemStack[] armor = Arrays.copyOfRange(loaded, 36, 40);
                player.getInventory().setContents(contents);
                player.getInventory().setArmorContents(armor);
                if (config.isDebugEnabled()) {
                    plugin.getLogger().info("[Utilis] Inventory restored for " + currentName);
                }
            }
        } else {
            saveInventoryToFile(currentName.toLowerCase(), uuid, player.getInventory().getContents(), player.getInventory().getArmorContents());
            if (config.isDebugEnabled()) {
                plugin.getLogger().info("[Utilis] Created new inventory file for " + currentName);
            }
        }
    }
    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof Player) {
            savePlayerInventory((Player) entity);
            if (config.isDebugEnabled()) {
                plugin.getLogger().info("[Utilis] Inventory saved for " + ((Player) entity).getName() + " on death.");
            }
        }
    }
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (isInventoryEmpty(player)) {
            if (config.isDebugEnabled()) {
                plugin.getLogger().info("[Utilis] Skipping save on quit for " + player.getName() + " (empty inventory).");
            }
            return;
        }
        savePlayerInventory(player);
        if (config.isDebugEnabled()) {
            plugin.getLogger().info("[Utilis] Inventory saved for " + player.getName() + " on quit.");
        }
    }
    private boolean isInventoryEmpty(Player player) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null) return false;
        }
        return true;
    }
}
