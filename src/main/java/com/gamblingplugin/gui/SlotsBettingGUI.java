package com.gamblingplugin.gui;

import com.gamblingplugin.GamblingPlugin;
import com.gamblingplugin.structures.Slots3DStructure;
import com.gamblingplugin.utils.TutorialBookUtils;
import org.bukkit.Bukkit;
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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SlotsBettingGUI implements Listener {

    public static class SlotsHolder implements InventoryHolder {
        @Override
        public Inventory getInventory() {
            return null;
        }
    }

    private final GamblingPlugin plugin;
    private final Map<UUID, Double> selectedBets = new HashMap<>();

    public SlotsBettingGUI(GamblingPlugin plugin) {
        this.plugin = plugin;
    }

    public void open(Player player) {
        double currentBet = selectedBets.getOrDefault(player.getUniqueId(), 100.0);
        Inventory inv = Bukkit.createInventory(new SlotsHolder(), 45, "§8🎰 §6Tragamonedas 3D - Apuesta §8🎰");

        // Panes
        for (int i = 0; i < 45; i++) {
            inv.setItem(i, createPane(Material.BLACK_STAINED_GLASS_PANE));
        }

        // Chip selection
        inv.setItem(10, createChip(10, Material.IRON_NUGGET, currentBet == 10));
        inv.setItem(11, createChip(50, Material.GOLD_NUGGET, currentBet == 50));
        inv.setItem(12, createChip(100, Material.DIAMOND, currentBet == 100));
        inv.setItem(13, createChip(500, Material.EMERALD, currentBet == 500));
        inv.setItem(14, createChip(1000, Material.NETHER_STAR, currentBet == 1000));
        inv.setItem(15, createChip(5000, Material.NETHERITE_INGOT, currentBet == 5000));

        // Custom Bet (Slot 16)
        inv.setItem(16, createBtn(Material.WRITABLE_BOOK, "§d§lApuesta Personalizada", Arrays.asList(
                "§7Escribe tu monto en el chat",
                "§7Actual: §a$" + String.format("%.2f", currentBet),
                "",
                "§e▶ Clic para ingresar monto"
        )));

        // Paytable Display (Slot 22)
        inv.setItem(22, createBtn(Material.GOLD_BLOCK, "§e§lTABLA DE PAGOS & COMBOS", Arrays.asList(
                "§d⭐ 3x Nether Star: §a50x + JACKPOT",
                "§b💎 3x Diamante: §a25x",
                "§a💰 3x Esmeralda: §a15x",
                "§e👑 3x Oro: §a10x",
                "§5🔮 3x Amatista: §a8x",
                "§c⚡ 3x Redstone: §a5x",
                "§9🌊 3x Lapis: §a3x",
                "§f✨ 2x Coincidencias: §e1.5x",
                "§6🎁 ¡Probabilidad de 3 Giros Gratis!"
        )));

        // Guide Book (Slot 36)
        inv.setItem(36, TutorialBookUtils.getSlotsGuide());

        // Pull Lever / Spin Button (Slot 31)
        inv.setItem(31, createBtn(Material.LEVER, "§a⚡ §l¡TIRAR DE LA PALANCA! ⚡", Arrays.asList(
                "§7Inicia el giro físico 3D",
                "§7Apuesta seleccionada: §e$" + String.format("%.2f", currentBet),
                "",
                "§a▶ Clic para jugar ahora"
        )));

        player.openInventory(inv);
        player.playSound(player.getLocation(), Sound.BLOCK_LEVER_CLICK, 0.8f, 1.4f);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (event.getInventory().getHolder() instanceof SlotsHolder) {
            event.setCancelled(true);
            if (!(event.getWhoClicked() instanceof Player player)) return;

            int slot = event.getRawSlot();
            double currentBet = selectedBets.getOrDefault(player.getUniqueId(), 100.0);

            if (slot == 10) { selectedBets.put(player.getUniqueId(), 10.0); open(player); }
            else if (slot == 11) { selectedBets.put(player.getUniqueId(), 50.0); open(player); }
            else if (slot == 12) { selectedBets.put(player.getUniqueId(), 100.0); open(player); }
            else if (slot == 13) { selectedBets.put(player.getUniqueId(), 500.0); open(player); }
            else if (slot == 14) { selectedBets.put(player.getUniqueId(), 1000.0); open(player); }
            else if (slot == 15) { selectedBets.put(player.getUniqueId(), 5000.0); open(player); }
            else if (slot == 16) {
                player.closeInventory();
                plugin.getBettingGUI().open(player, "slots");
            }
            else if (slot == 31) {
                player.closeInventory();

                // Check balance
                if (!plugin.getEconomyManager().has(player, currentBet)) {
                    player.sendMessage("§c✖ Fondos insuficientes para apostar $" + currentBet);
                    return;
                }

                // Check if near slots structure
                Slots3DStructure struct = plugin.getStructureManager().getSlotsStructure(player.getLocation());
                if (struct != null) {
                    struct.spin(player, currentBet, null);
                } else {
                    new com.gamblingplugin.games.PhysicalSlotsCabinet(plugin, player.getLocation().add(player.getLocation().getDirection().multiply(2.2))).spin(player, currentBet);
                }
            }
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (event.getInventory().getHolder() instanceof SlotsHolder) {
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

    private ItemStack createChip(int amount, Material mat, boolean selected) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName((selected ? "§a§l✔ " : "§e") + "$" + amount);
            meta.setLore(Arrays.asList(
                    selected ? "§a¡Apuesta actualmente seleccionada!" : "§7Haz clic para seleccionar esta apuesta."
            ));
            item.setItemMeta(meta);
        }
        return item;
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
