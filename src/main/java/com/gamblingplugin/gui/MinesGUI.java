package com.gamblingplugin.gui;

import com.gamblingplugin.GamblingPlugin;
import com.gamblingplugin.games.Mines;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.UUID;

public class MinesGUI implements Listener {

    private final GamblingPlugin plugin;
    private final String TITLE = "§c§lMINES";

    public MinesGUI(GamblingPlugin plugin) {
        this.plugin = plugin;
    }

    public void open(Player player, Mines.MinesSession session) {
        Inventory inv = Bukkit.createInventory(null, 54, TITLE);

        updateGUI(inv, session, false);
        player.openInventory(inv);
    }

    private void updateGUI(Inventory inv, Mines.MinesSession session, boolean gameEnded) {
        inv.clear();

        // Top info bar (slots 0-8)
        inv.setItem(0, createInfoItem(Material.GOLD_BLOCK, "§6Apuesta",
                "§7$" + String.format("%.2f", session.bet)));

        inv.setItem(1, createInfoItem(Material.TNT, "§cMinas", "§73 minas"));

        inv.setItem(2, createInfoItem(Material.GREEN_STAINED_GLASS, "§aReveladas",
                "§7" + session.revealedTiles.size() + "/22"));

        inv.setItem(3, createInfoItem(Material.DIAMOND, "§bMultiplicador",
                "§e" + String.format("%.2fx", session.currentMultiplier)));

        // Cash out button (slot 7)
        if (!session.revealedTiles.isEmpty() && !gameEnded) {
            double winnings = session.bet * session.currentMultiplier;
            ItemStack cashOut = new ItemStack(Material.GOLD_BLOCK);
            ItemMeta meta = cashOut.getItemMeta();
            meta.setDisplayName("§a§lCASH OUT");
            meta.setLore(Arrays.asList(
                    "§7Ganancia: §a$" + String.format("%.2f", winnings),
                    "",
                    "§eClick para cobrar!"));
            cashOut.setItemMeta(meta);
            inv.setItem(7, cashOut);
        }

        // Exit button (slot 8)
        ItemStack exit = new ItemStack(Material.BARRIER);
        ItemMeta exitMeta = exit.getItemMeta();
        exitMeta.setDisplayName("§cCerrar");
        exit.setItemMeta(exitMeta);
        inv.setItem(8, exit);

        // 5x5 Grid (slots 18-22, 27-31, 36-40, 45-49, 9-13 bottom + decoration)
        // Actually using slots in a clean 5x5 pattern
        int[] gridSlots = {
                10, 11, 12, 13, 14, // Row 1
                19, 20, 21, 22, 23, // Row 2
                28, 29, 30, 31, 32, // Row 3
                37, 38, 39, 40, 41, // Row 4
                46, 47, 48, 49, 50 // Row 5
        };

        for (int i = 0; i < 25; i++) {
            int slot = gridSlots[i];

            if (session.revealedTiles.contains(i)) {
                // Revealed tile
                if (session.minePositions.contains(i)) {
                    // Mine (only shown if game ended)
                    if (gameEnded) {
                        inv.setItem(slot, createTile(Material.TNT, "§c§lMINA", i));
                    }
                } else {
                    // Safe tile
                    inv.setItem(slot, createTile(Material.LIME_CONCRETE, "§a§lSEGURO", i));
                }
            } else {
                // Unrevealed tile
                if (gameEnded && session.minePositions.contains(i)) {
                    // Show remaining mines
                    inv.setItem(slot, createTile(Material.TNT, "§c§lMINA", i));
                } else {
                    inv.setItem(slot, createTile(Material.GRAY_CONCRETE, "§7???", i));
                }
            }
        }

        // Decoration (fill empty slots with glass)
        ItemStack glass = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta glassMeta = glass.getItemMeta();
        glassMeta.setDisplayName(" ");
        glass.setItemMeta(glassMeta);

        for (int i = 0; i < 54; i++) {
            if (inv.getItem(i) == null) {
                inv.setItem(i, glass);
            }
        }
    }

    private ItemStack createInfoItem(Material material, String name, String value) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(Arrays.asList(value));
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createTile(Material material, String name, int position) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        if (!name.equals("§7???")) {
            meta.setLore(Arrays.asList("§8Pos: " + position));
        } else {
            meta.setLore(Arrays.asList("§eClick para revelar"));
        }
        item.setItemMeta(meta);
        return item;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals(TITLE))
            return;
        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player))
            return;
        Player player = (Player) event.getWhoClicked();

        if (event.getCurrentItem() == null)
            return;

        Mines.MinesSession session = plugin.getGameManager().getMines().getSession(player.getUniqueId());
        if (session == null) {
            player.closeInventory();
            return;
        }

        int slot = event.getSlot();

        // Cash out button (slot 7)
        if (slot == 7 && event.getCurrentItem().getType() == Material.GOLD_BLOCK) {
            plugin.getGameManager().getMines().cashOut(player);
            player.closeInventory();
            return;
        }

        // Exit button (slot 8)
        if (slot == 8) {
            plugin.getGameManager().getMines().endSession(player.getUniqueId());
            player.closeInventory();
            return;
        }

        // Grid slots mapping
        int[] gridSlots = {
                10, 11, 12, 13, 14,
                19, 20, 21, 22, 23,
                28, 29, 30, 31, 32,
                37, 38, 39, 40, 41,
                46, 47, 48, 49, 50
        };

        // Find grid position
        int position = -1;
        for (int i = 0; i < gridSlots.length; i++) {
            if (gridSlots[i] == slot) {
                position = i;
                break;
            }
        }

        if (position == -1)
            return;

        // Only allow clicking unrevealed tiles
        if (session.revealedTiles.contains(position)) {
            return;
        }

        // Reveal tile
        plugin.getGameManager().getMines().revealTile(player, position);

        // Check if game ended (hit mine)
        boolean hitMine = session.minePositions.contains(position) && session.revealedTiles.contains(position);
        boolean won = session.revealedTiles.size() == 22 && !hitMine;

        if (hitMine || won) {
            // Reveal all mines
            updateGUI(event.getInventory(), session, true);
            // Close after 3 seconds
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                player.closeInventory();
            }, 60L);
        } else {
            // Update GUI
            updateGUI(event.getInventory(), session, false);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!event.getView().getTitle().equals(TITLE))
            return;

        if (event.getPlayer() instanceof Player) {
            Player player = (Player) event.getPlayer();
            UUID playerId = player.getUniqueId();

            // If player closes GUI with active session, refund bet
            Mines.MinesSession session = plugin.getGameManager().getMines().getSession(playerId);
            if (session != null && session.revealedTiles.isEmpty()) {
                // Refund if no tiles revealed
                plugin.getEconomyManager().deposit(player, session.bet);
                player.sendMessage("§e[VieGambling] Apuesta reembolsada (no revelaste casillas)");
                plugin.getGameManager().getMines().endSession(playerId);
            }
        }
    }
}
