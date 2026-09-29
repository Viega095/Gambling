package com.gamblingplugin.gui;

import com.gamblingplugin.GamblingPlugin;
import com.gamblingplugin.games.CaseOpening;
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

public class CaseOpeningGUI implements Listener {

    private final GamblingPlugin plugin;
    private final String TITLE = "§6Case Opening - Elige tu caja";

    public CaseOpeningGUI(GamblingPlugin plugin) {
        this.plugin = plugin;
    }

    public void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, TITLE);

        // Fill with glass panes
        ItemStack glass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta glassMeta = glass.getItemMeta();
        glassMeta.setDisplayName(" ");
        glass.setItemMeta(glassMeta);

        for (int i = 0; i < 27; i++) {
            inv.setItem(i, glass);
        }

        // Common Case (slot 10)
        ItemStack common = new ItemStack(Material.CHEST);
        ItemMeta commonMeta = common.getItemMeta();
        commonMeta.setDisplayName("§7§lCASE COMMON");
        commonMeta.setLore(Arrays.asList(
                "§7Precio: §a$50",
                "",
                "§7Rarezas:",
                "§7  COMMON §7(60%): §e0.5x-1x",
                "§a  UNCOMMON §7(25%): §e1x-2x",
                "§9  RARE §7(10%): §e2x-4x",
                "§5  EPIC §7(4%): §e4x-8x",
                "§6  LEGENDARY §7(1%): §e10x-20x",
                "",
                "§eClick para abrir!"));
        common.setItemMeta(commonMeta);
        inv.setItem(10, common);

        // Rare Case (slot 13)
        ItemStack rare = new ItemStack(Material.ENDER_CHEST);
        ItemMeta rareMeta = rare.getItemMeta();
        rareMeta.setDisplayName("§9§lCASE RARE");
        rareMeta.setLore(Arrays.asList(
                "§7Precio: §a$200",
                "",
                "§7Rarezas:",
                "§7  COMMON §7(60%): §e0.5x-1x",
                "§a  UNCOMMON §7(25%): §e1x-2x",
                "§9  RARE §7(10%): §e2x-4x",
                "§5  EPIC §7(4%): §e4x-8x",
                "§6  LEGENDARY §7(1%): §e10x-20x",
                "",
                "§eClick para abrir!"));
        rare.setItemMeta(rareMeta);
        inv.setItem(13, rare);

        // Legendary Case (slot 16)
        ItemStack legendary = new ItemStack(Material.SHULKER_BOX);
        ItemMeta legendaryMeta = legendary.getItemMeta();
        legendaryMeta.setDisplayName("§6§lCASE LEGENDARY");
        legendaryMeta.setLore(Arrays.asList(
                "§7Precio: §a$1,000",
                "",
                "§7Rarezas:",
                "§7  COMMON §7(60%): §e0.5x-1x",
                "§a  UNCOMMON §7(25%): §e1x-2x",
                "§9  RARE §7(10%): §e2x-4x",
                "§5  EPIC §7(4%): §e4x-8x",
                "§6  LEGENDARY §7(1%): §e10x-20x",
                "",
                "§eClick para abrir!"));
        legendary.setItemMeta(legendaryMeta);
        inv.setItem(16, legendary);

        // Exit button (slot 26)
        ItemStack exit = new ItemStack(Material.BARRIER);
        ItemMeta exitMeta = exit.getItemMeta();
        exitMeta.setDisplayName("§cCerrar");
        exit.setItemMeta(exitMeta);
        inv.setItem(26, exit);

        player.openInventory(inv);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals(TITLE))
            return;

        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player))
            return;

        Player player = (Player) event.getWhoClicked();

        if (event.getCurrentItem() == null)
            return;

        int slot = event.getSlot();

        // Exit button
        if (slot == 26) {
            player.closeInventory();
            return;
        }

        CaseOpening.CaseType caseType = null;

        if (slot == 10) {
            caseType = CaseOpening.CaseType.COMMON;
        } else if (slot == 13) {
            caseType = CaseOpening.CaseType.RARE;
        } else if (slot == 16) {
            caseType = CaseOpening.CaseType.LEGENDARY;
        }

        if (caseType != null) {
            player.closeInventory();
            player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 1f, 1f);

            // Open case
            plugin.getGameManager().getCaseOpening().openCase(player, caseType);
        }
    }
}
