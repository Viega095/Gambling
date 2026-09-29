package com.gamblingplugin.gui;

import com.gamblingplugin.GamblingPlugin;
import com.gamblingplugin.manager.VIPManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class VIPGUI implements Listener {

    private final GamblingPlugin plugin;
    private static final int CLAIM_SLOT = 13;

    public VIPGUI(GamblingPlugin plugin) {
        this.plugin = plugin;
    }

    public void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, ChatColor.GOLD + "✦ Club VIP & Rakeback ✦");

        VIPManager.VIPTier tier = plugin.getVIPManager().getTier(player);
        double wagered = plugin.getVIPManager().getTotalWagered(player);
        double rakeback = plugin.getVIPManager().getClaimableRakeback(player);

        // Status Item
        ItemStack status = new ItemStack(Material.NETHER_STAR);
        ItemMeta meta = status.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(tier.color + "Rango VIP Actual: " + tier.displayName);
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Total Apostado: " + ChatColor.YELLOW + plugin.getEconomyManager().format(wagered));
            lore.add(ChatColor.GRAY + "Tasa de Rakeback: " + ChatColor.GREEN + String.format("%.1f%%", tier.rakebackRate * 100));
            lore.add("");
            lore.add(ChatColor.GOLD + "Rakeback Acumulado:");
            lore.add(ChatColor.GREEN + "  " + plugin.getEconomyManager().format(rakeback));
            lore.add("");
            lore.add(ChatColor.YELLOW + "¡Haz clic para reclamar tus fondos!");
            meta.setLore(lore);
            status.setItemMeta(meta);
        }
        inv.setItem(CLAIM_SLOT, status);

        // Fill background
        ItemStack glass = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta gMeta = glass.getItemMeta();
        if (gMeta != null) {
            gMeta.setDisplayName(" ");
            glass.setItemMeta(gMeta);
        }
        for (int i = 0; i < 27; i++) {
            if (i != CLAIM_SLOT) {
                inv.setItem(i, glass);
            }
        }

        player.openInventory(inv);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().contains("Club VIP & Rakeback")) return;
        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) return;

        if (event.getRawSlot() == CLAIM_SLOT) {
            plugin.getVIPManager().claimRakeback(player);
            open(player);
        }
    }
}
