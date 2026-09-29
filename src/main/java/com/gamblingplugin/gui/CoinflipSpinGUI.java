package com.gamblingplugin.gui;

import com.gamblingplugin.GamblingPlugin;
import com.gamblingplugin.games.Coinflip;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;

public class CoinflipSpinGUI {

    private final GamblingPlugin plugin;
    private final String TITLE = "§6Coinflip - Girando...";

    public CoinflipSpinGUI(GamblingPlugin plugin) {
        this.plugin = plugin;
    }

    public void open(Player player, Coinflip.Side chosenSide, Coinflip.Side result, double bet) {
        Inventory inv = Bukkit.createInventory(null, 54, TITLE); // Large chest

        // Start spinning animation
        new SpinAnimation(player, inv, chosenSide, result, bet).runTaskTimer(plugin, 0L, 3L);
    }

    private class SpinAnimation extends BukkitRunnable {
        private final Player player;
        private final Inventory inv;
        private final Coinflip.Side chosenSide;
        private final Coinflip.Side result;
        private final double bet;
        private int ticks = 0;
        private int position = 0;

        public SpinAnimation(Player player, Inventory inv, Coinflip.Side chosenSide, Coinflip.Side result, double bet) {
            this.player = player;
            this.inv = inv;
            this.chosenSide = chosenSide;
            this.result = result;
            this.bet = bet;
            player.openInventory(inv);
        }

        @Override
        public void run() {
            // Animation duration: ~60 ticks (3 seconds at 3 tick interval = ~20 updates)
            if (ticks >= 20) {
                this.cancel();
                showResult();
                return;
            }

            // Clear inventory
            inv.clear();

            // Fill background with alternating gray glass
            fillBackground();

            // Create the spinning row in middle
            int middleRow = 2; // Row index 2 (slots 18-26)

            // Slow down as we approach the end
            int speed = (ticks < 10) ? 1 : (ticks < 15) ? 1 : 2;

            for (int i = 0; i < speed; i++) {
                position++;
            }

            // Display items in spinning row
            for (int col = 0; col < 9; col++) {
                int slot = middleRow * 9 + col;
                boolean isHeads = ((position + col) % 2 == 0);
                inv.setItem(slot, isHeads ? createHeadsItem() : createTailsItem());
            }

            // Highlight center (slot 22 = row 2, col 4)
            inv.setItem(22 - 9, createPointerItem()); // Above
            inv.setItem(22 + 9, createPointerItem()); // Below

            // Play sound
            if (ticks % 2 == 0) {
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f + (ticks * 0.05f));
            }

            ticks++;
        }

        private void fillBackground() {
            ItemStack gray = createGlass(Material.GRAY_STAINED_GLASS_PANE);
            ItemStack lightGray = createGlass(Material.LIGHT_GRAY_STAINED_GLASS_PANE);

            for (int row = 0; row < 6; row++) {
                for (int col = 0; col < 9; col++) {
                    if (row == 2)
                        continue; // Skip middle row (spinning row)

                    int slot = row * 9 + col;
                    boolean isGray = (row + col) % 2 == 0;
                    inv.setItem(slot, isGray ? gray : lightGray);
                }
            }
        }

        private void showResult() {
            inv.clear();
            fillBackground();

            // Show result in middle
            int middleRow = 2;
            for (int col = 0; col < 9; col++) {
                int slot = middleRow * 9 + col;
                if (col == 4) {
                    // Center - show result with enchant glow
                    ItemStack resultItem = (result == Coinflip.Side.HEADS) ? createHeadsItemGlowing()
                            : createTailsItemGlowing();
                    inv.setItem(slot, resultItem);
                } else {
                    boolean isHeads = ((col % 2 == 0) == (result == Coinflip.Side.HEADS));
                    inv.setItem(slot, isHeads ? createHeadsItem() : createTailsItem());
                }
            }

            // Pointers
            inv.setItem(13, createPointerItem());
            inv.setItem(31, createPointerItem());

            // Determine win/loss
            boolean won = (chosenSide == result);

            // Play final sound
            if (won) {
                player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
            } else {
                player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 0.5f, 1f);
            }

            // Close after showing result
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                player.closeInventory();
                // Process result in Coinflip game
            }, 40L); // 2 seconds
        }
    }

    private ItemStack createHeadsItem() {
        ItemStack item = new ItemStack(Material.GOLD_INGOT);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§6§lCARA");
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createHeadsItemGlowing() {
        ItemStack item = new ItemStack(Material.GOLD_INGOT);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§6§l✦ CARA ✦");
        meta.setLore(Arrays.asList("§e§l¡RESULTADO!"));
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createTailsItem() {
        ItemStack item = new ItemStack(Material.IRON_INGOT);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§7§lCRUZ");
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createTailsItemGlowing() {
        ItemStack item = new ItemStack(Material.IRON_INGOT);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§7§l✦ CRUZ ✦");
        meta.setLore(Arrays.asList("§e§l¡RESULTADO!"));
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createPointerItem() {
        ItemStack item = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§a§l▼");
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createGlass(Material material) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(" ");
        item.setItemMeta(meta);
        return item;
    }
}
