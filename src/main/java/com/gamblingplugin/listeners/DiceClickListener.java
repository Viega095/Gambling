package com.gamblingplugin.listeners;

import com.gamblingplugin.GamblingPlugin;
import com.gamblingplugin.gui.DiceGUI;
import com.gamblingplugin.structures.DiceStructure;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;

public class DiceClickListener implements Listener {

    private final GamblingPlugin plugin;

    public DiceClickListener(GamblingPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onDiceClick(PlayerInteractAtEntityEvent event) {
        if (!(event.getRightClicked() instanceof ArmorStand))
            return;

        ArmorStand stand = (ArmorStand) event.getRightClicked();
        Player player = event.getPlayer();

        // Check if this is a dice structure
        DiceStructure dice = plugin.getStructureManager().getDiceStructure(stand.getLocation());
        if (dice == null)
            return;

        event.setCancelled(true);

        // Check if already rolling
        if (dice.isRolling()) {
            player.sendMessage("§c[VieGambling] ¡El dado ya está rodando!");
            return;
        }

        // Open dice GUI
        DiceGUI gui = new DiceGUI(plugin);
        gui.open(player);
    }
}
