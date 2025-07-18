package main.java.org.matejko.utilis.Commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import main.java.org.matejko.utilis.Managers.NickManager;
import main.java.org.matejko.utilis.FileCreator.Messages;
import main.java.org.matejko.utilis.Managers.ColorUtil;
import java.util.Map;

public class RealNameCommand implements org.bukkit.command.CommandExecutor {
    private final NickManager nickManager;
    private final Messages messages;

    public RealNameCommand(NickManager nickManager, Messages messages) {
        this.nickManager = nickManager;
    	this.messages = messages;
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ColorUtil.translateColorCodes(messages.getMessage("commands-prefix") + ChatColor.RED + "Usage: /realname <nickname>"));
            return true;
        }
        String nicknameInput = "~" + args[0].toLowerCase();
        String realName = getRealNameFromNickname(nicknameInput);
        if (realName != null) {
            String nicknameColor = getNicknameColor(nicknameInput);
            String fullNickname = getFullNickname(nicknameInput);
            String coloredNickname = nicknameColor + fullNickname + ChatColor.WHITE;
            sender.sendMessage(ColorUtil.translateColorCodes(messages.getMessage("commands-prefix") + ChatColor.GOLD + "Real name of " + coloredNickname + ChatColor.GOLD + " is " + realName + "."));
        } else {
            sender.sendMessage(ColorUtil.translateColorCodes(messages.getMessage("commands-prefix") + ChatColor.RED + "No real name found for nickname containing " + nicknameInput + "."));
        }
        return true;
    }
    private String getRealNameFromNickname(String nickname) {
        for (Map.Entry<String, String[]> entry : nickManager.getPlayerData().entrySet()) {
            String[] data = entry.getValue();
            if (data[0].toLowerCase().contains(nickname.substring(1))) {
                return entry.getKey();
            }
        }
        return null;
    }
    private String getFullNickname(String nickname) {
        for (Map.Entry<String, String[]> entry : nickManager.getPlayerData().entrySet()) {
            String[] data = entry.getValue();
            if (data[0].toLowerCase().contains(nickname.substring(1))) {
                return data[0];
            }
        }
        return nickname;
    }
    private String getNicknameColor(String nickname) {
        for (Map.Entry<String, String[]> entry : nickManager.getPlayerData().entrySet()) {
            String[] data = entry.getValue();
            if (data[0].toLowerCase().contains(nickname.substring(1))) {
                String color = data[1];
                try {
                    // Try to convert the color to a ChatColor
                    return ChatColor.valueOf(color.toUpperCase()).toString();
                } catch (IllegalArgumentException e) {
                    return ChatColor.WHITE.toString();  // If the color is invalid, use white
                }
            }
        }
        return ChatColor.WHITE.toString();
    }
}
