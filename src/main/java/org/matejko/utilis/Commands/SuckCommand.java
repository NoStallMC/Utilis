package main.java.org.matejko.utilis.Commands;

import java.util.Collection;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import main.java.org.matejko.utilis.FileCreator.Messages;
import main.java.org.matejko.utilis.Managers.ColorUtil;
import org.bukkit.ChatColor;

public class SuckCommand implements CommandExecutor {
    private final Messages messages;

    public SuckCommand(Messages messages) {
        this.messages = messages;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("suck")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                    teleportItemsToPlayer(player);
                    player.sendMessage(ColorUtil.translateColorCodes(messages.getMessage("commands-prefix") + ChatColor.GRAY + "All dropped items have been teleported to you!"));
                }
             else {
               sender.sendMessage("Only players can use this command.");
            }
            return true;
        }
        return false;
    }
    private void teleportItemsToPlayer(Player player) {
        Location playerLocation = player.getLocation();
        Collection<org.bukkit.entity.Entity> entities = player.getWorld().getEntities();
        for (org.bukkit.entity.Entity entity : entities) {
            if (entity instanceof Item) {
                Item item = (Item) entity;
                if (item.getLocation().distance(playerLocation) < 100) {
                    item.teleport(playerLocation);
                }
            }
        }
    }
}
