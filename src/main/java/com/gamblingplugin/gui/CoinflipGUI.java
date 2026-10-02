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

public class CoinflipGUI implements Listener {

    private final GamblingPlugin plugin;
    private final String TITLE = "§6§lCoinflip";

    public CoinflipGUI(GamblingPlugin plugin) {
        this.plugin = plugin;
    }

    public void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, TITLE);

        // HEADS option (Slot 11)
        ItemStack heads = new ItemStack(Material.GOLD_INGOT);
        ItemMeta headsMeta = heads.getItemMeta();
        headsMeta.setDisplayName("§6§lCARA (HEADS)");
        headsMeta.setLore(Arrays.asList(
                "§7Probabilidad: §e50%",
                "§7Payout: §ax2",
                "",
                "§eClick para elegir CARA"));
        heads.setItemMeta(headsMeta);
        inv.setItem(11, heads);

        // TAILS option (Slot 15)
        ItemStack tails = new ItemStack(Material.IRON_INGOT);
        ItemMeta tailsMeta = tails.getItemMeta();
        tailsMeta.setDisplayName("§7§lCRUZ (TAILS)");
        tailsMeta.setLore(Arrays.asList(
                "§7Probabilidad: §e50%",
                "§7Payout: §ax2",
                "",
                "§eClick para elegir CRUZ"));
        tails.setItemMeta(tailsMeta);
        inv.setItem(15, tails);

        // Tutorial Guide Book (slot 22)
        inv.setItem(22, com.gamblingplugin.utils.TutorialBookUtils.getCoinflipGuide());

        // Decoration
        ItemStack glass = new ItemStack(Material.YELLOW_STAINED_GLASS_PANE);
        ItemMeta glassMeta = glass.getItemMeta();
        glassMeta.setDisplayName(" ");
        glass.setItemMeta(glassMeta);

        for (int i = 0; i < 27; i++) {
            if (i != 11 && i != 15 && i != 22) {
                inv.setItem(i, glass);
            }
        }

        player.openInventory(inv);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals(TITLE))
            return;
        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player))
            return;
        Player player = (Player) event.getWhoClicked();

        if (event.getCurrentItem() == null)
            return;

        String side = null;
        if (event.getSlot() == 11) {
            side = "HEADS";
        } else if (event.getSlot() == 15) {
            side = "TAILS";
        }

        if (side == null)
            return;

        player.closeInventory();

        // Open betting GUI with selected side
        plugin.getBettingGUI().open(player, "coinflip", side);

        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1.5f);
    }
}
