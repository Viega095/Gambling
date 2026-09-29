package com.gamblingplugin.gui;

import com.gamblingplugin.GamblingPlugin;
import com.gamblingplugin.games.CaseOpening;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class CaseAnimationGUI {

    private final GamblingPlugin plugin;
    private final Random random = new Random();

    public CaseAnimationGUI(GamblingPlugin plugin) {
        this.plugin = plugin;
    }

    public void open(Player player, CaseOpening.CaseType caseType, CaseOpening.Rarity winningRarity, double bet,
            double winnings) {
        Inventory inv = Bukkit.createInventory(null, 27, "§6§lOPENING " + caseType.name() + " CASE...");

        // Create item list for animation (30 items)
        List<ItemStack> items = new ArrayList<>();

        // Generate random items with rarities
        for (int i = 0; i < 30; i++) {
            CaseOpening.Rarity rarity = (i == 25) ? winningRarity : getRandomRarity();
            items.add(createRarityItem(rarity));
        }

        // Show initial items
        for (int i = 0; i < 9; i++) {
            inv.setItem(9 + i, items.get(i));
        }

        player.openInventory(inv);

        // Animate scrolling
        new BukkitRunnable() {
            int offset = 0;
            int ticks = 0;
            int maxTicks = 80; // 4 seconds

            @Override
            public void run() {
                if (!player.getOpenInventory().getTitle().contains("OPENING")) {
                    this.cancel();
                    return;
                }

                ticks++;

                // Slow down over time
                int speed = Math.max(1, 10 - (ticks / 8));

                if (ticks % speed == 0) {
                    offset++;

                    // Update display
                    for (int i = 0; i < 9; i++) {
                        int index = (offset + i) % items.size();
                        inv.setItem(9 + i, items.get(index));
                    }

                    // Play tick sound
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 0.5f, 1.0f + (ticks * 0.01f));
                }

                // Stop at winning item
                if (ticks >= maxTicks) {
                    this.cancel();

                    // Ensure winning item is in center (slot 13)
                    inv.setItem(13, createRarityItem(winningRarity));

                    // Highlight winner
                    highlightWinner(inv, winningRarity);

                    // Play win sound
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);

                    // Close after 3 seconds
                    Bukkit.getScheduler().runTaskLater(plugin, player::closeInventory, 60L);
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private void highlightWinner(Inventory inv, CaseOpening.Rarity rarity) {
        // Clear other slots
        for (int i = 0; i < 9; i++) {
            inv.setItem(i, null);
        }
        for (int i = 18; i < 27; i++) {
            inv.setItem(i, null);
        }

        // Add border around winner
        ItemStack border = new ItemStack(Material.YELLOW_STAINED_GLASS_PANE);
        ItemMeta meta = border.getItemMeta();
        meta.setDisplayName("§e§l★ GANASTE ★");
        border.setItemMeta(meta);

        inv.setItem(3, border);
        inv.setItem(4, border);
        inv.setItem(5, border);
        inv.setItem(12, border);
        inv.setItem(14, border);
        inv.setItem(21, border);
        inv.setItem(22, border);
        inv.setItem(23, border);
    }

    private CaseOpening.Rarity getRandomRarity() {
        double roll = random.nextDouble() * 100.0;
        double cumulative = 0.0;

        for (CaseOpening.Rarity rarity : CaseOpening.Rarity.values()) {
            cumulative += rarity.getProbability();
            if (roll < cumulative) {
                return rarity;
            }
        }

        return CaseOpening.Rarity.COMMON;
    }

    private ItemStack createRarityItem(CaseOpening.Rarity rarity) {
        Material material;
        switch (rarity) {
            case COMMON:
                material = Material.COAL;
                break;
            case UNCOMMON:
                material = Material.IRON_INGOT;
                break;
            case RARE:
                material = Material.DIAMOND;
                break;
            case EPIC:
                material = Material.EMERALD;
                break;
            case LEGENDARY:
                material = Material.NETHER_STAR;
                break;
            default:
                material = Material.COAL;
        }

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(rarity.getColor() + rarity.name());
        meta.setLore(Arrays.asList(
                "",
                "§7Multiplicador: §e" + String.format("%.1f", rarity.getMinMultiplier()) + "x-"
                        + String.format("%.1f", rarity.getMaxMultiplier()) + "x"));
        item.setItemMeta(meta);

        return item;
    }
}
