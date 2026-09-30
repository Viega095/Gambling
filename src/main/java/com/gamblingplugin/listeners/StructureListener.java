package com.gamblingplugin.listeners;

import com.gamblingplugin.GamblingPlugin;
import com.gamblingplugin.structures.DiceStructure;
import com.gamblingplugin.structures.RouletteWheel;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;

public class StructureListener implements Listener {

    private final GamblingPlugin plugin;

    public StructureListener(GamblingPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEntityInteract(PlayerInteractAtEntityEvent event) {
        Location loc = event.getRightClicked().getLocation();

        // Check roulette
        RouletteWheel wheel = plugin.getStructureManager().getRouletteWheel(loc);
        if (wheel != null) {
            event.setCancelled(true);
            if (!wheel.isSpinning()) {
                plugin.getBettingGUI().open(event.getPlayer(), "roulette");
            }
            return;
        }

        // Check dice
        DiceStructure dice = plugin.getStructureManager().getDiceStructure(loc);
        if (dice != null) {
            event.setCancelled(true);
            if (!dice.isRolling()) {
                plugin.getBettingGUI().open(event.getPlayer(), "dice");
            }
            return;
        }

        // Check Crash 3D Rocket Launchpad
        if (plugin.getStructureManager().getCrashStructure(loc) != null) {
            event.setCancelled(true);
            if (plugin.getCrashBettingAndCashoutGUI() != null) {
                plugin.getCrashBettingAndCashoutGUI().openBettingGUI(event.getPlayer());
            }
            return;
        }

        // Check Baccarat Table
        if (plugin.getStructureManager().getBaccaratStructure(loc) != null) {
            event.setCancelled(true);
            if (plugin.getBaccaratTable() != null) {
                plugin.getBaccaratTable().openGUI(event.getPlayer());
            }
            return;
        }

        // Check Lottery Booth
        if (plugin.getStructureManager().getLotteryStructure(loc) != null) {
            event.setCancelled(true);
            event.getPlayer().performCommand("gamble lottery");
            return;
        }

        // Check VIP Lounge Bar
        if (plugin.getStructureManager().getVIPLoungeStructure(loc) != null) {
            event.setCancelled(true);
            if (plugin.getCasinoVIPLounge() != null) {
                plugin.getCasinoVIPLounge().openLoungeMenu(event.getPlayer());
            }
            return;
        }
    }
}
