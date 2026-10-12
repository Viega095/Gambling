package com.gamblingplugin.gui;

import com.gamblingplugin.GamblingPlugin;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class ScratchCardGUI implements Listener {

    public static class ScratchHolder implements InventoryHolder {
        @Override
        public Inventory getInventory() {
            return null;
        }
    }

    public static class ActiveCard {
        public final UUID playerId;
        public final double cost;
        public final Material[] hiddenGrid = new Material[9];
        public final boolean[] revealed = new boolean[9];
        public boolean finalized = false;

        public ActiveCard(UUID playerId, double cost) {
            this.playerId = playerId;
            this.cost = cost;
            generateGrid();
        }

        private void generateGrid() {
            Material[] pool = {
                    Material.NETHER_STAR, Material.DIAMOND, Material.EMERALD,
                    Material.GOLD_INGOT, Material.AMETHYST_SHARD, Material.IRON_INGOT,
                    Material.COAL, Material.COPPER_INGOT
            };

            // Chance for 3 matching winning symbols
            double r = Math.random();
            Material winSymbol = null;
            if (r < 0.05) winSymbol = Material.NETHER_STAR; // 100x
            else if (r < 0.15) winSymbol = Material.DIAMOND; // 50x
            else if (r < 0.30) winSymbol = Material.EMERALD; // 25x
            else if (r < 0.45) winSymbol = Material.GOLD_INGOT; // 10x
            else if (r < 0.60) winSymbol = Material.AMETHYST_SHARD; // 5x
            else if (r < 0.75) winSymbol = Material.IRON_INGOT; // 2x

            List<Integer> slots = new ArrayList<>(Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8));
            Collections.shuffle(slots);

            if (winSymbol != null) {
                hiddenGrid[slots.get(0)] = winSymbol;
                hiddenGrid[slots.get(1)] = winSymbol;
                hiddenGrid[slots.get(2)] = winSymbol;
                for (int i = 3; i < 9; i++) {
                    hiddenGrid[slots.get(i)] = pool[ThreadLocalRandom.current().nextInt(pool.length)];
                }
            } else {
                for (int i = 0; i < 9; i++) {
                    hiddenGrid[i] = pool[ThreadLocalRandom.current().nextInt(pool.length)];
                }
            }
        }
    }

    private final GamblingPlugin plugin;
    private final Map<UUID, ActiveCard> activeCards = new HashMap<>();

    public ScratchCardGUI(GamblingPlugin plugin) {
        this.plugin = plugin;
    }

    public void openNewCard(Player player, double cost) {
        if (!plugin.getEconomyManager().has(player, cost)) {
            player.sendMessage("§c✖ Fondos insuficientes para comprar el Raspe de $" + (int)cost);
            return;
        }

        plugin.getEconomyManager().withdraw(player, cost);
        ActiveCard card = new ActiveCard(player.getUniqueId(), cost);
        activeCards.put(player.getUniqueId(), card);

        renderGUI(player, card);
        player.playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_GENERIC, 1f, 1.2f);
    }

    public void renderGUI(Player player, ActiveCard card) {
        Inventory inv = Bukkit.createInventory(new ScratchHolder(), 54, "§8🎟️ §6Raspe de la Fortuna §8- §a$" + (int) card.cost);

        // Border
        ItemStack border = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta bMeta = border.getItemMeta();
        if (bMeta != null) {
            bMeta.setDisplayName(" ");
            border.setItemMeta(bMeta);
        }
        for (int i = 0; i < 54; i++) inv.setItem(i, border);

        // Header Info (Slot 4)
        inv.setItem(4, createBtn(Material.GOLD_BLOCK, "§e§lTABLA DE PREMIOS DEL RASPE", Arrays.asList(
                "§d⭐ 3x Nether Star: §a100x ($" + (int)(card.cost * 100) + ")",
                "§b💎 3x Diamante: §a50x ($" + (int)(card.cost * 50) + ")",
                "§a💚 3x Esmeralda: §a25x ($" + (int)(card.cost * 25) + ")",
                "§e👑 3x Oro: §a10x ($" + (int)(card.cost * 10) + ")",
                "§5🔮 3x Amatista: §a5x ($" + (int)(card.cost * 5) + ")",
                "§f✨ 3x Hierro: §a2x ($" + (int)(card.cost * 2) + ")",
                "",
                "§7¡Haz clic en las 9 casillas para raspar la capa plateada!"
        )));

        // 3x3 Scratch Grid: Slots 20, 21, 22, 29, 30, 31, 38, 39, 40
        int[] gridSlots = {20, 21, 22, 29, 30, 31, 38, 39, 40};
        for (int i = 0; i < 9; i++) {
            if (card.revealed[i]) {
                inv.setItem(gridSlots[i], createRevealedItem(card.hiddenGrid[i]));
            } else {
                inv.setItem(gridSlots[i], createBtn(Material.LIGHT_GRAY_STAINED_GLASS_PANE, "§f§l[ RASPAR CASILLA " + (i + 1) + " ]", Arrays.asList(
                        "§7Capa protectora de plata.",
                        "§e▶ Haz clic con una moneda para raspar"
                )));
            }
        }

        // Buy new cards shortcuts
        inv.setItem(45, createBtn(Material.NETHER_WART, "§c🔥 Tesoros del Nether ($2,500)", Arrays.asList("§7Premio máx: $250,000", "§e▶ Clic para comprar")));
        inv.setItem(46, createBtn(Material.IRON_NUGGET, "§f🎟️ Raspe Plata ($100)", Arrays.asList("§7Premio máx: $10,000", "§e▶ Clic para comprar")));
        inv.setItem(48, createBtn(Material.GOLD_NUGGET, "§e🎟️ Raspe Oro ($500)", Arrays.asList("§7Premio máx: $50,000", "§e▶ Clic para comprar")));
        inv.setItem(50, createBtn(Material.DIAMOND, "§b🎟️ Raspe Diamante ($1,000)", Arrays.asList("§7Premio máx: $100,000", "§e▶ Clic para comprar")));
        inv.setItem(51, createBtn(Material.NETHER_STAR, "§6⚡ Olimpo Dorado ($5,000)", Arrays.asList("§7Premio máx: $500,000", "§e▶ Clic para comprar")));

        player.openInventory(inv);
    }

    private ItemStack createRevealedItem(Material mat) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§a✦ " + mat.name() + " ✦");
            meta.setLore(Arrays.asList("§7Símbolo revelado"));
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createBtn(Material mat, String name, List<String> lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getView().getTopInventory().getHolder() instanceof ScratchHolder)) return;
        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) return;
        ActiveCard card = activeCards.get(player.getUniqueId());

        int slot = event.getRawSlot();
        if (slot == 45) { openNewCard(player, 2500.0); return; }
        if (slot == 46) { openNewCard(player, 100.0); return; }
        if (slot == 48) { openNewCard(player, 500.0); return; }
        if (slot == 50) { openNewCard(player, 1000.0); return; }
        if (slot == 51) { openNewCard(player, 5000.0); return; }

        if (card == null || card.finalized) return;

        int[] gridSlots = {20, 21, 22, 29, 30, 31, 38, 39, 40};
        for (int i = 0; i < 9; i++) {
            if (gridSlots[i] == slot && !card.revealed[i]) {
                card.revealed[i] = true;
                player.playSound(player.getLocation(), Sound.BLOCK_SAND_HIT, 1f, 1.6f);
                player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 0.7f, 1.8f);
                player.spawnParticle(Particle.CRIT, player.getLocation().add(0, 1, 0), 10, 0.2, 0.2, 0.2, 0.05);

                renderGUI(player, card);
                checkWinningCondition(player, card);
                return;
            }
        }
    }

    private void checkWinningCondition(Player player, ActiveCard card) {
        // Count symbols
        Map<Material, Integer> counts = new HashMap<>();
        int totalRevealed = 0;
        for (int i = 0; i < 9; i++) {
            if (card.revealed[i]) {
                totalRevealed++;
                counts.put(card.hiddenGrid[i], counts.getOrDefault(card.hiddenGrid[i], 0) + 1);
            }
        }

        // Check 3 matching
        for (Map.Entry<Material, Integer> entry : counts.entrySet()) {
            if (entry.getValue() >= 3 && !card.finalized) {
                card.finalized = true;
                double mult = 2.0;
                if (entry.getKey() == Material.NETHER_STAR) mult = 100.0;
                else if (entry.getKey() == Material.DIAMOND) mult = 50.0;
                else if (entry.getKey() == Material.EMERALD) mult = 25.0;
                else if (entry.getKey() == Material.GOLD_INGOT) mult = 10.0;
                else if (entry.getKey() == Material.AMETHYST_SHARD) mult = 5.0;

                double prize = card.cost * mult;
                plugin.getEconomyManager().deposit(player, prize);

                player.sendTitle("§6🎟️ ¡PREMIO MAYOR! 🎟️", "§a+" + plugin.getEconomyManager().format(prize), 10, 70, 20);
                player.sendMessage("§6🎟️ [Raspe de la Fortuna] §a¡3x " + entry.getKey().name() + "! Has ganado §e" + plugin.getEconomyManager().format(prize) + "!");
                player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
                player.spawnParticle(Particle.TOTEM, player.getLocation().add(0, 1, 0), 50, 0.5, 0.8, 0.5, 0.1);
                return;
            }
        }

        if (totalRevealed >= 9 && !card.finalized) {
            card.finalized = true;
            player.sendMessage("§c🎟️ [Raspe de la Fortuna] No hubo 3 figuras iguales en esta tarjeta. ¡Prueba otra!");
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 0.4f, 0.8f);
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (event.getView().getTopInventory().getHolder() instanceof ScratchHolder) {
            event.setCancelled(true);
        }
    }
}
