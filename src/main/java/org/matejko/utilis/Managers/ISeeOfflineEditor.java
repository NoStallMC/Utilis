package main.java.org.matejko.utilis.Managers;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import java.io.*;
import java.util.*;

public class ISeeOfflineEditor implements Listener {
    private final File baseDir;
    private final Map<UUID, ItemStack[][]> savedInventories = new HashMap<>();
    private final Map<UUID, UUID> offlineEditSession = new HashMap<>();
    private final RecoverManager recoverManager;

    public ISeeOfflineEditor(RecoverManager recoverManager) {
        this.recoverManager = recoverManager;
        this.baseDir = new File("plugins/Utilis/recinv");
        if (!baseDir.exists()) baseDir.mkdirs();
    }
    public void storeViewerInventory(Player viewer) {
        ItemStack[] contents = viewer.getInventory().getContents().clone();
        ItemStack[] armor = viewer.getInventory().getArmorContents().clone();
        savedInventories.put(viewer.getUniqueId(), new ItemStack[][] { contents, armor });
    }
    public void restoreViewerInventory(Player viewer) {
        ItemStack[][] stored = savedInventories.remove(viewer.getUniqueId());
        if (stored != null) {
            viewer.getInventory().setContents(stored[0]);
            viewer.getInventory().setArmorContents(stored[1]);
        }
    }
    public void openOfflineInventory(Player viewer, String targetName, UUID targetUUID) throws IOException {
        File file = new File(baseDir, targetName + ".inv");
        if (!file.exists()) {
            throw new FileNotFoundException("No saved inventory found for " + targetName);
        }
        storeViewerInventory(viewer);
        ItemStack[] combined = recoverManager.loadInventoryFromFile(file);
        if (combined == null) {
            throw new IOException("Failed to load inventory for " + targetName);
        }
        PlayerInventory inv = viewer.getInventory();
        inv.clear();
        ItemStack[] contents = Arrays.copyOfRange(combined, 0, 36);
        inv.setContents(contents);
        ItemStack[] armor = Arrays.copyOfRange(combined, 36, 40);
        inv.setArmorContents(armor);
        offlineEditSession.put(viewer.getUniqueId(), targetUUID);
    }
    public void saveOfflineInventory(Player viewer) throws IOException {
        UUID targetUUID = offlineEditSession.get(viewer.getUniqueId());
        if (targetUUID == null) {
        	return;
        }
        String targetName = recoverManager.uuidToName.get(targetUUID);
        if (targetName == null) {
            throw new IOException("No player name mapped for UUID " + targetUUID);
        }
        ItemStack[] contents = viewer.getInventory().getContents();
        ItemStack[] armor = viewer.getInventory().getArmorContents();
        recoverManager.saveInventoryToFile(targetName, targetUUID, contents, armor);
        offlineEditSession.remove(viewer.getUniqueId());
        restoreViewerInventory(viewer);
    }
    public void exitOfflineEditMode(Player viewer) {
        offlineEditSession.remove(viewer.getUniqueId());
        restoreViewerInventory(viewer);
    }
    public boolean isInOfflineEditMode(Player viewer) {
        return offlineEditSession.containsKey(viewer.getUniqueId());
    }
    public UUID getCurrentEditingTarget(Player viewer) {
        return offlineEditSession.get(viewer.getUniqueId());
    }
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        UUID joinedUUID = event.getPlayer().getUniqueId();
        List<UUID> viewersToExit = new ArrayList<>();
        for (Map.Entry<UUID, UUID> entry : offlineEditSession.entrySet()) {
            if (entry.getValue().equals(joinedUUID)) {
                viewersToExit.add(entry.getKey());
            }
        }
        for (UUID viewerUUID : viewersToExit) {
            Player viewer = event.getPlayer().getServer().getPlayer(viewerUUID);
            if (viewer != null && viewer.isOnline()) {
                try {
                    saveOfflineInventory(viewer);
                    exitOfflineEditMode(viewer);
                    viewer.sendMessage("§7[§2Utilis§7] §eThe player you were editing has joined the server. Exiting offline edit mode and saving changes.");
                } catch (IOException e) {
                    System.err.println("Failed to save edited inventory for viewer " + viewer.getName() + " after target joined.");
                    e.printStackTrace();
                }
            } else {
                offlineEditSession.remove(viewerUUID);
            }
        }
    }
}
