package main.java.org.matejko.plugin.Commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import main.java.org.matejko.plugin.Utilis;
import main.java.org.matejko.plugin.Managers.VanishUserManager;
import org.bukkit.ChatColor;

public class VanishCommand implements CommandExecutor {
    private final Utilis plugin;

    public VanishCommand(Utilis plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be executed by a player.");
            return true;
        }

        Player player = (Player) sender;
        VanishUserManager vanishUser = plugin.getUtilisGetters().getVanishedPlayers().stream()
                .filter(vu -> vu.getPlayer().equals(player))
                .findFirst()
                .orElse(null);

        if (vanishUser != null) {
            plugin.getUtilisGetters().getVanishedPlayers().remove(vanishUser);
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.showPlayer(player);
            }
            plugin.getUtilisGetters().getUtilisNotifier().notifyUnvanished(player);
            plugin.getUtilisGetters().getDynmapManager().removeFromHiddenPlayersFile(player.getName()); // Show them on Dynmap.
            player.sendMessage(ChatColor.GRAY + "You are now visible to other players.");
        } else {
        	VanishUserManager newVanishUser = new VanishUserManager(player, true);
            plugin.getUtilisGetters().getVanishedPlayers().add(newVanishUser);
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.hidePlayer(player);
            }
            plugin.getUtilisGetters().getUtilisNotifier().notifyVanished(player); 
            plugin.getUtilisGetters().getDynmapManager().addToHiddenPlayersFile(player.getName()); // Hide them on Dynmap.
            player.sendMessage(ChatColor.GRAY + "You are now hidden from other players.");
        }
        // Save the updated list of vanished players to the file
        plugin.getUtilisGetters().getVanishedPlayersManager().saveVanishedPlayers(plugin.getUtilisGetters().getVanishedPlayers());
        return true;
    }

    public boolean isPlayerVanished(Player player) {
        return plugin.getUtilisGetters().getVanishedPlayers().stream()
                .anyMatch(vu -> vu.getPlayer().equals(player));
    }
}
