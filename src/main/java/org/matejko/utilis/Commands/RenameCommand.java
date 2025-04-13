package main.java.org.matejko.utilis.Commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import main.java.org.matejko.utilis.Managers.NickManager;

public class RenameCommand implements org.bukkit.command.CommandExecutor {
    private final NickManager nickManager;
    public RenameCommand(NickManager nickManager) {
        this.nickManager = nickManager;
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("§7[§2Utilis§7] " + ChatColor.RED + "Usage: /rename <player> <nickname>");
            return false;
        }
        String targetName = args[0];
        String newNickname = args[1];
        Player targetPlayer = getTargetPlayer(targetName);
        if (targetPlayer == null) {
            sender.sendMessage("§7[§2Utilis§7] " + ChatColor.RED + "Player with the name or nickname '" + targetName + "' not found.");
            return false;
        }
        if (!nickManager.isValidNickname(newNickname)) {
            sender.sendMessage("§7[§2Utilis§7] " + ChatColor.RED + "The nickname '" + "~" + newNickname + "' is already in use.");
            return false;
        }
        nickManager.setNickname(targetPlayer, newNickname);
        String playerColor = ChatColor.GRAY.toString();
        String sourceName = "console";
        if (sender instanceof Player) {
            Player player = (Player) sender;
            playerColor = ChatColor.valueOf(nickManager.getPlayerColor(player).toUpperCase()).toString();
            sourceName = player.getName();
        }
        sender.sendMessage("§7[§2Utilis§7] " + ChatColor.GRAY + "You have renamed " + playerColor + targetPlayer.getName() + ChatColor.GRAY + " to ~" + newNickname + ".");
        targetPlayer.sendMessage("§7[§2Utilis§7] " + ChatColor.GRAY + "You have been renamed to " + playerColor + "~" + newNickname + ChatColor.GRAY + " by " + sourceName + ".");
        return true;
    }
    private Player getTargetPlayer(String targetName) {
        Player targetPlayer = null;
        String nickname = "~" + targetName.toLowerCase();
        for (String playerName : nickManager.getPlayerData().keySet()) {
            String[] data = nickManager.getPlayerData().get(playerName);
            if (data != null && data[0].toLowerCase().contains(nickname.substring(1))) {
                targetPlayer = nickManager.getServer().getPlayer(playerName);
                break;
            }
        }
        if (targetPlayer == null) {
            targetPlayer = nickManager.getServer().getPlayerExact(targetName);
        }
        return targetPlayer;
    }
}
