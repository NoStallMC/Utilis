package main.java.org.matejko.utilis.Managers;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class QoLManager implements Listener{
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        // Check if the block is wooden_stairs (ID: 53) or cobblestone_stairs (ID: 67)
        if (block.getTypeId() == 53 || block.getTypeId() == 67 || block.getTypeId() == 30) {
            block.getWorld().dropItem(block.getLocation().add(0.5, 0.5, 0.5), new ItemStack(block.getTypeId(), 1));
            block.setTypeId(0); 
        }
        // Check if the block is either glass (ID: 20), or glowstone (ID: 89), or ice (ID: 79), or stone (ID: 1)
        if (block.getTypeId() == 20 || block.getTypeId() == 89 || block.getTypeId() == 79 || block.getTypeId() == 1) {
            if (player.getInventory().getItemInHand().getTypeId() == 285) {
                block.getWorld().dropItem(block.getLocation().add(0.5, 0.5, 0.5), new ItemStack(block.getTypeId(), 1));
                block.setTypeId(0); 
            }
        }
        // Check if the block is grass (ID: 2)
        if (block.getTypeId() == 2) {
            if (player.getInventory().getItemInHand().getTypeId() == 284) {
                block.getWorld().dropItem(block.getLocation().add(0.5, 0.5, 0.5), new ItemStack(block.getTypeId(), 1));
                block.setTypeId(0); 
            }
        }
    }
    @EventHandler
    public void onSignChange(SignChangeEvent event) {
        for (int i = 0; i < event.getLines().length; i++) {
            String line = event.getLine(i);
            if (line == null) continue;
            String coloredLine = ColorUtil.translateColorCodes(line);
            event.setLine(i, coloredLine);
        }
    }
}
