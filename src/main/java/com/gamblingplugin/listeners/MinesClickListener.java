package com.gamblingplugin.listeners;

import com.gamblingplugin.GamblingPlugin;
import com.gamblingplugin.structures.MinesStructure;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;

public class MinesClickListener implements Listener {

    private final GamblingPlugin plugin;

    public MinesClickListener(GamblingPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEntityInteract(PlayerInteractAtEntityEvent event) {
        if (!(event.getRightClicked() instanceof ArmorStand)) {
            return;
        }

        ArmorStand stand = (ArmorStand) event.getRightClicked();
        Player player = event.getPlayer();

        // Check if it's a mines structure (use stand location, not player!)
        MinesStructure structure = plugin.getStructureManager().getMinesStructure(stand.getLocation());
        if (structure == null || !stand.equals(structure.getDisplayStand())) {
            return;
        }

        event.setCancelled(true);

        // Open betting GUI
        plugin.getBettingGUI().open(player, "mines");
    }
}
