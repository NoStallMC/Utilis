package main.java.org.matejko.utilis.Commands;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import main.java.org.matejko.utilis.Utilis;
import main.java.org.matejko.utilis.FileCreator.Config;
import main.java.org.matejko.utilis.Managers.VanishUserManager;
import org.bukkit.ChatColor;

public class VanishCommand implements CommandExecutor {
	private final List<String> silentVanished = new ArrayList<>();
    private final Utilis plugin;
    private final Config config;

    public VanishCommand(Utilis plugin, Config config) {
        this.plugin = plugin;
        this.config = config;
    }
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be executed by a player.");
            return true;
        }
        Player player = (Player) sender;
        // Check if silent mode is enabled (e.g., /v s or /v silent)
        boolean silent = args.length > 0 && (args[0].equalsIgnoreCase("s") || args[0].equalsIgnoreCase("silent"));
        // If a player name is specified, toggle vanish for the target player
        if (args.length == 1 && !silent) {
            String targetName = args[0];
            Player targetPlayer = getTargetPlayer(targetName);
            if (targetPlayer == null) {
                player.sendMessage("§7[§2Utilis§7] " + ChatColor.RED + "Player not found or offline.");
                return true;
            }
            toggleVanish(targetPlayer, silent);
            return true;
        }
        // If no player name is provided, toggle vanish for the sender
        toggleVanish(player, silent);
        return true;
    }
    private void toggleVanish(Player player, boolean silent) {
    	if (silent) {
    	    if (!silentVanished.contains(player.getName())) {
    	        silentVanished.add(player.getName());
    	    }
    	    for (Player target : Bukkit.getOnlinePlayers()) {
    	        target.hidePlayer(player);
    	    }
    	    player.sendMessage("§7[§2Utilis§7] " + ChatColor.GRAY + "You are now hidden from players.");
    	    return;
    	}
        VanishUserManager vanishUser = null;
        // Check if the player is already vanished
        for (VanishUserManager vu : plugin.getUtilisGetters().getVanishedPlayers()) {
            if (vu.getPlayer().equals(player)) {
                vanishUser = vu;
                break;
            }
        }
        if (vanishUser != null) {
            // Player is already vanished, so unvanish them
            plugin.getUtilisGetters().getVanishedPlayers().remove(vanishUser);
            silentVanished.remove(player.getName());
            for (Player target : Bukkit.getOnlinePlayers()) {
                target.showPlayer(player);
            }
            if (!silent) {
                for (Player target : Bukkit.getOnlinePlayers()) {
                    if (target.hasPermission("utilis.vanish") && target != player) {
                        if (config.isOpSeeVanishEnabled()) {
                            target.sendMessage("§7[§2Utilis§7] " + player.getDisplayName() + ChatColor.GRAY + " has unvanished.");
                        }
                    } else {
                        // Only notify non-vanished players
                        if (!target.hasPermission("utilis.vanish")) {
                            plugin.getUtilisGetters().getUtilisNotifier().notifyUnvanished(player);
                        }
                    }
                }
            }
            player.sendMessage("§7[§2Utilis§7] " + ChatColor.GRAY + "You are now visible to other players.");
            if (!silent && config.isDynmapHideEnabled()) {
                plugin.getUtilisGetters().getDynmapManager().removeFromHiddenPlayersFile(player.getName());
            }
        } else {
            // Player isn't vanished, so vanish them
            VanishUserManager newVanishUser = new VanishUserManager(player, true);
            plugin.getUtilisGetters().getVanishedPlayers().add(newVanishUser);
            for (Player target : Bukkit.getOnlinePlayers()) {
                target.hidePlayer(player);
                // Only show player to those with the permission
                if (!silent && config.isOpSeeVanishEnabled() && target.hasPermission("utilis.vanish")) {
                    target.showPlayer(player);
                }
            }
            if (!silent) {
                for (Player target : Bukkit.getOnlinePlayers()) {
                    if (target.hasPermission("utilis.vanish") && target != player) {
                        if (config.isOpSeeVanishEnabled()) {
                            target.sendMessage("§7[§2Utilis§7] " + player.getDisplayName() + ChatColor.GRAY + " has vanished.");
                        }
                    } else {
                        // Only notify non-vanished players
                        if (!target.hasPermission("utilis.vanish")) {
                            plugin.getUtilisGetters().getUtilisNotifier().notifyVanished(player);
                        }
                    }
                }
            }
            player.sendMessage("§7[§2Utilis§7] " + ChatColor.GRAY + "You are now hidden from other players.");
            if (config.isDynmapHideEnabled()) {
                plugin.getUtilisGetters().getDynmapManager().addToHiddenPlayersFile(player.getName());
            }
        }
        // Save the updated list of vanished players
        plugin.getUtilisGetters().getVanishedPlayersManager().saveVanishedPlayers(plugin.getUtilisGetters().getVanishedPlayers());
    }

    private Player getTargetPlayer(String targetName) {
        Player targetPlayer = null;
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (onlinePlayer.getName().toLowerCase().contains(targetName.toLowerCase())) {
                targetPlayer = onlinePlayer;
                break;
            }
        }
        if (targetPlayer == null) {
            targetPlayer = Bukkit.getPlayerExact(targetName);
        }
        return targetPlayer;
    }
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player joining = event.getPlayer();
        for (String silentName : silentVanished) {
            Player silentPlayer = Bukkit.getPlayerExact(silentName);
            if (silentPlayer != null && silentPlayer.isOnline()) {
                joining.hidePlayer(silentPlayer);
            }
        }
    }
}
