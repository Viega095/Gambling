package com.gamblingplugin.listeners;

import com.gamblingplugin.GamblingPlugin;
import com.gamblingplugin.structures.CaseStructure;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;

public class CaseClickListener implements Listener {

    private final GamblingPlugin plugin;

    public CaseClickListener(GamblingPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEntityInteract(PlayerInteractAtEntityEvent event) {
        if (!(event.getRightClicked() instanceof ArmorStand)) {
            return;
        }

        ArmorStand stand = (ArmorStand) event.getRightClicked();
        Player player = event.getPlayer();

        // Check if it's a case structure
        CaseStructure structure = plugin.getStructureManager().getCaseStructure(stand.getLocation());
        if (structure == null || !stand.equals(structure.getDisplayStand())) {
            return;
        }

        event.setCancelled(true);

        // Open case selection GUI
        com.gamblingplugin.gui.CaseOpeningGUI gui = new com.gamblingplugin.gui.CaseOpeningGUI(plugin);
        gui.open(player);
    }
}
