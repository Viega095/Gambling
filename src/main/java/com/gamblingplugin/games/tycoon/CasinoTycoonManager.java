package com.gamblingplugin.games.tycoon;

import com.gamblingplugin.GamblingPlugin;
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

import java.util.*;

public class CasinoTycoonManager implements Listener {

    public static class TycoonHolder implements InventoryHolder {
        @Override
        public Inventory getInventory() {
            return null;
        }
    }

    private final GamblingPlugin plugin;
    private final Map<UUID, TycoonProfile> playerProfiles = new HashMap<>();

    public static class TycoonProfile {
        public int ownedBlackjackTables = 0;
        public int ownedRouletteTables = 0;
        public int ownedCrashRockets = 0;
        public double uncollectedProfits = 0.0;
        public double totalEarned = 0.0;
    }

    public CasinoTycoonManager(GamblingPlugin plugin) {
        this.plugin = plugin;
        startPassiveProfitTask();
    }

    public TycoonProfile getProfile(UUID uuid) {
        return playerProfiles.computeIfAbsent(uuid, k -> new TycoonProfile());
    }

    private void startPassiveProfitTask() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Map.Entry<UUID, TycoonProfile> entry : playerProfiles.entrySet()) {
                TycoonProfile prof = entry.getValue();
                int totalTables = prof.ownedBlackjackTables + prof.ownedRouletteTables + prof.ownedCrashRockets;
                if (totalTables > 0) {
                    double generated = totalTables * (25.0 + Math.random() * 40.0);
                    prof.uncollectedProfits += generated;
                    prof.totalEarned += generated;
                }
            }
        }, 1200L, 1200L); // Every 60s
    }

    public void openTycoonGUI(Player player) {
        Inventory inv = Bukkit.createInventory(new TycoonHolder(), 36, "§8🏛️ §6Casino Tycoon - Dueño de Mesas §8🏛️");

        for (int i = 0; i < 36; i++) {
            inv.setItem(i, createPane(Material.BLACK_STAINED_GLASS_PANE));
        }

        TycoonProfile prof = getProfile(player.getUniqueId());

        // Slot 11: Mesa de Blackjack ($25,000)
        inv.setItem(11, createBtn(Material.DARK_OAK_SIGN, "§a♠ Comprar Mesa de Blackjack",
                Arrays.asList("§7Poseídas: §e" + prof.ownedBlackjackTables, "§7Genera: §a$25-$50 / minuto", "§7Costo: §6$25,000", "", "§a▶ Clic para comprar mesa")));

        // Slot 13: Mesa de Ruleta ($40,000)
        inv.setItem(13, createBtn(Material.RED_TERRACOTTA, "§c🎡 Comprar Ruleta Europea VIP",
                Arrays.asList("§7Poseídas: §e" + prof.ownedRouletteTables, "§7Genera: §a$50-$90 / minuto", "§7Costo: §6$40,000", "", "§a▶ Clic para comprar mesa")));

        // Slot 15: Estación Crash 3D ($75,000)
        inv.setItem(15, createBtn(Material.FIREWORK_ROCKET, "§b🚀 Comprar Estación Crash 3D",
                Arrays.asList("§7Poseídas: §e" + prof.ownedCrashRockets, "§7Genera: §a$100-$180 / minuto", "§7Costo: §6$75,000", "", "§a▶ Clic para comprar estación")));

        // Slot 31: Recaudar Ganancias
        inv.setItem(31, createBtn(Material.GOLD_BLOCK, "§e💰 Recaudar Beneficios Acumulados",
                Arrays.asList("§7Ganancias sin reclamar: §a" + plugin.getEconomyManager().format(prof.uncollectedProfits),
                        "§7Total histórico ganado: §e" + plugin.getEconomyManager().format(prof.totalEarned),
                        "", "§a▶ Clic para transferir a tu cuenta")));

        player.openInventory(inv);
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 0.8f, 1.2f);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (event.getInventory().getHolder() instanceof TycoonHolder) {
            event.setCancelled(true);
            if (!(event.getWhoClicked() instanceof Player player)) return;

            int slot = event.getRawSlot();
            TycoonProfile prof = getProfile(player.getUniqueId());

            if (slot == 11) { // Buy BJ table
                if (plugin.getEconomyManager().getBalance(player) < 25000) {
                    player.sendMessage(ChatColor.RED + "Saldo insuficiente ($25,000 requeridos).");
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.8f, 1f);
                    return;
                }
                plugin.getEconomyManager().withdraw(player, 25000);
                prof.ownedBlackjackTables++;
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.4f);
                player.sendMessage(ChatColor.GREEN + "🏛️ ¡Has adquirido una Mesa de Blackjack! Ahora generas ingresos pasivos del casino.");
                openTycoonGUI(player);
            } else if (slot == 13) { // Buy Roulette
                if (plugin.getEconomyManager().getBalance(player) < 40000) {
                    player.sendMessage(ChatColor.RED + "Saldo insuficiente ($40,000 requeridos).");
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.8f, 1f);
                    return;
                }
                plugin.getEconomyManager().withdraw(player, 40000);
                prof.ownedRouletteTables++;
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.4f);
                player.sendMessage(ChatColor.GREEN + "🏛️ ¡Has adquirido una Ruleta Europea VIP! Los jugadores financian tu imperio.");
                openTycoonGUI(player);
            } else if (slot == 15) { // Buy Crash Station
                if (plugin.getEconomyManager().getBalance(player) < 75000) {
                    player.sendMessage(ChatColor.RED + "Saldo insuficiente ($75,000 requeridos).");
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.8f, 1f);
                    return;
                }
                plugin.getEconomyManager().withdraw(player, 75000);
                prof.ownedCrashRockets++;
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.4f);
                player.sendMessage(ChatColor.GREEN + "🏛️ ¡Has adquirido una Estación Crash 3D de alto rendimiento!");
                openTycoonGUI(player);
            } else if (slot == 31) { // Collect profits
                if (prof.uncollectedProfits <= 0) {
                    player.sendMessage(ChatColor.YELLOW + "No tienes ganancias pendientes por recaudar.");
                    return;
                }
                double amount = prof.uncollectedProfits;
                prof.uncollectedProfits = 0;
                plugin.getEconomyManager().deposit(player, amount);
                player.playSound(player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1f, 1.5f);
                player.sendTitle("§6💰 ¡BENEFICIOS COBRADOS!", "§a+" + plugin.getEconomyManager().format(amount), 10, 50, 15);
                openTycoonGUI(player);
            }
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (event.getInventory().getHolder() instanceof TycoonHolder) {
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
