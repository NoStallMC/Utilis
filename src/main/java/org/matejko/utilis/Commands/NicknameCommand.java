package main.java.org.matejko.utilis.Commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import main.java.org.matejko.utilis.FileCreator.Config;
import main.java.org.matejko.utilis.FileCreator.Messages;
import main.java.org.matejko.utilis.Managers.ColorUtil;
import main.java.org.matejko.utilis.Managers.CooldownManager;
import main.java.org.matejko.utilis.Managers.NickManager;
import java.util.logging.Logger;

public class NicknameCommand implements org.bukkit.command.CommandExecutor {
    private final NickManager nickManager;
    private final CooldownManager cooldownManager;
    private final Messages messages;
    private final Config config;
    private final Logger logger;

    public NicknameCommand(NickManager nickManager, CooldownManager cooldownManager, Messages messages, Config config) {
        this.nickManager = nickManager;
        this.cooldownManager = cooldownManager;
        this.messages = messages;
        this.config = config;
        this.logger = Logger.getLogger("Utilis");
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can execute this command.");
            return false;
        }
        // Check if the player is on cooldown
        Player player = (Player) sender;
        if (cooldownManager.isOnNicknameCooldown(player)) {
            long remaining = cooldownManager.getRemainingNicknameCooldown(player);
            String cooldownMessage = ColorUtil.translateColorCodes(messages.getMessage("commands-prefix") + ChatColor.RED + "You must wait " + remaining + " seconds before using /nickname again.");
            player.sendMessage(cooldownMessage);
            return false;
        }
        // If no nickname is provided
        if (args.length == 0) {
            player.sendMessage(ColorUtil.translateColorCodes(messages.getMessage("commands-prefix") + ChatColor.RED + "Usage: /nickname <nickname>"));
            return false;
        }
        // Check nickname length
        String nickname = args[0];
        int maxNicknameLength = config.maxNicknameLength();
        if (maxNicknameLength > 0 && nickname.length() > maxNicknameLength) {
            player.sendMessage(ColorUtil.translateColorCodes(messages.getMessage("commands-prefix") + ChatColor.RED + "The nickname is too long. Maximum allowed length is " + maxNicknameLength + " characters."));
            return false;
        }
        // Check if the nickname is available
        if (!nickManager.isValidNickname(nickname)) {
            String nicknameUsedMessage = messages.getMessage("nickname.used");
            if (nicknameUsedMessage == null) {
                logger.warning("[Utilis] Message 'nickname.used' is null. Please check your messages config.");
                nicknameUsedMessage = messages.getMessage("commands-prefix") + "That nickname is already in use.";
            }
            player.sendMessage(ColorUtil.translateColorCodes(formatMessage(nicknameUsedMessage, player, nickname)));
            return false;
        }
        // Set the player's nickname
        try {
            nickManager.setNickname(player, nickname);
        } catch (Exception e) {
            logger.severe("[Utilis] Error while setting nickname for player " + player.getName());
            e.printStackTrace();
            player.sendMessage(ChatColor.RED + "An error occurred while setting your nickname.");
            return false;
        }
        cooldownManager.setNicknameCooldown(player);
        String nicknameSetMessage = messages.getMessage("nickname.set");
        if (nicknameSetMessage == null) {
            logger.warning("[Utilis] Message 'nickname.set' is null. Please check your messages config.");
            nicknameSetMessage = messages.getMessage("commands-prefix") + "&7Your nickname has been set to: %nickname%";
        }
        player.sendMessage(formatMessage(nicknameSetMessage, player, nickname));
        return true;
    }
    private String formatMessage(String message, Player player) {
        message = message.replace("%player%", player.getDisplayName());
        return ColorUtil.translateColorCodes(message);
    }
    private String formatMessage(String message, Player player, String nickname) {
        String playerColor = nickManager.getPlayerColor(player);
        message = message.replace("%nickname%", ChatColor.valueOf(playerColor.toUpperCase()) + "~" + nickname + ChatColor.WHITE);
        message = formatMessage(message, player);
        return message;
    }
}
