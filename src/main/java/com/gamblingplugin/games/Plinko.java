package com.gamblingplugin.games;

import com.gamblingplugin.GamblingPlugin;
import org.bukkit.entity.Player;

public class Plinko extends Game {

    public Plinko(GamblingPlugin plugin) {
        super(plugin, "Plinko");
    }

    @Override
    public void play(Player player, double bet, String[] args) {
        // Plinko is now fully GUI-based
        // This method is kept for compatibility but doesn't do anything
        // The game is accessed through PlinkoClickListener → PlinkoGUI
        player.sendMessage("§cUsa el Plinko clickeando la estructura!");
    }

    @Override
    public double getMinBet() {
        return 100;
    }

    @Override
    public double getMaxBet() {
        return 10000;
    }
}
