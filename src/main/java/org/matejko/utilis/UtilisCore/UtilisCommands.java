package main.java.org.matejko.utilis.UtilisCore;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import main.java.org.matejko.utilis.Utilis;
import main.java.org.matejko.utilis.Commands.*;
import main.java.org.matejko.utilis.FileCreator.*;
import main.java.org.matejko.utilis.Listeners.*;
import main.java.org.matejko.utilis.Managers.*;

public class UtilisCommands {
    private final Utilis plugin;
    private final Config config;
    private final NickManager nickManager;
    private final CooldownManager cooldownManager;
    private final Messages messages;
	private WhitelistCommand whitelistCommand;
	
    public UtilisCommands(Utilis plugin, Config config, NickManager nickManager, CooldownManager cooldownManager, Messages messages) {
        this.plugin = plugin;
        this.config = config;
        this.nickManager = nickManager;
        this.cooldownManager = cooldownManager;
        this.messages = messages;
        whitelistCommand = new WhitelistCommand(plugin, config, messages);
    }
    public void registerCommands() {
        // Register commands with permission checks
        if (config.isNickEnabled()) {
            registerCommandWithPermission("nickname", "utilis.nickname", new NicknameCommand(nickManager, cooldownManager, messages, config));
        }
        if (config.isRenameEnabled()) {
            registerCommandWithPermission("rename", "utilis.rename", new RenameCommand(nickManager, messages));
        }
        if (config.isColorEnabled()) {
            registerCommandWithPermission("color", "utilis.color", new ColorCommand(nickManager, cooldownManager, messages));
        }
        if (config.isNickResetEnabled()) {
            registerCommandWithPermission("nickreset", "utilis.nickreset", new NickResetCommand(nickManager, cooldownManager, messages));
        }
        if (config.isRealNameEnabled()) {
            registerCommandWithPermission("realname", "utilis.realname", new RealNameCommand(nickManager, messages));
        }
        if (config.isListEnabled()) {
            registerCommandWithPermission("list", "utilis.list", new ListCommand(plugin, config));
        }
        registerCommandWithPermission("utilisdebug", "utilis.debug", new UtilisDebugCommand(plugin));
        registerCommandWithPermission("sudo", "utilis.sudo", new SudoManager(messages));
        registerCommandWithPermission("suck", "utilis.suck", new SuckCommand(messages));
        if (config.isVanishEnabled()) {
            VanishCommand vanishCommand = new VanishCommand(plugin, config, messages);
            registerCommandWithPermission("vanish", "utilis.vanish", vanishCommand);
            registerCommandWithPermission("v", "utilis.vanish", vanishCommand);
        }
        // Register the RecoverCommand with the plugin and pass the RecoverManager
        RecoverManager recoverManager = new RecoverManager(plugin);
        plugin.getCommand("recover").setExecutor(new RecoverCommand(recoverManager, messages));
        // Register ISee Command
        ISeeManager iSeeManager = UtilisGetters.getISeeManager();
        ISeeInventoryListener iSeeInventoryListener = new ISeeInventoryListener(plugin, iSeeManager);
        ISeeArmorListener iSeeArmorListener = new ISeeArmorListener(plugin, iSeeManager);
        ISeeOfflineEditor iSeeOfflineEditor = UtilisGetters.getISeeOfflineEditor();
        plugin.getCommand("isee").setExecutor(new CommandExecutor() {
            @Override
            public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
                if (!(sender instanceof Player)) {
                    sender.sendMessage("[Utilis] Only players can use this command.");
                    return true;
                }
                Player player = (Player) sender;
                if (!player.hasPermission("utilis.isee")) {
                    player.sendMessage(ColorUtil.translateColorCodes(messages.getMessage("commands-prefix") + "§cYou do not have permission to use this command."));
                    return true;
                }
                new ISeeCommand(
                    iSeeManager,
                    iSeeInventoryListener,
                    iSeeArmorListener,
                    iSeeOfflineEditor,
                    recoverManager
                ).onCommand(sender, command, label, args);
                return true;
            }
        });
        // Register the /wl command
        plugin.getCommand("wl").setExecutor(new CommandExecutor() {
            @Override
            public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
                if (cmd.getName().equalsIgnoreCase("whitelist")) {
                    return whitelistCommand.onCommand(sender, cmd, label, args);
                }
                return false;
            }
        });
    }
    private void registerCommandWithPermission(String commandName, String permission, CommandExecutor executor) {
        plugin.getCommand(commandName).setExecutor(new CommandExecutor() {
            @Override
            public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    if (!player.hasPermission(permission)) {
                        player.sendMessage(ColorUtil.translateColorCodes(messages.getMessage("commands-prefix") + "§cYou do not have permission to use this command."));
                        return true;
                    }
                }
                return executor.onCommand(sender, command, label, args);
            }
        });
    }
}
