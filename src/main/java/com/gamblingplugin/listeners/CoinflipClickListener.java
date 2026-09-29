package com.gamblingplugin.listeners;

import com.gamblingplugin.GamblingPlugin;
import com.gamblingplugin.gui.CoinflipGUI;
import com.gamblingplugin.structures.CoinflipStructure;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;

public class CoinflipClickListener implements Listener {

    private final GamblingPlugin plugin;

    public CoinflipClickListener(GamblingPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onCoinflipClick(PlayerInteractAtEntityEvent event) {
        if (!(event.getRightClicked() instanceof ArmorStand))
            return;

        ArmorStand stand = (ArmorStand) event.getRightClicked();
        Player player = event.getPlayer();

        // Check if this is a coinflip structure
        CoinflipStructure coinflip = plugin.getStructureManager().getCoinflipStructure(stand.getLocation());
        if (coinflip == null)
            return;

        event.setCancelled(true);

        // Check if already flipping
        if (coinflip.isFlipping()) {
            player.sendMessage("§c[VieGambling] La moneda ya está en el aire!");
            return;
        }

        // Open coinflip GUI
        CoinflipGUI gui = new CoinflipGUI(plugin);
        gui.open(player);
    }
}
