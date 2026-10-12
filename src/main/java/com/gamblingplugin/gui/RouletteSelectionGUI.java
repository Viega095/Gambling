package com.gamblingplugin.gui;

import com.gamblingplugin.GamblingPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RouletteSelectionGUI implements Listener {

    public static class RouletteHolder implements org.bukkit.inventory.InventoryHolder {
        @Override
        public Inventory getInventory() {
            return null;
        }
    }

    private final Map<UUID, Double> pendingBets = new HashMap<>();

    public RouletteSelectionGUI(GamblingPlugin plugin) {
    }

    public void open(Player player, double betAmount) {
        pendingBets.put(player.getUniqueId(), betAmount);

        Inventory inv = Bukkit.createInventory(new RouletteHolder(), 54, "Selección de Ruleta");

        // Fill background
        fillBackground(inv);

        // Dozens (Slots 0, 1, 2)
        inv.setItem(0, createItem(Material.GOLD_INGOT, "§61ª Docena (1-12)", "§7Paga 2:1 neto (3x)"));
        inv.setItem(1, createItem(Material.GOLD_INGOT, "§62ª Docena (13-24)", "§7Paga 2:1 neto (3x)"));
        inv.setItem(2, createItem(Material.GOLD_INGOT, "§63ª Docena (25-36)", "§7Paga 2:1 neto (3x)"));

        // 0 and 00 (Green) (Slots 4, 5)
        inv.setItem(4, createItem(Material.LIME_CONCRETE, "§a0", "§7Paga 36:1"));
        inv.setItem(5, createItem(Material.LIME_CONCRETE, "§a00", "§7Paga 36:1"));

        // Columns (Slots 6, 7, 8)
        inv.setItem(6, createItem(Material.RAW_GOLD, "§eColumna 1", "§7Paga 2:1 neto (3x)"));
        inv.setItem(7, createItem(Material.RAW_GOLD, "§eColumna 2", "§7Paga 2:1 neto (3x)"));
        inv.setItem(8, createItem(Material.RAW_GOLD, "§eColumna 3", "§7Paga 2:1 neto (3x)"));

        // Numbers 1-36
        for (int i = 1; i <= 36; i++) {
            inv.setItem(8 + i, createItem(getConcrete(i), "§f" + i, "§7Paga 36:1"));
        }

        // Side Bets (Bottom Row)
        inv.setItem(45, createItem(Material.RED_CONCRETE, "§cRojo", "§7Paga 1:1"));
        inv.setItem(46, createItem(Material.BLACK_CONCRETE, "§8Negro", "§7Paga 1:1"));

        inv.setItem(48, createItem(Material.OAK_SIGN, "§ePar", "§7Paga 1:1"));
        inv.setItem(49, createItem(Material.SPRUCE_SIGN, "§eImpar", "§7Paga 1:1"));

        inv.setItem(51, createItem(Material.PAPER, "§b1-18", "§7Paga 1:1"));
        inv.setItem(52, createItem(Material.PAPER, "§b19-36", "§7Paga 1:1"));

        player.openInventory(inv);
    }

    private void fillBackground(Inventory inv) {
        ItemStack glass = createItem(Material.GRAY_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < inv.getSize(); i++) {
            if (inv.getItem(i) == null || inv.getItem(i).getType() == Material.AIR) {
                inv.setItem(i, glass);
            }
        }
    }

    private ItemStack createItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        if (lore.length > 0) {
            meta.setLore(Arrays.asList(lore));
        }
        item.setItemMeta(meta);
        return item;
    }

    private Material getConcrete(int number) {
        int[] red = { 1, 3, 5, 7, 9, 12, 14, 16, 18, 19, 21, 23, 25, 27, 30, 32, 34, 36 };
        for (int r : red) {
            if (number == r)
                return Material.RED_CONCRETE;
        }
        return Material.BLACK_CONCRETE;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getView().getTopInventory().getHolder() instanceof RouletteHolder))
            return;

        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player)) return;
        ItemStack clicked = event.getCurrentItem();

        if (clicked == null || clicked.getType() == Material.AIR
                || clicked.getType() == Material.GRAY_STAINED_GLASS_PANE)
            return;

        double bet = pendingBets.getOrDefault(player.getUniqueId(), 0.0);
        String choice = null;

        String name = clicked.getItemMeta().getDisplayName();

        if (name.equals("§cRojo"))
            choice = "red";
        else if (name.equals("§8Negro"))
            choice = "black";
        else if (name.equals("§a0"))
            choice = "0";
        else if (name.equals("§a00"))
            choice = "00";
        else if (name.equals("§ePar"))
            choice = "even";
        else if (name.equals("§eImpar"))
            choice = "odd";
        else if (name.equals("§b1-18"))
            choice = "low";
        else if (name.equals("§b19-36"))
            choice = "high";
        else if (name.contains("1ª Docena"))
            choice = "1st12";
        else if (name.contains("2ª Docena"))
            choice = "2nd12";
        else if (name.contains("3ª Docena"))
            choice = "3rd12";
        else if (name.contains("Columna 1"))
            choice = "col1";
        else if (name.contains("Columna 2"))
            choice = "col2";
        else if (name.contains("Columna 3"))
            choice = "col3";
        else {
            // It's a number
            try {
                // Strip color code
                String numStr = name.substring(2);
                Integer.parseInt(numStr); // Validate
                choice = numStr;
            } catch (Exception e) {
                return;
            }
        }

        if (choice != null) {
            player.closeInventory();
            player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
            player.performCommand("gamble roulette " + bet + " " + choice);
            pendingBets.remove(player.getUniqueId());
        }
    }

    @EventHandler
    public void onInventoryDrag(org.bukkit.event.inventory.InventoryDragEvent event) {
        if (event.getView().getTopInventory().getHolder() instanceof RouletteHolder) {
            event.setCancelled(true);
        }
    }
}
