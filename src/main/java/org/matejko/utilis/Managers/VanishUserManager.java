package main.java.org.matejko.utilis.Managers;

import org.bukkit.entity.Player;

public class VanishUserManager {
    private final Player player;
    private boolean isVanished;

    public VanishUserManager(Player player, boolean isVanished) {
        this.player = player;
        this.isVanished = isVanished;
    }
    public String getName() {
        return player.getName();
    }
    public boolean isOnline() {
        return player.isOnline();
    }
    public Player getPlayer() {
        return player;
    }
    public boolean isVanished() {
        return isVanished;
    }
    public void setVanished(boolean vanished) {
        this.isVanished = vanished;
    }
}
