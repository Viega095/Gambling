package com.gamblingplugin.gui;

import com.gamblingplugin.GamblingPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class PlinkoGUI implements Listener {

    private final GamblingPlugin plugin;
    private static final String TITLE = "§c§lPLINKO - Elige modo";

    public PlinkoGUI(GamblingPlugin plugin) {
        this.plugin = plugin;
    }

    public void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, TITLE);

        // Background
        ItemStack gray = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta grayMeta = gray.getItemMeta();
        grayMeta.setDisplayName(" ");
        gray.setItemMeta(grayMeta);

        for (int i = 0; i < 27; i++) {
            inv.setItem(i, gray);
        }

        // Risk modes
        inv.setItem(11, createModeItem("SAFE", Material.SHIELD));
        inv.setItem(13, createModeItem("NORMAL", Material.DIAMOND));
        inv.setItem(15, createModeItem("EXTREME", Material.NETHER_STAR));

        // Info
        ItemStack info = new ItemStack(Material.BOOK);
        ItemMeta infoMeta = info.getItemMeta();
        infoMeta.setDisplayName("§e§lPLINKO CUSTOMIZABLE");
        infoMeta.setLore(Arrays.asList(
                "",
                "§7Elige tu nivel de riesgo:",
                "",
                "§a🛡️ SEGURO §7- Ganancias estables",
                "§e⚖️ NORMAL §7- Balanceado",
                "§c💀 EXTREMO §7- ¡Alto riesgo!",
                "",
                "§7Cada modo tiene diferentes",
                "§7multiplicadores y opciones!"));
        info.setItemMeta(infoMeta);
        inv.setItem(4, info);

        // Exit
        ItemStack exit = new ItemStack(Material.BARRIER);
        ItemMeta exitMeta = exit.getItemMeta();
        exitMeta.setDisplayName("§cCerrar");
        exit.setItemMeta(exitMeta);
        inv.setItem(26, exit);

        player.openInventory(inv);
    }

    private ItemStack createModeItem(String mode, Material material) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        switch (mode) {
            case "SAFE":
                meta.setDisplayName("§a§l🛡️ MODO SEGURO");
                meta.setLore(Arrays.asList(
                        "",
                        "§7Multiplicadores: §e0.5x - 1.3x",
                        "§7Distribución: §aEstable",
                        "",
                        "§a✓ Bajo riesgo",
                        "§a✓ Ganancias pequeñas pero estables",
                        "§c✗ No hay jackpots grandes",
                        "",
                        "§7Perfecto para jugar seguro",
                        "§7sin perder mucho dinero.",
                        "",
                        "§eClick para ver opciones!"));
                break;
            case "NORMAL":
                meta.setDisplayName("§e§l⚖️ MODO NORMAL");
                meta.setLore(Arrays.asList(
                        "",
                        "§7Multiplicadores: §e0.5x - 10x",
                        "§7Distribución: §eBalanceada",
                        "",
                        "§e◆ Riesgo medio",
                        "§a✓ Posibilidad de x10",
                        "§c✗ Puedes perder en el centro",
                        "",
                        "§7Balance perfecto entre",
                        "§7riesgo y recompensa.",
                        "",
                        "§eClick para ver opciones!"));
                break;
            case "EXTREME":
                meta.setDisplayName("§c§l💀 MODO EXTREMO");
                meta.setLore(Arrays.asList(
                        "",
                        "§7Multiplicadores: §c0.2x - 50x",
                        "§7Distribución: §cPeligrosa",
                        "",
                        "§c✗ ¡MUY alto riesgo!",
                        "§6✓ ¡JACKPOT x50 posible!",
                        "§c✗ Pierdes 80% en el centro",
                        "",
                        "§c§l¡ADVERTENCIA!",
                        "§7Solo para jugadores",
                        "§7que buscan emociones fuertes.",
                        "",
                        "§eClick para ver opciones!"));
                break;
        }

        item.setItemMeta(meta);
        return item;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals(TITLE)) {
            return;
        }

        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();

        if (event.getCurrentItem() == null) {
            return;
        }

        int slot = event.getSlot();

        // Exit
        if (slot == 26) {
            player.closeInventory();
            return;
        }

        // Mode selection
        String mode = null;
        switch (slot) {
            case 11:
                mode = "SAFE";
                break;
            case 13:
                mode = "NORMAL";
                break;
            case 15:
                mode = "EXTREME";
                break;
            default:
                return;
        }

        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);

        // Open ball selection for chosen mode
        PlinkoBallSelectionGUI ballGUI = new PlinkoBallSelectionGUI(plugin, mode);
        ballGUI.open(player);
    }
}
