package com.gamblingplugin.gui;

import com.gamblingplugin.GamblingPlugin;
import com.gamblingplugin.games.PhysicalCrashRocket;
import com.gamblingplugin.utils.TutorialBookUtils;
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
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class CrashBettingAndCashoutGUI implements Listener {

    public static class CrashBettingHolder implements InventoryHolder {
        @Override
        public Inventory getInventory() {
            return null;
        }
    }

    public static class CrashCashoutHolder implements InventoryHolder {
        @Override
        public Inventory getInventory() {
            return null;
        }
    }

    private final GamblingPlugin plugin;
    private final Map<UUID, Double> selectedBets = new ConcurrentHashMap<>();
    private final Map<UUID, BukkitTask> cashoutRefreshTasks = new ConcurrentHashMap<>();

    public CrashBettingAndCashoutGUI(GamblingPlugin plugin) {
        this.plugin = plugin;
    }

    public void openBettingGUI(Player player) {
        double currentBet = selectedBets.computeIfAbsent(player.getUniqueId(), k -> 100.0);
        Inventory inv = Bukkit.createInventory(new CrashBettingHolder(), 27, "§8🚀 §eCohete Crash 3D: Apuesta §8🚀");

        ItemStack border = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta bMeta = border.getItemMeta();
        if (bMeta != null) {
            bMeta.setDisplayName(" ");
            border.setItemMeta(bMeta);
        }
        for (int i = 0; i < 27; i++) inv.setItem(i, border);

        // Bet Presets: Slots 10: $50, 11: $100, 12: $250, 14: $500, 15: $1000
        inv.setItem(10, createPresetItem(50.0, currentBet == 50.0));
        inv.setItem(11, createPresetItem(100.0, currentBet == 100.0));
        inv.setItem(12, createPresetItem(250.0, currentBet == 250.0));
        inv.setItem(14, createPresetItem(500.0, currentBet == 500.0));
        inv.setItem(15, createPresetItem(1000.0, currentBet == 1000.0));

        // Join / Launch Button (Slot 13)
        ItemStack launch = new ItemStack(Material.FIREWORK_ROCKET);
        ItemMeta lMeta = launch.getItemMeta();
        if (lMeta != null) {
            lMeta.setDisplayName("§a§l🚀 APOSTAR Y UNIRSE AL VUELO");
            lMeta.setLore(Arrays.asList(
                    "§7Monto de apuesta: §a$" + String.format("%.0f", currentBet),
                    "",
                    "§e▶ Haz clic para unirte a la ronda"
            ));
            launch.setItemMeta(lMeta);
        }
        inv.setItem(13, launch);

        // Tutorial Book (Slot 26)
        inv.setItem(26, TutorialBookUtils.getCrashGuide());

        player.openInventory(inv);
    }

    private ItemStack createPresetItem(double amount, boolean selected) {
        ItemStack item = new ItemStack(selected ? Material.EMERALD : Material.GOLD_NUGGET);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName((selected ? "§a§l✔ $" : "§e$") + String.format("%.0f", amount));
            meta.setLore(Arrays.asList(
                    selected ? "§a[Monto Seleccionado]" : "§7Haz clic para seleccionar $" + String.format("%.0f", amount)
            ));
            item.setItemMeta(meta);
        }
        return item;
    }

    public void openCashoutGUI(Player player, double betAmount) {
        Inventory inv = Bukkit.createInventory(new CrashCashoutHolder(), 27, "§8🚀 §a¡VUELO EN PROGRESO! CASHOUT §8🚀");

        ItemStack border = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
        ItemMeta bMeta = border.getItemMeta();
        if (bMeta != null) {
            bMeta.setDisplayName(" ");
            border.setItemMeta(bMeta);
        }
        for (int i = 0; i < 27; i++) inv.setItem(i, border);

        // Guide book (Slot 26)
        inv.setItem(26, TutorialBookUtils.getCrashGuide());

        player.openInventory(inv);

        // Real-time live update task
        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline() || !(player.getOpenInventory().getTopInventory().getHolder() instanceof CrashCashoutHolder)) {
                    cancel();
                    cashoutRefreshTasks.remove(player.getUniqueId());
                    return;
                }

                PhysicalCrashRocket rocket = plugin.getPhysicalCrashRocket();
                if (!rocket.isRoundActive()) {
                    player.closeInventory();
                    cancel();
                    cashoutRefreshTasks.remove(player.getUniqueId());
                    return;
                }

                double mult = rocket.getCurrentMultiplier();
                double currentWin = betAmount * mult;

                // Big central cashout button
                ItemStack cashoutBtn = new ItemStack(Material.EMERALD_BLOCK);
                ItemMeta cMeta = cashoutBtn.getItemMeta();
                if (cMeta != null) {
                    cMeta.setDisplayName("§a§l💰 [CLIC AQUÍ PARA RETIRAR GANANCIAS] 💰");
                    cMeta.setLore(Arrays.asList(
                            "§eMultiplicador en Vivo: §a§l" + String.format("%.2fx", mult),
                            "§6Ganancia Neta Actual: §a§l" + (plugin.getEconomyManager().getEconomy() != null ? plugin.getEconomyManager().format(currentWin) : "$" + String.format("%.2f", currentWin)),
                            "",
                            "§e▶ ¡Haz clic en cualquier momento para asegurar tu dinero!"
                    ));
                    cashoutBtn.setItemMeta(cMeta);
                }
                player.getOpenInventory().getTopInventory().setItem(13, cashoutBtn);
            }
        }.runTaskTimer(plugin, 0L, 2L);

        cashoutRefreshTasks.put(player.getUniqueId(), task);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (event.getView().getTopInventory().getHolder() instanceof CrashBettingHolder) {
            event.setCancelled(true);
            if (!(event.getWhoClicked() instanceof Player player)) return;

            int slot = event.getRawSlot();
            if (slot == 10) selectedBets.put(player.getUniqueId(), 50.0);
            else if (slot == 11) selectedBets.put(player.getUniqueId(), 100.0);
            else if (slot == 12) selectedBets.put(player.getUniqueId(), 250.0);
            else if (slot == 14) selectedBets.put(player.getUniqueId(), 500.0);
            else if (slot == 15) selectedBets.put(player.getUniqueId(), 1000.0);
            else if (slot == 13) {
                // Join flight
                double bet = selectedBets.getOrDefault(player.getUniqueId(), 100.0);
                boolean joined = plugin.getPhysicalCrashRocket().joinRound(player, bet);
                if (joined) {
                    player.closeInventory();
                    // If rocket is not launched yet, start launch after short countdown
                    if (!plugin.getPhysicalCrashRocket().isRoundActive()) {
                        plugin.getPhysicalCrashRocket().launchRocket(player.getLocation());
                    }
                    openCashoutGUI(player, bet);
                }
                return;
            }

            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1.2f);
            openBettingGUI(player);
            return;
        }

        if (event.getView().getTopInventory().getHolder() instanceof CrashCashoutHolder) {
            event.setCancelled(true);
            if (!(event.getWhoClicked() instanceof Player player)) return;

            // Clicking in the GUI cashes out!
            boolean ok = plugin.getPhysicalCrashRocket().cashOut(player);
            if (ok) {
                player.closeInventory();
            }
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (event.getView().getTopInventory().getHolder() instanceof CrashBettingHolder ||
            event.getView().getTopInventory().getHolder() instanceof CrashCashoutHolder) {
            event.setCancelled(true);
        }
    }
}
