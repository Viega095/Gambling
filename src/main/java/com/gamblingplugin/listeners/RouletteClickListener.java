package com.gamblingplugin.listeners;

import com.gamblingplugin.GamblingPlugin;
import com.gamblingplugin.structures.RouletteWheel;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;

public class RouletteClickListener implements Listener {

    private final GamblingPlugin plugin;

    public RouletteClickListener(GamblingPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent event) {
        if (!(event.getRightClicked() instanceof ArmorStand)) {
            return;
        }

        Player player = event.getPlayer();
        ArmorStand armorStand = (ArmorStand) event.getRightClicked();

        // Check if this ArmorStand belongs to a roulette wheel
        RouletteWheel wheel = plugin.getStructureManager().getNearestRouletteWheel(armorStand.getLocation());

        if (wheel != null) {
            // Check if the clicked ArmorStand is part of this wheel
            if (wheel.isPartOfWheel(armorStand)) {
                event.setCancelled(true); // Prevent default interaction

                // Open the betting GUI (first select amount, then selection)
                plugin.getBettingGUI().open(player, "roulette");

                player.sendMessage("§6[VieGambling] §7Selecciona el monto de tu apuesta");
            }
        }
    }
}
