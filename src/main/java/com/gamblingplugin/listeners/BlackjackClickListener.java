package com.gamblingplugin.listeners;

import com.gamblingplugin.GamblingPlugin;
import com.gamblingplugin.structures.BlackjackStructure;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class BlackjackClickListener implements Listener {

    private final GamblingPlugin plugin;

    public BlackjackClickListener(GamblingPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.RIGHT_CLICK_AIR) {
            return;
        }

        Player player = event.getPlayer();
        BlackjackStructure structure = plugin.getStructureManager().getBlackjackStructure(player.getLocation());

        if (structure != null) {
            event.setCancelled(true);

            // Open Blackjack lobby GUI
            com.gamblingplugin.gui.BlackjackLobbyGUI lobby = new com.gamblingplugin.gui.BlackjackLobbyGUI(plugin);
            lobby.open(player);
        }
    }
}
