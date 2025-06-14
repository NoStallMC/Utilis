package main.java.org.matejko.utilis.Commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import main.java.org.matejko.utilis.Managers.RecoverManager;
import java.util.UUID;
import main.java.org.matejko.utilis.FileCreator.Messages;
import main.java.org.matejko.utilis.Managers.ColorUtil;

public class RecoverCommand implements CommandExecutor {
    private final RecoverManager recoverManager;
    private final Messages messages;

    public RecoverCommand(RecoverManager recoverManager, Messages messages) {
    	this.messages = messages;
        this.recoverManager = recoverManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length != 1) {
            sender.sendMessage(ColorUtil.translateColorCodes(messages.getMessage("commands-prefix") + ChatColor.RED + "Usage: /recover <playername>"));
            return true;
        }
        String playerNameArg = args[0].toLowerCase();
        Player matchedPlayer = null;
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getName().toLowerCase().contains(playerNameArg)) {
                matchedPlayer = player;
                break;
            }
        }
        if (matchedPlayer == null) {
            sender.sendMessage(ColorUtil.translateColorCodes(messages.getMessage("commands-prefix") + ChatColor.RED + "Player not found or not online."));
            return true;
        }
        UUID uuid = matchedPlayer.getUniqueId();
        if (!recoverManager.hasSavedInventory(uuid)) {
            sender.sendMessage(ColorUtil.translateColorCodes(messages.getMessage("commands-prefix") + ChatColor.RED + "No saved inventory found for player " + matchedPlayer.getName()));
            return true;
        }
        ItemStack[] savedInventory = recoverManager.recoverPlayerInventory(uuid);
        if (savedInventory == null) {
            sender.sendMessage(ColorUtil.translateColorCodes(messages.getMessage("commands-prefix") + ChatColor.RED + "Failed to load saved inventory."));
            return true;
        }
        Player target = matchedPlayer;
        target.getInventory().clear();
        ItemStack[] contents = new ItemStack[36];
        ItemStack[] armor = new ItemStack[4];
        System.arraycopy(savedInventory, 0, contents, 0, 36);
        System.arraycopy(savedInventory, 36, armor, 0, 4);
        target.getInventory().setContents(contents);
        target.getInventory().setArmorContents(armor);
        sender.sendMessage(ColorUtil.translateColorCodes(messages.getMessage("commands-prefix") + ChatColor.GREEN + "Recovered inventory for player " + target.getName()));
        return true;
    }
}
