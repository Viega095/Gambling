package com.gamblingplugin.listeners;

import com.gamblingplugin.GamblingPlugin;
import com.gamblingplugin.gui.CaseOpeningGUI;
import com.gamblingplugin.structures.*;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

public class StructureListener implements Listener {

    private final GamblingPlugin plugin;

    public StructureListener(GamblingPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityInteractAt(PlayerInteractAtEntityEvent event) {
        if (event.getHand() == EquipmentSlot.HAND) {
            handleInteraction(event.getPlayer(), event.getRightClicked().getLocation(), event);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityInteract(PlayerInteractEntityEvent event) {
        if (event.getHand() == EquipmentSlot.HAND) {
            handleInteraction(event.getPlayer(), event.getRightClicked().getLocation(), event);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockInteract(PlayerInteractEvent event) {
        if (event.getHand() == EquipmentSlot.HAND && (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR)) {
            if (event.getClickedBlock() != null) {
                Location blockLoc = event.getClickedBlock().getLocation();
                handleInteraction(event.getPlayer(), blockLoc, event);
            }
        }
    }

    private void handleInteraction(Player player, Location loc, org.bukkit.event.Cancellable event) {
        if (player == null || loc == null) return;

        // 1. Crash 3D Rocket Launchpad
        if (plugin.getStructureManager().getCrashStructure(loc) != null) {
            event.setCancelled(true);
            if (plugin.getCrashBettingAndCashoutGUI() != null) {
                plugin.getCrashBettingAndCashoutGUI().openBettingGUI(player);
            }
            return;
        }

        // 2. Blackjack Table
        if (plugin.getStructureManager().getBlackjackStructure(loc) != null) {
            event.setCancelled(true);
            new com.gamblingplugin.gui.BlackjackLobbyGUI(plugin).open(player);
            return;
        }

        // 3. Roulette Wheel
        RouletteWheel wheel = plugin.getStructureManager().getRouletteWheel(loc);
        if (wheel != null) {
            event.setCancelled(true);
            if (!wheel.isSpinning()) {
                plugin.getBettingGUI().open(player, "roulette");
            }
            return;
        }

        // 4. Dice Table
        DiceStructure dice = plugin.getStructureManager().getDiceStructure(loc);
        if (dice != null) {
            event.setCancelled(true);
            if (!dice.isRolling()) {
                plugin.getBettingGUI().open(player, "dice");
            }
            return;
        }

        // 5. Coinflip Table
        CoinflipStructure coinflip = plugin.getStructureManager().getCoinflipStructure(loc);
        if (coinflip != null) {
            event.setCancelled(true);
            plugin.getBettingGUI().open(player, "coinflip");
            return;
        }

        // 6. Mines Field
        MinesStructure mines = plugin.getStructureManager().getMinesStructure(loc);
        if (mines != null) {
            event.setCancelled(true);
            plugin.getBettingGUI().open(player, "mines");
            return;
        }

        // 7. Case Opening Station
        CaseStructure cs = plugin.getStructureManager().getCaseStructure(loc);
        if (cs != null) {
            event.setCancelled(true);
            new CaseOpeningGUI(plugin).open(player);
            return;
        }

        // 8. Plinko Machine
        PlinkoStructure plinko = plugin.getStructureManager().getPlinkoStructure(loc);
        if (plinko != null) {
            event.setCancelled(true);
            plugin.getBettingGUI().open(player, "plinko");
            return;
        }

        // 9. Baccarat Table
        if (plugin.getStructureManager().getBaccaratStructure(loc) != null) {
            event.setCancelled(true);
            if (plugin.getBaccaratTable() != null) {
                plugin.getBaccaratTable().openGUI(player);
            }
            return;
        }

        // 10. Lottery Booth
        if (plugin.getStructureManager().getLotteryStructure(loc) != null) {
            event.setCancelled(true);
            player.performCommand("gamble lottery");
            return;
        }

        // 11. VIP Lounge Bar
        if (plugin.getStructureManager().getVIPLoungeStructure(loc) != null) {
            event.setCancelled(true);
            if (plugin.getCasinoVIPLounge() != null) {
                plugin.getCasinoVIPLounge().openLoungeMenu(player);
            }
            return;
        }
    }
}
