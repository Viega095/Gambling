package com.gamblingplugin.manager;

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
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public class CasinoVIPLounge implements Listener {

    public static class LoungeHolder implements InventoryHolder {
        @Override
        public Inventory getInventory() {
            return null;
        }
    }

    private final GamblingPlugin plugin;

    public CasinoVIPLounge(GamblingPlugin plugin) {
        this.plugin = plugin;
    }

    public void openLoungeMenu(Player player) {
        Inventory inv = Bukkit.createInventory(new LoungeHolder(), 27, "§8🍸 §6Bar & Lounge Exclusivo VIP §8🍸");

        ItemStack border = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        org.bukkit.inventory.meta.ItemMeta bMeta = border.getItemMeta();
        if (bMeta != null) {
            bMeta.setDisplayName(" ");
            border.setItemMeta(bMeta);
        }
        for (int i = 0; i < 27; i++) inv.setItem(i, border);

        // Cocktails
        // Slot 10: Martini Royale
        ItemStack martini = new ItemStack(Material.POTION);
        PotionMeta p1 = (PotionMeta) martini.getItemMeta();
        if (p1 != null) {
            p1.setDisplayName("§b🍸 Martini Royale §e(Exclusivo)");
            p1.setLore(Arrays.asList(
                    "§7Un cóctel refinado con aceituna dorada.",
                    "§a✦ Otorga: §fVelocidad II (10 min)",
                    "§a✦ Bono: §e+15% Ganancias en apuestas",
                    "",
                    "§6Precio: §a$250",
                    "§e▶ Haz clic para ordenar"
            ));
            p1.setColor(Color.fromRGB(135, 206, 250));
            martini.setItemMeta(p1);
        }
        inv.setItem(10, martini);

        // Slot 12: Champagne Grand Cru
        ItemStack champagne = new ItemStack(Material.POTION);
        PotionMeta p2 = (PotionMeta) champagne.getItemMeta();
        if (p2 != null) {
            p2.setDisplayName("§6🍾 Champagne Grand Cru Diamante");
            p2.setLore(Arrays.asList(
                    "§7Burbujas doradas de alta sociedad.",
                    "§a✦ Otorga: §fSuerte II + Resplandor (15 min)",
                    "§a✦ Bono: §65% Cashback en pérdidas de casino",
                    "",
                    "§6Precio: §a$1,000",
                    "§e▶ Haz clic para ordenar"
            ));
            p2.setColor(Color.fromRGB(255, 215, 0));
            champagne.setItemMeta(p2);
        }
        inv.setItem(12, champagne);

        // Slot 14: Lucky Leprechaun Ale
        ItemStack ale = new ItemStack(Material.POTION);
        PotionMeta p3 = (PotionMeta) ale.getItemMeta();
        if (p3 != null) {
            p3.setDisplayName("§a🍺 Lucky Leprechaun Ale");
            p3.setLore(Arrays.asList(
                    "§7Cerveza artesanal con tréboles de 4 hojas.",
                    "§a✦ Otorga: §fPrisa Minera II (10 min)",
                    "§a✦ Bono: §a+10% Probabilidad en Plinko y Minas",
                    "",
                    "§6Precio: §a$500",
                    "§e▶ Haz clic para ordenar"
            ));
            p3.setColor(Color.fromRGB(50, 205, 50));
            ale.setItemMeta(p3);
        }
        inv.setItem(14, ale);

        // Slot 16: Dragon's Breath Whisky
        ItemStack whisky = new ItemStack(Material.POTION);
        PotionMeta p4 = (PotionMeta) whisky.getItemMeta();
        if (p4 != null) {
            p4.setDisplayName("§c🔥 Dragon's Breath Whisky");
            p4.setLore(Arrays.asList(
                    "§7Destilado en llamas del Nether.",
                    "§a✦ Otorga: §fFuerza II + Resistencia al Fuego (10 min)",
                    "§a✦ Bono: §cPartículas ígneas al ganar apuestas",
                    "",
                    "§6Precio: §a$750",
                    "§e▶ Haz clic para ordenar"
            ));
            p4.setColor(Color.fromRGB(220, 20, 60));
            whisky.setItemMeta(p4);
        }
        inv.setItem(16, whisky);

        player.openInventory(inv);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getView().getTopInventory().getHolder() instanceof LoungeHolder)) return;
        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) return;

        int slot = event.getRawSlot();
        if (slot == 10) buyDrink(player, "Martini Royale", 250.0, PotionEffectType.SPEED, 12000, 1);
        else if (slot == 12) buyDrink(player, "Champagne Grand Cru", 1000.0, PotionEffectType.LUCK, 18000, 1);
        else if (slot == 14) buyDrink(player, "Lucky Leprechaun Ale", 500.0, PotionEffectType.FAST_DIGGING, 12000, 1);
        else if (slot == 16) buyDrink(player, "Dragon's Breath Whisky", 750.0, PotionEffectType.INCREASE_DAMAGE, 12000, 1);
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (event.getView().getTopInventory().getHolder() instanceof LoungeHolder) {
            event.setCancelled(true);
        }
    }

    private void buyDrink(Player player, String name, double price, PotionEffectType type, int durationTicks, int amplifier) {
        if (plugin.getEconomyManager().getEconomy() != null) {
            if (!plugin.getEconomyManager().has(player, price)) {
                player.sendMessage(ChatColor.RED + "✖ No tienes suficiente dinero. Requiere " + plugin.getEconomyManager().format(price));
                return;
            }
            plugin.getEconomyManager().withdraw(player, price);
        }

        player.addPotionEffect(new PotionEffect(type, durationTicks, amplifier));
        player.sendMessage(ChatColor.GOLD + "🍸 ¡Has ordenado un §e" + name + "§6 por " + (plugin.getEconomyManager().getEconomy() != null ? plugin.getEconomyManager().format(price) : "$" + price) + "!");
        player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_DRINK, 1f, 1f);
        player.closeInventory();
    }
}
