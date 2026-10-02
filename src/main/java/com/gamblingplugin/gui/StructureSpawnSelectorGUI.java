package com.gamblingplugin.gui;

import com.gamblingplugin.GamblingPlugin;
import org.bukkit.Bukkit;
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

import java.util.Arrays;
import java.util.List;

public class StructureSpawnSelectorGUI implements Listener {

    public static class SpawnSelectorHolder implements InventoryHolder {
        @Override
        public Inventory getInventory() {
            return null;
        }
    }

    private final GamblingPlugin plugin;

    public StructureSpawnSelectorGUI(GamblingPlugin plugin) {
        this.plugin = plugin;
    }

    public void open(Player player) {
        Inventory inv = Bukkit.createInventory(new SpawnSelectorHolder(), 27, "§8➕ §aSpawnear Estructura 3D");

        // Background
        ItemStack bg = createItem(Material.GRAY_STAINED_GLASS_PANE, " ", null);
        for (int i = 0; i < 27; i++) {
            inv.setItem(i, bg);
        }

        // 13 Game Structures
        inv.setItem(0, createItem(Material.REDSTONE_BLOCK, "§c🚀 Cohete Crash 3D", Arrays.asList("§7Plataforma de lanzamiento y cohete 3D", "", "§a▶ Clic para spawnear aquí")));
        inv.setItem(1, createItem(Material.SUNFLOWER, "§6🎡 Mega Rueda de la Fortuna", Arrays.asList("§7Rueda vertical con 8 sectores y aguja", "", "§a▶ Clic para spawnear aquí")));
        inv.setItem(2, createItem(Material.COMPASS, "§a🎡 Ruleta Francesa 3D", Arrays.asList("§7Ruleta de 37 casillas con bola física", "", "§a▶ Clic para spawnear aquí")));
        inv.setItem(3, createItem(Material.LEVER, "§e🎰 Tragamonedas 3D", Arrays.asList("§7Cabina arcade con 3 rodillos y palanca", "", "§a▶ Clic para spawnear aquí")));
        inv.setItem(4, createItem(Material.BONE_BLOCK, "§f🎲 Mesa de Dados 3D", Arrays.asList("§7Mesa de dados con animación de lanzamiento", "", "§a▶ Clic para spawnear aquí")));
        inv.setItem(5, createItem(Material.GOLD_BLOCK, "§6🪙 Mesa de Coinflip 3D", Arrays.asList("§7Mesa con moneda dorada que salta y gira", "", "§a▶ Clic para spawnear aquí")));
        inv.setItem(6, createItem(Material.GREEN_WOOL, "§2🃏 Mesa de Blackjack 21", Arrays.asList("§7Mesa de paño verde con croupier vestido", "", "§a▶ Clic para spawnear aquí")));
        inv.setItem(7, createItem(Material.TNT, "§c💣 Campo de Buscaminas", Arrays.asList("§7Cuadrícula de minas interactivas", "", "§a▶ Clic para spawnear aquí")));
        inv.setItem(8, createItem(Material.ENDER_CHEST, "§d📦 Apertura de Cajas", Arrays.asList("§7Pedestal con cofre místico y hologramas", "", "§a▶ Clic para spawnear aquí")));
        inv.setItem(9, createItem(Material.SLIME_BALL, "§a🎯 Máquina de Plinko 3D", Arrays.asList("§7Pirámide con clavijas y caída de bolas", "", "§a▶ Clic para spawnear aquí")));
        inv.setItem(10, createItem(Material.PAPER, "§9🎴 Mesa de Baccarat Punto Banco", Arrays.asList("§7Mesa interactiva Jugador vs Banca", "", "§a▶ Clic para spawnear aquí")));
        inv.setItem(11, createItem(Material.NAME_TAG, "§e🎫 Quiosco de Lotería", Arrays.asList("§7Quiosco del pozo progresivo nacional", "", "§a▶ Clic para spawnear aquí")));
        inv.setItem(12, createItem(Material.POTION, "§5🍸 Barra VIP Lounge & Bar", Arrays.asList("§7Barra de bebidas con pociones de suerte", "", "§a▶ Clic para spawnear aquí")));

        // Slot 22: Back button
        inv.setItem(22, createItem(Material.ARROW, "§e◀ Volver al Gestor", Arrays.asList("§7Regresar al panel de estructuras")));

        player.openInventory(inv);
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 0.8f, 1.2f);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (event.getInventory().getHolder() instanceof SpawnSelectorHolder) {
            event.setCancelled(true);
            if (!(event.getWhoClicked() instanceof Player player)) return;

            int slot = event.getRawSlot();
            if (slot == 22) {
                new StructureManagerGUI(plugin).open(player);
                return;
            }

            Location spawnLoc = player.getLocation().clone();
            String name = null;

            switch (slot) {
                case 0 -> { plugin.getStructureManager().spawnCrashStructure(spawnLoc); name = "Cohete Crash 3D"; }
                case 1 -> { plugin.getStructureManager().spawnWheelStructure(spawnLoc); name = "Mega Rueda de la Fortuna"; }
                case 2 -> { plugin.getStructureManager().spawnRouletteWheel(spawnLoc); name = "Ruleta Francesa"; }
                case 3 -> { plugin.getStructureManager().spawnSlotsStructure(spawnLoc); name = "Tragamonedas 3D"; }
                case 4 -> { plugin.getStructureManager().spawnDiceStructure(spawnLoc); name = "Mesa de Dados 3D"; }
                case 5 -> { plugin.getStructureManager().spawnCoinflipStructure(spawnLoc); name = "Mesa de Coinflip 3D"; }
                case 6 -> { plugin.getStructureManager().spawnBlackjackStructure(spawnLoc); name = "Mesa de Blackjack"; }
                case 7 -> { plugin.getStructureManager().spawnMinesStructure(spawnLoc); name = "Campo de Buscaminas"; }
                case 8 -> { plugin.getStructureManager().spawnCaseStructure(spawnLoc); name = "Apertura de Cajas"; }
                case 9 -> { plugin.getStructureManager().spawnPlinkoStructure(spawnLoc); name = "Máquina de Plinko"; }
                case 10 -> { plugin.getStructureManager().spawnBaccaratStructure(spawnLoc); name = "Mesa de Baccarat"; }
                case 11 -> { plugin.getStructureManager().spawnLotteryStructure(spawnLoc); name = "Quiosco de Lotería"; }
                case 12 -> { plugin.getStructureManager().spawnVIPLoungeStructure(spawnLoc); name = "VIP Lounge Bar"; }
            }

            if (name != null) {
                player.sendMessage("§a✦ ¡Estructura '" + name + "' generada exitosamente en tu ubicación!");
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.4f);
                new StructureManagerGUI(plugin).open(player);
            }
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (event.getInventory().getHolder() instanceof SpawnSelectorHolder) {
            event.setCancelled(true);
        }
    }

    private ItemStack createItem(Material mat, String name, List<String> lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            if (lore != null) meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }
}
