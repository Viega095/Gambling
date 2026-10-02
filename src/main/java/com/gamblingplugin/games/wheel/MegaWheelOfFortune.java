package com.gamblingplugin.games.wheel;

import com.gamblingplugin.GamblingPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
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

public class MegaWheelOfFortune implements Listener {

    public static class WheelHolder implements InventoryHolder {
        @Override
        public Inventory getInventory() {
            return null;
        }
    }

    private final GamblingPlugin plugin;
    private final Map<UUID, Long> lastFreeSpin = new HashMap<>();

    public MegaWheelOfFortune(GamblingPlugin plugin) {
        this.plugin = plugin;
    }

    public void openWheelGUI(Player player) {
        Inventory inv = Bukkit.createInventory(new WheelHolder(), 45, "§8🎡 §6Mega Ruleta de la Fortuna §8🎡");

        for (int i = 0; i < 45; i++) {
            inv.setItem(i, createPane(Material.YELLOW_STAINED_GLASS_PANE));
        }

        // Circular sector displays (Slots 10, 11, 12, 14, 15, 16, 20, 24, 28, 29, 30, 32, 33, 34)
        int[] wheelSlots = { 10, 11, 12, 14, 15, 16, 20, 24, 28, 29, 30, 32, 33, 34 };
        Material[] colors = { Material.EMERALD, Material.GOLD_INGOT, Material.DIAMOND, Material.NETHER_STAR, Material.AMETHYST_SHARD, Material.LAPIS_LAZULI, Material.REDSTONE };
        String[] multipliers = { "§aMultiplicador x2", "§eMultiplicador x5", "§bMultiplicador x10", "§d⭐ MEGA PREMIO x50 ⭐", "§6💎 JACKPOT SECRETO", "§eMultiplicador x3", "§cInténtalo de Nuevo" };

        for (int i = 0; i < wheelSlots.length; i++) {
            inv.setItem(wheelSlots[i], createBtn(colors[i % colors.length], multipliers[i % multipliers.length], Arrays.asList("§7Sector de la Mega Ruleta")));
        }

        // Center Spin Button (Slot 22)
        boolean hasFree = canUseFreeSpin(player);
        String costText = hasFree ? "§a¡GIRO GRATIS DISPONIBLE!" : "§eCosto por Giro: §6$500";
        inv.setItem(22, createBtn(Material.COMPASS, "§6⚡ §lGIRAR LA RULETA",
                Arrays.asList("§7Haz girar la rueda de la fortuna", "§7para ganar multiplicadores y botes.", "", costText, "", "§6▶ Haz clic para girar")));

        player.openInventory(inv);
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 0.8f, 1.2f);
    }

    public void spawnInteractiveStand(Location loc) {
        if (loc.getWorld() == null) return;
        org.bukkit.entity.ArmorStand stand = (org.bukkit.entity.ArmorStand) loc.getWorld().spawnEntity(loc, org.bukkit.entity.EntityType.ARMOR_STAND);
        stand.setVisible(false);
        stand.setGravity(false);
        stand.setCustomName("§6🎡 §e§lMEGA RUEDA DE LA FORTUNA §7(Clic Derecho)");
        stand.setCustomNameVisible(true);
        stand.setHelmet(new ItemStack(Material.SUNFLOWER));
    }

    public boolean canUseFreeSpin(Player player) {
        long last = lastFreeSpin.getOrDefault(player.getUniqueId(), 0L);
        return System.currentTimeMillis() - last >= 86400000L; // 24h
    }

    public void spinWheel(Player player) {
        boolean isFree = canUseFreeSpin(player);
        if (!isFree) {
            if (plugin.getEconomyManager().getBalance(player) < 500) {
                player.sendMessage(ChatColor.RED + "Saldo insuficiente para girar ($500).");
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.8f, 1f);
                return;
            }
            plugin.getEconomyManager().withdraw(player, 500);
        } else {
            lastFreeSpin.put(player.getUniqueId(), System.currentTimeMillis());
            player.sendMessage(ChatColor.GREEN + "🎁 ¡Has utilizado tu Giro Diario Gratuito!");
        }

        new BukkitRunnable() {
            int ticks = 0;
            int maxTicks = 25;

            @Override
            public void run() {
                ticks++;
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 0.7f, 1.0f + (ticks * 0.04f));

                if (ticks >= maxTicks) {
                    cancel();
                    // Award outcome
                    double roll = ThreadLocalRandom.current().nextDouble();
                    if (roll < 0.05) { // 5% Mega Jackpot x50
                        double prize = 25000.0;
                        plugin.getEconomyManager().deposit(player, prize);
                        player.sendTitle("§d⭐ ¡MEGA PREMIO x50! ⭐", "§a+" + plugin.getEconomyManager().format(prize), 10, 70, 20);
                        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
                        Bukkit.broadcastMessage(ChatColor.GOLD + "🎡 ¡" + player.getName() + " ha ganado el ⭐ MEGA PREMIO x50 ⭐ en la Ruleta de la Fortuna!");
                    } else if (roll < 0.20) { // 15% x10
                        double prize = 5000.0;
                        plugin.getEconomyManager().deposit(player, prize);
                        player.sendTitle("§b✨ ¡MULTIPLICADOR x10! ✨", "§a+" + plugin.getEconomyManager().format(prize), 10, 50, 15);
                        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.4f);
                    } else if (roll < 0.50) { // 30% x3
                        double prize = 1500.0;
                        plugin.getEconomyManager().deposit(player, prize);
                        player.sendTitle("§e✨ ¡MULTIPLICADOR x3! ✨", "§a+" + plugin.getEconomyManager().format(prize), 10, 50, 15);
                        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1.2f);
                    } else { // x2 or miss
                        double prize = 750.0;
                        plugin.getEconomyManager().deposit(player, prize);
                        player.sendTitle("§a✨ ¡PREMIO x1.5! ✨", "§a+" + plugin.getEconomyManager().format(prize), 10, 40, 10);
                        player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1f, 1.2f);
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 2L);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (event.getInventory().getHolder() instanceof WheelHolder) {
            event.setCancelled(true);
            if (!(event.getWhoClicked() instanceof Player player)) return;

            int slot = event.getRawSlot();
            if (slot == 22) {
                player.closeInventory();
                spinWheel(player);
            }
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (event.getInventory().getHolder() instanceof WheelHolder) {
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
