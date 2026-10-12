package com.gamblingplugin.gui;

import com.gamblingplugin.GamblingPlugin;
import com.gamblingplugin.games.KenoGame;
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

import java.util.*;

public class KenoGUI implements Listener {

    public static class KenoHolder implements InventoryHolder {
        private final Set<Integer> selectedNumbers = new HashSet<>();
        private double bet = 100.0;

        @Override
        public Inventory getInventory() {
            return null;
        }

        public Set<Integer> getSelectedNumbers() {
            return selectedNumbers;
        }

        public double getBet() {
            return bet;
        }

        public void setBet(double bet) {
            this.bet = Math.max(10.0, bet);
        }
    }

    private final GamblingPlugin plugin;
    private static final String TITLE = "§6§l🎱 Keno de la Fortuna 3D";

    public KenoGUI(GamblingPlugin plugin) {
        this.plugin = plugin;
    }

    public void open(Player player) {
        open(player, new KenoHolder());
    }

    public void open(Player player, KenoHolder holder) {
        Inventory inv = Bukkit.createInventory(holder, 54, TITLE);
        render(inv, holder);
        player.openInventory(inv);
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1.4f);
    }

    private void render(Inventory inv, KenoHolder holder) {
        // Numbers 1 to 40 (Slots 0 to 39: 5 rows of 8)
        for (int i = 1; i <= 40; i++) {
            int slot = getSlotForNumber(i);
            boolean selected = holder.getSelectedNumbers().contains(i);

            ItemStack numItem = new ItemStack(selected ? Material.LIME_DYE : Material.GRAY_DYE);
            ItemMeta meta = numItem.getItemMeta();
            if (meta != null) {
                meta.setDisplayName((selected ? "§a§l✔ Número " : "§7Número ") + i);
                meta.setLore(Arrays.asList(
                        selected ? "§a[SELECCIONADO]" : "§7No seleccionado",
                        "",
                        selected ? "§c▶ Clic para desmarcar" : "§e▶ Clic para elegir este número"
                ));
                numItem.setItemMeta(meta);
            }
            inv.setItem(slot, numItem);
        }

        // Sidebar / Bottom Controls
        // Slots 8, 17, 26, 35 are right border fillers
        ItemStack separator = createItem(Material.BLACK_STAINED_GLASS_PANE, " ");
        inv.setItem(8, separator);
        inv.setItem(17, separator);
        inv.setItem(26, separator);
        inv.setItem(35, separator);

        // Row 6: Controls (45 - 53)
        // 45: Limpiar Selección
        inv.setItem(45, createItem(Material.BARRIER, "§c✖ Limpiar Todo", "§7Desmarca todos los números elegidos"));

        // 46: Selección Rápida (5 aleatorios)
        inv.setItem(46, createItem(Material.FEATHER, "§b🎲 Selección Rápida (5)", "§7Elige 5 números aleatorios"));

        // 47: Disminuir apuesta
        inv.setItem(47, createItem(Material.REDSTONE, "§c-$50 Apuesta", "§7Disminuye el monto actual"));

        // 48: Apuesta actual
        inv.setItem(48, createItem(Material.GOLD_INGOT, "§e✦ Apuesta: §a$" + String.format("%.2f", holder.getBet()),
                "§7Números elegidos: §e" + holder.getSelectedNumbers().size() + "/10",
                "",
                "§7Acierta bolas para multiplicar tu premio hasta §650x"));

        // 49: Aumentar apuesta
        inv.setItem(49, createItem(Material.EMERALD, "§a+$50 Apuesta", "§7Aumenta el monto actual"));

        // 51: Tabla de pagos
        inv.setItem(51, createItem(Material.BOOK, "§6📜 Tabla de Pagos Keno",
                "§71 Acierto: §f1.5x",
                "§72 Aciertos: §f2.2x",
                "§73 Aciertos: §f4.5x",
                "§74 Aciertos: §f9.0x",
                "§75 Aciertos: §f18.0x",
                "§76 Aciertos: §f30.0x",
                "§77+ Aciertos: §6§l50.0x (JACKPOT)"));

        // 53: JUGAR / INICIAR SORTEO
        boolean ready = !holder.getSelectedNumbers().isEmpty();
        inv.setItem(53, createItem(ready ? Material.NETHER_STAR : Material.STRUCTURE_VOID,
                ready ? "§a§l▶ ¡INICIAR SORTEO KENO!" : "§7Selecciona al menos 1 número",
                ready ? Arrays.asList("§7Costo: §a$" + String.format("%.2f", holder.getBet()), "§e¡Que la suerte te acompañe!") :
                        Collections.singletonList("§cDebes elegir entre 1 y 10 números.")
        ));
    }

    private int getSlotForNumber(int number) {
        int index = number - 1; // 0..39
        int row = index / 8;
        int col = index % 8;
        return row * 9 + col;
    }

    private int getNumberFromSlot(int slot) {
        int row = slot / 9;
        int col = slot % 9;
        if (row < 5 && col < 8) {
            return row * 8 + col + 1;
        }
        return -1;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof KenoHolder holder)) return;
        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) return;
        int slot = event.getRawSlot();
        if (slot >= 54) return;

        int num = getNumberFromSlot(slot);
        if (num != -1) {
            if (holder.getSelectedNumbers().contains(num)) {
                holder.getSelectedNumbers().remove(num);
                player.playSound(player.getLocation(), Sound.BLOCK_DISPENSER_DISPENSE, 1f, 1.5f);
            } else {
                if (holder.getSelectedNumbers().size() >= 10) {
                    player.sendMessage("§c✖ Máximo 10 números permitidos por sorteo.");
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
                    return;
                }
                holder.getSelectedNumbers().add(num);
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.8f, 1.6f);
            }
            render(event.getInventory(), holder);
            return;
        }

        // Action controls
        if (slot == 45) { // Clear
            holder.getSelectedNumbers().clear();
            player.playSound(player.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 1f, 1.2f);
            render(event.getInventory(), holder);
        } else if (slot == 46) { // Quick Pick 5
            holder.getSelectedNumbers().clear();
            List<Integer> list = new ArrayList<>();
            for (int i = 1; i <= 40; i++) list.add(i);
            Collections.shuffle(list);
            for (int i = 0; i < 5; i++) holder.getSelectedNumbers().add(list.get(i));
            player.playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_GOLD, 1f, 1.3f);
            render(event.getInventory(), holder);
        } else if (slot == 47) { // -$50
            holder.setBet(holder.getBet() - 50.0);
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
            render(event.getInventory(), holder);
        } else if (slot == 49) { // +$50
            holder.setBet(holder.getBet() + 50.0);
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1.2f);
            render(event.getInventory(), holder);
        } else if (slot == 53) { // Play
            if (holder.getSelectedNumbers().isEmpty()) {
                player.sendMessage("§c✖ Elige al menos 1 número para comenzar.");
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
                return;
            }
            player.closeInventory();
            KenoGame game = new KenoGame(plugin);
            game.playKeno(player, holder.getBet(), new HashSet<>(holder.getSelectedNumbers()), () -> {
                // Done
            });
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (event.getInventory().getHolder() instanceof KenoHolder) {
            event.setCancelled(true);
        }
    }

    private ItemStack createItem(Material mat, String name, String... lore) {
        return createItem(mat, name, Arrays.asList(lore));
    }

    private ItemStack createItem(Material mat, String name, List<String> lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }
}
