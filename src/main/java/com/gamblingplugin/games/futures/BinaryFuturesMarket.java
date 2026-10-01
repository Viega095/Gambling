package com.gamblingplugin.games.futures;

import com.gamblingplugin.GamblingPlugin;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class BinaryFuturesMarket implements Listener {

    public static class FuturesHolder implements InventoryHolder {
        @Override
        public Inventory getInventory() {
            return null;
        }
    }

    private final GamblingPlugin plugin;
    private final Map<UUID, Boolean> activeTrades = new HashMap<>(); // true = UP (Call), false = DOWN (Put)
    private double currentPrice = 100.0;

    public BinaryFuturesMarket(GamblingPlugin plugin) {
        this.plugin = plugin;
    }

    public void openFuturesGUI(Player player) {
        Inventory inv = Bukkit.createInventory(new FuturesHolder(), 27, "§8📉 §eFuturos Binarios (30s) §8📈");

        for (int i = 0; i < 27; i++) {
            inv.setItem(i, createPane(Material.GRAY_STAINED_GLASS_PANE));
        }

        // Info Asset (Slot 13)
        inv.setItem(13, createBtn(Material.NETHERITE_INGOT, "§6🪙 Índice: Netherite Futures ($" + String.format("%.2f", currentPrice) + ")",
                Arrays.asList("§7Predice la dirección del precio", "§7en los próximos 15 segundos.", "§aRetorno: x1.85 Tu Apuesta ($250)", "")));

        // UP / CALL (Slot 11)
        inv.setItem(11, createBtn(Material.LIME_CONCRETE, "§a📈 SUBE (CALL / AL ALZA)",
                Arrays.asList("§7Apuesta a que el precio terminará", "§7por encima del precio actual.", "", "§a▶ Clic para apostar $250 AL ALZA")));

        // DOWN / PUT (Slot 15)
        inv.setItem(15, createBtn(Material.RED_CONCRETE, "§c📉 BAJA (PUT / A LA BAJA)",
                Arrays.asList("§7Apuesta a que el precio terminará", "§7por debajo del precio actual.", "", "§c▶ Clic para apostar $250 A LA BAJA")));

        player.openInventory(inv);
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 0.8f, 1.2f);
    }

    public void startBinaryTrade(Player player, boolean isCall) {
        if (plugin.getEconomyManager().getBalance(player) < 250) {
            player.sendMessage(ChatColor.RED + "Saldo insuficiente para operar ($250).");
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.8f, 1f);
            return;
        }

        plugin.getEconomyManager().withdraw(player, 250);
        double strikePrice = currentPrice;
        String typeName = isCall ? "§a📈 AL ALZA (CALL)" : "§c📉 A LA BAJA (PUT)";
        player.sendMessage(ChatColor.GOLD + "📉 [Futuros] " + ChatColor.YELLOW + "Posición abierta: " + typeName + " a $" + String.format("%.2f", strikePrice));

        new BukkitRunnable() {
            int secondsLeft = 15;
            double simPrice = strikePrice;
            StringBuilder candleHistory = new StringBuilder();

            @Override
            public void run() {
                if (!player.isOnline()) {
                    cancel();
                    return;
                }

                secondsLeft--;
                double delta = (ThreadLocalRandom.current().nextDouble() - 0.49) * 3.5;
                simPrice += delta;
                currentPrice = Math.max(10.0, simPrice);

                if (delta >= 0) {
                    candleHistory.append("§a█ ");
                } else {
                    candleHistory.append("§c█ ");
                }

                String directionColor = simPrice >= strikePrice ? "§a▲ " : "§c▼ ";
                String bar = "§8[ " + candleHistory.toString() + "§8] " + directionColor + "§f$" + String.format("%.2f", simPrice) + 
                        " §7(Strike: $" + String.format("%.2f", strikePrice) + ") §e⏱ " + secondsLeft + "s";
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(bar));

                if (secondsLeft <= 0) {
                    cancel();
                    boolean won = isCall ? (simPrice > strikePrice) : (simPrice < strikePrice);
                    if (won) {
                        double payout = 250.0 * 1.85;
                        plugin.getEconomyManager().deposit(player, payout);
                        player.sendTitle("§a📈 ¡PREDICCIÓN ACERTADA!", "§6+" + plugin.getEconomyManager().format(payout), 10, 60, 20);
                        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1.2f);
                        player.sendMessage(ChatColor.GREEN + "✔ ¡Tu contrato de futuros expiró en ganancias!");
                    } else {
                        player.sendTitle("§c📉 CONTRATO EXPIRADO", "§7Cierre: $" + String.format("%.2f", simPrice), 10, 50, 15);
                        player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 0.7f, 1f);
                        player.sendMessage(ChatColor.RED + "✖ El mercado se movió en tu contra.");
                    }
                }
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (event.getInventory().getHolder() instanceof FuturesHolder) {
            event.setCancelled(true);
            if (!(event.getWhoClicked() instanceof Player player)) return;

            int slot = event.getRawSlot();
            if (slot == 11) { // Call
                player.closeInventory();
                startBinaryTrade(player, true);
            } else if (slot == 15) { // Put
                player.closeInventory();
                startBinaryTrade(player, false);
            }
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (event.getInventory().getHolder() instanceof FuturesHolder) {
            event.setCancelled(true);
        }
    }

    private ItemStack createPane(Material material) {
        ItemStack pane = new ItemStack(material);
        ItemMeta meta = pane.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(" ");
            pane.setItemMeta(meta);
        }
        return pane;
    }

    private ItemStack createBtn(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }
}
