package main.java.org.matejko.plugin.UtilisCore;

import main.java.org.matejko.plugin.Utilis;
import main.java.org.matejko.plugin.Commands.*;
import main.java.org.matejko.plugin.FileCreator.*;
import main.java.org.matejko.plugin.Listeners.*;
import main.java.org.matejko.plugin.Managers.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class UtilisCommands {
    private final Utilis plugin;
    private final Config config;
    private final NickManager nickManager;
    private final CooldownManager cooldownManager;
    private final Messages messages;
    
    public UtilisCommands(Utilis plugin, Config config, NickManager nickManager, CooldownManager cooldownManager, Messages messages) {
        this.plugin = plugin;
        this.config = config;
        this.nickManager = nickManager;
        this.cooldownManager = cooldownManager;
        this.messages = messages;
    }

    public void registerCommands() {
        // Register commands with permission checks
        if (config.isNickEnabled()) {
            registerCommandWithPermission("nickname", "utilis.nickname", new NicknameCommand(nickManager, cooldownManager, messages, config));
        }
        if (config.isRenameEnabled()) {
            registerCommandWithPermission("rename", "utilis.rename", new RenameCommand(nickManager));
        }
        if (config.isColorEnabled()) {
            registerCommandWithPermission("color", "utilis.color", new ColorCommand(nickManager, cooldownManager, messages));
        }
        if (config.isNickResetEnabled()) {
            registerCommandWithPermission("nickreset", "utilis.nickreset", new NickResetCommand(nickManager, cooldownManager));
        }
        if (config.isRealNameEnabled()) {
            registerCommandWithPermission("realname", "utilis.realname", new RealNameCommand(nickManager));
        }
        if (config.isListEnabled()) {
            registerCommandWithPermission("list", "utilis.list", new ListCommand(plugin));
        }
        registerCommandWithPermission("utilisdebug", "utilis.debug", new UtilisDebugCommand(plugin));
        registerCommandWithPermission("sudo", "utilis.sudo", new SudoManager());
        registerCommandWithPermission("suck", "utilis.suck", new SuckCommand());
        if (config.isVanishEnabled()) {
            VanishCommand vanishCommand = new VanishCommand(plugin);
            registerCommandWithPermission("vanish", "utilis.vanish", vanishCommand);
            registerCommandWithPermission("v", "utilis.vanish", vanishCommand);
        }
        // Register the RecoverCommand with the plugin and pass the RecoverManager
        RecoverManager recoverManager = new RecoverManager();
        plugin.getCommand("recover").setExecutor(new RecoverCommand(recoverManager));
        // Register ISee Command
        ISeeManager iSeeManager = UtilisGetters.getISeeManager();
        ISeeInventoryListener iSeeInventoryListener = new ISeeInventoryListener(plugin, iSeeManager);
        ISeeArmorListener iSeeArmorListener = new ISeeArmorListener(plugin, iSeeManager);
        plugin.getCommand("isee").setExecutor(new CommandExecutor() {
            @Override
            public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
                if (!(sender instanceof Player)) {
                    sender.sendMessage("Only players can use this command.");
                    return true;
                }
                Player player = (Player) sender;
                if (!player.hasPermission("utilis.isee")) {
                    player.sendMessage("§cYou do not have permission to use this command.");
                    return true;
                }
                new ISeeCommand(iSeeManager, iSeeInventoryListener, iSeeArmorListener, plugin)
                        .onCommand(sender, command, label, args);
                return true;
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
                        player.sendMessage("§cYou do not have permission to use this command.");
                        return true;
                    }
                }
                return executor.onCommand(sender, command, label, args);
            }
        });
    }
}
