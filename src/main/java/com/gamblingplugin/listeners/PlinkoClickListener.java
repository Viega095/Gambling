package com.gamblingplugin.listeners;

import com.gamblingplugin.GamblingPlugin;
import com.gamblingplugin.structures.PlinkoStructure;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

public class PlinkoClickListener implements Listener {

    private final GamblingPlugin plugin;

    public PlinkoClickListener(GamblingPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockClick(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) {
            return;
        }

        Player player = event.getPlayer();

        // Check if player clicked a Plinko structure block
        PlinkoStructure structure = plugin.getStructureManager()
                .getPlinkoStructure(event.getClickedBlock().getLocation());

        if (structure == null) {
            return;
        }

        event.setCancelled(true);

        // Open Plinko GUI
        com.gamblingplugin.gui.PlinkoGUI gui = new com.gamblingplugin.gui.PlinkoGUI(plugin);
        gui.open(player);
    }
}
