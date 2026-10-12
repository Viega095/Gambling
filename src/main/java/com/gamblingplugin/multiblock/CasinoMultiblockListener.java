package com.gamblingplugin.multiblock;

import com.gamblingplugin.GamblingPlugin;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.inventory.ItemStack;

public class CasinoMultiblockListener implements Listener {

    private final GamblingPlugin plugin;
    private final CasinoMultiblockManager manager;

    public CasinoMultiblockListener(GamblingPlugin plugin, CasinoMultiblockManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        ItemStack item = event.getItemInHand();
        CasinoMultiblockType type = manager.getTypeFromItem(item);
        if (type == null) return;

        Player player = event.getPlayer();
        Block placedBlock = event.getBlockPlaced();

        boolean placed = manager.placeMultiblock(player, placedBlock.getLocation(), type);
        if (!placed) {
            event.setCancelled(true);
            return;
        }

        // Successfully deployed: if survival, consume item from hand
        if (player.getGameMode() != GameMode.CREATIVE) {
            ItemStack inHand = item.clone();
            inHand.setAmount(inHand.getAmount() - 1);
            player.getInventory().setItem(event.getHand(), inHand);
        }
        event.setCancelled(true); // Cancel vanilla single-block placement so multiblock's atomic placement is authoritative
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (manager.isMultiblockBlock(block.getLocation())) {
            // Strictly prohibit natural drops of constituent building blocks
            event.setDropItems(false);
            event.setExpToDrop(0);
            block.setType(Material.AIR, false);

            // Execute clean 100% dismantle and refund 1 original PDC item
            manager.dismantleAt(block.getLocation(), event.getPlayer());

            // Ensure event is not cancelled so broken block is definitely cleared
            event.setCancelled(false);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPistonExtend(BlockPistonExtendEvent event) {
        for (Block b : event.getBlocks()) {
            if (manager.isMultiblockBlock(b.getLocation())) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPistonRetract(BlockPistonRetractEvent event) {
        for (Block b : event.getBlocks()) {
            if (manager.isMultiblockBlock(b.getLocation())) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        event.blockList().removeIf(b -> manager.isMultiblockBlock(b.getLocation()));
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent event) {
        event.blockList().removeIf(b -> manager.isMultiblockBlock(b.getLocation()));
    }
}
