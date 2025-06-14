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
import java.util.logging.Logger;

public class ColorCommand implements CommandExecutor {
    private final NickManager nickManager;
    private final CooldownManager cooldownManager;
    private final Messages messages;
    private final Logger logger;
    
    public ColorCommand(NickManager nickManager, CooldownManager cooldownManager, Messages messages) {
        this.nickManager = nickManager;
        this.cooldownManager = cooldownManager;
        this.messages = messages;
        this.logger = Logger.getLogger("Utilis");
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can change their color.");
            return false;
        }
        Player player = (Player) sender;
        if (cooldownManager.isOnColorCooldown(player)) {
            long remaining = cooldownManager.getRemainingColorCooldown(player);
            String cooldownMessage = messages.getMessage("commands-prefix") + ChatColor.RED + "You must wait " + remaining + " seconds before using /color again.";
            player.sendMessage(ColorUtil.translateColorCodes(cooldownMessage));
            return false;
        }
        if (args.length == 0) {
        	String usageMessagePart = messages.getMessage("color.usage");
            String prefixPart = messages.getMessage("commands-prefix");
            if (usageMessagePart == null) {
                logger.warning("[Utilis] Message 'color.set' is null. Please check your messages config.");
                usageMessagePart = "&cUsage: /color <color> or /color help";
            }
            if (prefixPart == null) {
                logger.warning("[Utilis] Message 'commands-prefix' is null. Please check your messages config.");
                prefixPart = "§7[§2Utilis§7] ";
            }
            String usageMessage = prefixPart + usageMessagePart;
            player.sendMessage(ColorUtil.translateColorCodes(usageMessage));
            return true;
        }
        if (args[0].equalsIgnoreCase("help")) {
            player.sendMessage(ChatColor.GOLD + "Available nickname colors: " +
                    ChatColor.BLACK + "black, " + ChatColor.DARK_BLUE + "dark_blue, " +
                    ChatColor.DARK_GREEN + "dark_green, " + ChatColor.DARK_AQUA + "dark_aqua, " +
                    ChatColor.DARK_RED + "dark_red, " + ChatColor.DARK_PURPLE + "dark_purple, " +
                    ChatColor.GOLD + "gold, " + ChatColor.GRAY + "gray, " +
                    ChatColor.DARK_GRAY + "dark_gray, " + ChatColor.BLUE + "blue, " +
                    ChatColor.GREEN + "green, " + ChatColor.AQUA + "aqua, " +
                    ChatColor.RED + "red, " + ChatColor.LIGHT_PURPLE + "light_purple, " +
                    ChatColor.YELLOW + "yellow, " + ChatColor.WHITE + "white.");
            return true;
        }
        // Validate the color
        String color = args[0].toUpperCase();
        if (!nickManager.isValidColor(color)) {
            String invalidMessage = messages.getMessage("color.invalid");
            if (invalidMessage == null) {
                logger.warning("[Utilis] Message 'color.invalid' is null. Please check your messages config.");
                invalidMessage = ChatColor.RED + "Invalid color.";
            }
            player.sendMessage(invalidMessage);
            return false;
        }
        // Inform the player of the change
        nickManager.setNicknameColor(player, color);
        cooldownManager.setColorCooldown(player);
        String colorSetPart = messages.getMessage("color.set");
        String prefixPart = messages.getMessage("commands-prefix");
        if (colorSetPart == null) {
            logger.warning("[Utilis] Message 'color.set' is null. Please check your messages config.");
            colorSetPart = "&7Your color has been changed to %color%";
        }
        if (prefixPart == null) {
            logger.warning("[Utilis] Message 'commands-prefix' is null. Please check your messages config.");
            prefixPart = "§7[§2Utilis§7] ";
        }
        String colorSetMessage = prefixPart + colorSetPart;
        // Format the message to include the actual color.
        ChatColor chatColor = ChatColor.valueOf(color);
        colorSetMessage = colorSetMessage.replace("%color%", chatColor + color);
        player.sendMessage(ColorUtil.translateColorCodes(colorSetMessage));
        return true;
    }
}
