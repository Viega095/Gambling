package com.gamblingplugin.listeners;

import com.gamblingplugin.GamblingPlugin;
import com.gamblingplugin.gui.BlackjackLobbyGUI;
import com.gamblingplugin.gui.CaseOpeningGUI;
import com.gamblingplugin.gui.CoinflipGUI;
import com.gamblingplugin.gui.DiceGUI;
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

        // 2. Mega Wheel of Fortune 3D
        WheelStructure wheel = plugin.getStructureManager().getWheelStructure(loc);
        if (wheel != null) {
            event.setCancelled(true);
            if (!wheel.isSpinning()) {
                plugin.getMegaWheelOfFortune().openWheelGUI(player);
            }
            return;
        }

        // 3. Slots 3D Cabinet
        Slots3DStructure slot = plugin.getStructureManager().getSlotsStructure(loc);
        if (slot != null) {
            event.setCancelled(true);
            if (!slot.isSpinning()) {
                plugin.getBettingGUI().open(player, "slots");
            }
            return;
        }

        // 4. Blackjack Table
        if (plugin.getStructureManager().getBlackjackStructure(loc) != null) {
            event.setCancelled(true);
            new BlackjackLobbyGUI(plugin).open(player);
            return;
        }

        // 5. Roulette Wheel
        RouletteWheel roulette = plugin.getStructureManager().getRouletteWheel(loc);
        if (roulette != null) {
            event.setCancelled(true);
            if (!roulette.isSpinning()) {
                plugin.getBettingGUI().open(player, "roulette");
            }
            return;
        }

        // 6. Dice Table 3D
        DiceStructure dice = plugin.getStructureManager().getDiceStructure(loc);
        if (dice != null) {
            event.setCancelled(true);
            if (!dice.isRolling()) {
                new DiceGUI(plugin).open(player);
            }
            return;
        }

        // 7. Coinflip Table 3D
        CoinflipStructure coinflip = plugin.getStructureManager().getCoinflipStructure(loc);
        if (coinflip != null) {
            event.setCancelled(true);
            if (!coinflip.isFlipping()) {
                new CoinflipGUI(plugin).open(player);
            }
            return;
        }

        // 8. Mines Field
        MinesStructure mines = plugin.getStructureManager().getMinesStructure(loc);
        if (mines != null) {
            event.setCancelled(true);
            plugin.getBettingGUI().open(player, "mines");
            return;
        }

        // 9. Case Opening Station
        CaseStructure cs = plugin.getStructureManager().getCaseStructure(loc);
        if (cs != null) {
            event.setCancelled(true);
            if (!cs.isOpening()) {
                new CaseOpeningGUI(plugin).open(player);
            }
            return;
        }

        // 10. Plinko Machine
        PlinkoStructure plinko = plugin.getStructureManager().getPlinkoStructure(loc);
        if (plinko != null) {
            event.setCancelled(true);
            plugin.getBettingGUI().open(player, "plinko");
            return;
        }

        // 11. Baccarat Table
        if (plugin.getStructureManager().getBaccaratStructure(loc) != null) {
            event.setCancelled(true);
            if (plugin.getBaccaratTable() != null) {
                plugin.getBaccaratTable().openGUI(player);
            }
            return;
        }

        // 12. Lottery Booth
        if (plugin.getStructureManager().getLotteryStructure(loc) != null) {
            event.setCancelled(true);
            player.performCommand("gamble lottery");
            return;
        }

        // 13. VIP Lounge Bar
        if (plugin.getStructureManager().getVIPLoungeStructure(loc) != null) {
            event.setCancelled(true);
            if (plugin.getCasinoVIPLounge() != null) {
                plugin.getCasinoVIPLounge().openLoungeMenu(player);
            }
            return;
        }
    }
}
