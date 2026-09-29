package com.gamblingplugin.games;

import com.gamblingplugin.GamblingPlugin;
import org.bukkit.entity.Player;

public abstract class Game {

    protected final GamblingPlugin plugin;
    private final String name;

    public Game(GamblingPlugin plugin, String name) {
        this.plugin = plugin;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public boolean isEnabled() {
        return plugin.getConfigManager().getConfig().getBoolean("games." + name + ".enabled", true);
    }

    public double getMinBet() {
        return plugin.getConfigManager().getMinBet(name);
    }

    public double getMaxBet() {
        return plugin.getConfigManager().getMaxBet(name);
    }

    public abstract void play(Player player, double bet, String[] args);

    protected void sendWinMessage(Player player, double amount) {
        plugin.getStatsManager().addWin(player, amount);
        String msg = plugin.getConfigManager().getMessage("win")
                .replace("%amount%",
                        plugin.getConfigManager().getConfig().getString("economy.currency-symbol") + amount);
        player.sendMessage(msg);
    }

    protected void sendLossMessage(Player player) {
        plugin.getStatsManager().addLoss(player, 0);
        player.sendMessage(plugin.getConfigManager().getMessage("loss"));
    }
}
