package main.java.org.matejko.utilis.Commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import main.java.org.matejko.utilis.FileCreator.Messages;
import main.java.org.matejko.utilis.Managers.ColorUtil;
import main.java.org.matejko.utilis.Managers.CooldownManager;
import main.java.org.matejko.utilis.Managers.NickManager;

public class NickResetCommand implements CommandExecutor {
	private final Messages messages;
    private final NickManager nickManager;
    private final CooldownManager cooldownManager;

    public NickResetCommand(NickManager nickManager, CooldownManager cooldownManager, Messages messages) {
        this.nickManager = nickManager;
        this.messages = messages;
        this.cooldownManager = cooldownManager;
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can execute this command.");
            return false;
        }
        Player player = (Player) sender;
        if (cooldownManager.isOnResetCooldown(player)) {
            long remaining = cooldownManager.getRemainingResetCooldown(player);
            player.sendMessage(ColorUtil.translateColorCodes(messages.getMessage("commands-prefix") + ChatColor.RED + "You must wait " + remaining + " seconds before using /nickreset again."));
            return false;
        }
        nickManager.resetNickname(player);
        player.sendMessage(ColorUtil.translateColorCodes(messages.getMessage("commands-prefix") + ChatColor.GREEN + "Your nickname has been reset"));
        cooldownManager.setResetCooldown(player);
        return true;
    }
}
