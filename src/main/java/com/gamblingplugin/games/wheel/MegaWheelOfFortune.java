package com.gamblingplugin.games.wheel;

import com.gamblingplugin.GamblingPlugin;
import com.gamblingplugin.structures.WheelStructure;
import com.gamblingplugin.utils.TutorialBookUtils;
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
        Inventory inv = Bukkit.createInventory(new WheelHolder(), 54, "§8🎡 §6Mega Rueda de la Fortuna 3D §8🎡");

        // Background panes
        for (int i = 0; i < 54; i++) {
            inv.setItem(i, createPane(Material.YELLOW_STAINED_GLASS_PANE));
        }

        // 16 Circular sector displays around the perimeter (slots 10-16, 25, 34-28, 19)
        int[] perimeterSlots = { 10, 11, 12, 13, 14, 15, 16, 25, 34, 33, 32, 31, 30, 29, 28, 19 };
        WheelStructure.WheelSector[] sectors = WheelStructure.SECTORS;

        for (int i = 0; i < perimeterSlots.length; i++) {
            WheelStructure.WheelSector sec = sectors[i % sectors.length];
            inv.setItem(perimeterSlots[i], createBtn(sec.material, sec.name, Arrays.asList(
                    "§7Multiplicador: §a" + sec.multiplier + "x",
                    sec.isJackpot ? "§d⭐ ¡BOTE MAYOR DEL CASINO!" : "§ePremio oficial de la Rueda 3D"
            )));
        }

        // Guide book in slot 45 (bottom left)
        inv.setItem(45, TutorialBookUtils.getWheelGuide());

        // Center Spin Button (Slot 22 / 31)
        boolean hasFree = canUseFreeSpin(player);
        String costText = hasFree ? "§a¡GIRO GRATIS DISPONIBLE!" : "§eCosto por Giro: §6$500";
        inv.setItem(22, createBtn(Material.COMPASS, "§6⚡ §lGIRAR LA MEGA RUEDA",
                Arrays.asList("§7Haz girar la gran rueda vertical 3D", "§7con 16 premios y multiplicadores.", "", costText, "", "§6▶ Haz clic para girar")));

        player.openInventory(inv);
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 0.8f, 1.2f);
    }

    public void spawnInteractiveStand(Location loc) {
        plugin.getStructureManager().spawnWheelStructure(loc);
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

        // Check if there is a 3D Wheel Structure nearby
        WheelStructure wheelStruct = plugin.getStructureManager().getWheelStructure(player.getLocation());
        if (wheelStruct != null) {
            wheelStruct.spin(player, 500.0, null);
            return;
        }

        // Standalone virtual wheel spin fallback
        new BukkitRunnable() {
            int ticks = 0;
            final int maxTicks = 35;

            @Override
            public void run() {
                ticks++;
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 0.7f, 1.0f + (ticks * 0.03f));

                if (ticks >= maxTicks) {
                    cancel();
                    // Award outcome from 16 sectors
                    WheelStructure.WheelSector won = WheelStructure.SECTORS[ThreadLocalRandom.current().nextInt(WheelStructure.SECTORS.length)];
                    double prize = 500.0 * won.multiplier;
                    plugin.getEconomyManager().deposit(player, prize);

                    if (won.isJackpot) {
                        player.sendTitle("§d⭐ ¡MEGA PREMIO x50! ⭐", "§a+" + plugin.getEconomyManager().format(prize), 10, 70, 20);
                        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
                        Bukkit.broadcastMessage(ChatColor.GOLD + "🎡 ¡" + player.getName() + " ha ganado el ⭐ MEGA PREMIO x50 ⭐ en la Ruleta de la Fortuna!");
                    } else {
                        player.sendTitle(won.name, "§a+" + plugin.getEconomyManager().format(prize), 10, 50, 15);
                        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.4f);
                    }
                    player.sendMessage("§6🎡 [Mega Rueda] §7¡Has obtenido §e" + won.name + "§7! Premio: §a" + plugin.getEconomyManager().format(prize));
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
