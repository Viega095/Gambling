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

public class DiceGUI implements Listener {

    private final GamblingPlugin plugin;
    private final String TITLE = "§6§lDados";

    public DiceGUI(GamblingPlugin plugin) {
        this.plugin = plugin;
    }

    public void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, TITLE);

        // Specific numbers (slots 10-15)
        for (int i = 1; i <= 6; i++) {
            ItemStack dice = new ItemStack(Material.DIAMOND);
            ItemMeta meta = dice.getItemMeta();
            meta.setDisplayName("§a§lNúmero " + i);
            meta.setLore(Arrays.asList(
                    "§7Probabilidad: §e16.67%",
                    "§7Payout: §ax6",
                    "",
                    "§eClick para apostar al " + i));
            dice.setItemMeta(meta);
            inv.setItem(9 + i, dice);
        }

        // Low range (1-3) - Slot 19
        ItemStack lowRange = new ItemStack(Material.GOLD_INGOT);
        ItemMeta lowMeta = lowRange.getItemMeta();
        lowMeta.setDisplayName("§6§lRango Bajo (1-3)");
        lowMeta.setLore(Arrays.asList(
                "§7Probabilidad: §e50%",
                "§7Payout: §ax1.5",
                "",
                "§eGanas si sale 1, 2 o 3"));
        lowRange.setItemMeta(lowMeta);
        inv.setItem(19, lowRange);

        // High range (4-6) - Slot 25
        ItemStack highRange = new ItemStack(Material.EMERALD);
        ItemMeta highMeta = highRange.getItemMeta();
        highMeta.setDisplayName("§a§lRango Alto (4-6)");
        highMeta.setLore(Arrays.asList(
                "§7Probabilidad: §e50%",
                "§7Payout: §ax1.5",
                "",
                "§eGanas si sale 4, 5 o 6"));
        highRange.setItemMeta(highMeta);
        inv.setItem(25, highRange);

        // Tutorial Guide Book (slot 22)
        inv.setItem(22, com.gamblingplugin.utils.TutorialBookUtils.getDiceGuide());

        // Decoration
        ItemStack glass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta glassMeta = glass.getItemMeta();
        glassMeta.setDisplayName(" ");
        glass.setItemMeta(glassMeta);

        for (int i = 0; i < 27; i++) {
            if (inv.getItem(i) == null) {
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

        int slot = event.getSlot();
        String betType = null;
        int specificNumber = 0;

        // Specific numbers (slots 10-15)
        if (slot >= 10 && slot <= 15) {
            betType = "SPECIFIC";
            specificNumber = slot - 9; // slot 10 = number 1, etc.
        }
        // Low range
        else if (slot == 19) {
            betType = "LOW_RANGE";
        }
        // High range
        else if (slot == 25) {
            betType = "HIGH_RANGE";
        }

        if (betType != null) {
            player.closeInventory();
            // Open betting GUI with dice bet info (use plugin singleton!)
            // Store both betType and specificNumber as selection
            String selection = betType + ":" + specificNumber;
            plugin.getBettingGUI().open(player, "dice", selection);
        }
    }
}
