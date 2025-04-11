package main.java.org.matejko.utilis.Commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import main.java.org.matejko.utilis.Utilis;
public class UtilisDebugCommand implements CommandExecutor {
    private final Utilis plugin;
    
    public UtilisDebugCommand(Utilis plugin) {
        this.plugin = plugin;
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        @SuppressWarnings("static-access")
		String[] messages = new String[] {
            "§6[Utilis Debug Report]",
            plugin.getUtilisGetters().getConfig() == null || !plugin.getUtilisGetters().getConfig().isLoaded() ? 
                "§cConfig is not loaded properly." : "§aConfig is loaded correctly.",
            plugin.getUtilisGetters().getEssentials() == null ? 
                "§cEssentials plugin is not found!" : "§aEssentials plugin is loaded correctly.",
            plugin.getUtilisGetters().getDynmapPlugin() == null ? 
                "§cDynmap plugin is not found." : "§aDynmap plugin is loaded correctly.",
            plugin.getUtilisGetters().getSleepingManager() == null ? 
                "§cSleepingManager is not initialized." : "§aSleepingManager is initialized correctly.",
            plugin.getUtilisGetters().getNickManager() == null ? 
                "§cNickManager is not initialized." : "§aNickManager is initialized correctly.",
            plugin.getUtilisGetters().getMotdManager() == null ? 
                "§cMOTDManager is not initialized." : "§aMOTDManager is initialized correctly.",
            plugin.getUtilisGetters().getUtilisNotifier() == null ? 
                "§cUtilisNotifier is not initialized." : "§aUtilisNotifier is initialized correctly.",
            plugin.getUtilisGetters().getISeeManager() == null ? 
                "§cISeeManager is not initialized." : "§aISeeManager is initialized correctly.",
            plugin.getUtilisGetters().getVanishedPlayersManager() == null ? 
                "§cVanish system is not initialized." : "§aVanish system is working correctly.",
            plugin.getUtilisGetters().getVanishedPlayers() == null || plugin.getUtilisGetters().getVanishedPlayers().isEmpty() ? 
                "§cNo vanished players." : "§aVanished players: " + plugin.getUtilisGetters().getVanishedPlayers().size(),
        };
        // Send each line separately
        for (String line : messages) {
            sender.sendMessage(line);
        }
        return true;
    }
}
