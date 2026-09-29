package com.gamblingplugin.listeners;

import com.gamblingplugin.GamblingPlugin;
import com.gamblingplugin.structures.DiceStructure;
import com.gamblingplugin.structures.RouletteWheel;
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
        // Check if interacting with a roulette wheel part
        RouletteWheel wheel = plugin.getStructureManager().getRouletteWheel(event.getRightClicked().getLocation());
        if (wheel != null) {
            event.setCancelled(true);
            if (!wheel.isSpinning()) {
                plugin.getBettingGUI().open(event.getPlayer(), "roulette");
            }
            return;
        }

        // Check if interacting with a dice structure
        DiceStructure dice = plugin.getStructureManager().getDiceStructure(event.getRightClicked().getLocation());
        if (dice != null) {
            event.setCancelled(true);
            if (!dice.isRolling()) {
                plugin.getBettingGUI().open(event.getPlayer(), "dice");
            }
        }
    }
}
