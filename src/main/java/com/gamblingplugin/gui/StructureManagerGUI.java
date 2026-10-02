package com.gamblingplugin.gui;

import com.gamblingplugin.GamblingPlugin;
import com.gamblingplugin.manager.StructureManager;
import com.gamblingplugin.structures.*;
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

import java.util.*;

public class StructureManagerGUI implements Listener {

    public static class ManagerHolder implements InventoryHolder {
        @Override
        public Inventory getInventory() {
            return null;
        }
    }

    public static class StructureEntry {
        public final String type;
        public final String displayName;
        public final Material icon;
        public final Location location;
        public final Object structureObject;

        public StructureEntry(String type, String displayName, Material icon, Location location, Object structureObject) {
            this.type = type;
            this.displayName = displayName;
            this.icon = icon;
            this.location = location;
            this.structureObject = structureObject;
        }
    }

    private final GamblingPlugin plugin;
    private final Map<UUID, Integer> playerPages = new HashMap<>();

    public StructureManagerGUI(GamblingPlugin plugin) {
        this.plugin = plugin;
    }

    public void open(Player player) {
        openPage(player, 0);
    }

    public void openPage(Player player, int page) {
        playerPages.put(player.getUniqueId(), page);
        Inventory inv = Bukkit.createInventory(new ManagerHolder(), 54, "§8🏛️ §6Gestor de Estructuras 3D §7(Pág. " + (page + 1) + ")");

        // Fill background
        ItemStack border = createItem(Material.BLACK_STAINED_GLASS_PANE, " ", null);
        for (int i = 45; i < 54; i++) {
            inv.setItem(i, border);
        }

        List<StructureEntry> entries = collectAllStructures(player);

        int pageSize = 45;
        int startIndex = page * pageSize;
        int endIndex = Math.min(startIndex + pageSize, entries.size());

        for (int i = startIndex; i < endIndex; i++) {
            StructureEntry entry = entries.get(i);
            int slot = i - startIndex;

            int dist = (int) entry.location.distance(player.getLocation());
            List<String> lore = Arrays.asList(
                    "§7Tipo: §e" + entry.type,
                    "§7Mundo: §f" + (entry.location.getWorld() != null ? entry.location.getWorld().getName() : "Desconocido"),
                    "§7Posición: §fX: " + entry.location.getBlockX() + ", Y: " + entry.location.getBlockY() + ", Z: " + entry.location.getBlockZ(),
                    "§7Distancia a ti: §b" + dist + " bloques",
                    "",
                    "§b▶ Clic Izquierdo: §fTeleportarse aquí",
                    "§c▶ Clic Derecho: §fEliminar estructura",
                    "§e▶ Shift + Clic: §fRe-spawnear estructura"
            );

            inv.setItem(slot, createItem(entry.icon, entry.displayName, lore));
        }

        // Action Bar
        // Slot 45: Generar Resort
        inv.setItem(45, createItem(Material.BEACON, "§6🏛️ §lGENERAR CASINO RESORT 3D",
                Arrays.asList("§7Construye automáticamente la plataforma", "§7con los 14 juegos del casino 3D.", "", "§e▶ Haz clic para generar")));

        // Slot 47: Spawnear Estructura...
        inv.setItem(47, createItem(Material.NETHER_STAR, "§a➕ §lSPAWNEAR NUEVA ESTRUCTURA",
                Arrays.asList("§7Abre el menú para colocar cualquier", "§7mesa de juego en tu ubicación.", "", "§a▶ Haz clic para abrir")));

        // Slot 49: Refrescar
        inv.setItem(49, createItem(Material.CLOCK, "§e🔄 §lREFRESCAR LISTA",
                Arrays.asList("§7Actualiza la lista y distancias.", "§7Total activas: §e" + entries.size() + " estructuras", "", "§e▶ Haz clic para refrescar")));

        // Slot 51: Eliminar Todo
        inv.setItem(51, createItem(Material.LAVA_BUCKET, "§c🗑️ §lELIMINAR TODAS LAS ESTRUCTURAS",
                Arrays.asList("§7Borra todas las mesas y hologramas", "§7activos del servidor.", "", "§c⚠ ¡Esta acción no se puede deshacer!")));

        // Slot 53: Cerrar
        inv.setItem(53, createItem(Material.BARRIER, "§c✖ Cerrar", Arrays.asList("§7Cerrar el gestor.")));

        // Navigation
        if (page > 0) {
            inv.setItem(46, createItem(Material.ARROW, "§e◀ Página Anterior", null));
        }
        if (endIndex < entries.size()) {
            inv.setItem(52, createItem(Material.ARROW, "§ePágina Siguiente ▶", null));
        }

        player.openInventory(inv);
        player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 0.7f, 1.2f);
    }

    private List<StructureEntry> collectAllStructures(Player player) {
        List<StructureEntry> list = new ArrayList<>();
        StructureManager sm = plugin.getStructureManager();

        // 1. Crash Rocket Launchpads
        for (CrashStructure c : sm.getAllCrash().values()) {
            list.add(new StructureEntry("Crash 3D Rocket", "§c🚀 Cohete Crash 3D", Material.REDSTONE_BLOCK, c.getCenter(), c));
        }

        // 2. Mega Wheel of Fortune
        for (WheelStructure w : sm.getAllWheels().values()) {
            list.add(new StructureEntry("Mega Wheel of Fortune", "§6🎡 Mega Rueda de la Fortuna", Material.SUNFLOWER, w.getCenter(), w));
        }

        // 3. Roulette Wheels
        for (Map.Entry<String, RouletteWheel> entry : sm.getAllRoulettes().entrySet()) {
            list.add(new StructureEntry("Ruleta Francesa", "§a🎡 " + entry.getKey(), Material.COMPASS, entry.getValue().getCenter(), entry.getValue()));
        }

        // 4. Slots 3D Machines
        for (Slots3DStructure s : sm.getAllSlots().values()) {
            list.add(new StructureEntry("Tragamonedas 3D", "§e🎰 Máquina Tragamonedas 3D", Material.LEVER, s.getCenter(), s));
        }

        // 5. Dice Tables
        for (DiceStructure d : sm.getAllDice().values()) {
            list.add(new StructureEntry("Dados 3D", "§f🎲 Mesa de Dados 3D", Material.BONE_BLOCK, d.getCenter(), d));
        }

        // 6. Coinflip Tables
        for (CoinflipStructure cf : sm.getAllCoinflips().values()) {
            list.add(new StructureEntry("Coinflip 3D", "§6🪙 Mesa de Coinflip 3D", Material.GOLD_BLOCK, cf.getCenter(), cf));
        }

        // 7. Blackjack Tables
        for (BlackjackStructure b : sm.getAllBlackjack().values()) {
            list.add(new StructureEntry("Blackjack 21", "§2🃏 Mesa de Blackjack 21", Material.GREEN_WOOL, b.getCenter(), b));
        }

        // 8. Mines Fields
        for (MinesStructure m : sm.getAllMines().values()) {
            list.add(new StructureEntry("Mines", "§c💣 Campo de Buscaminas", Material.TNT, m.getCenter(), m));
        }

        // 9. Case Opening Stations
        for (CaseStructure cs : sm.getAllCases().values()) {
            list.add(new StructureEntry("Case Opening", "§d📦 Estación de Apertura de Cajas", Material.ENDER_CHEST, cs.getCenter(), cs));
        }

        // 10. Plinko Machines
        for (PlinkoStructure p : sm.getAllPlinko().values()) {
            list.add(new StructureEntry("Plinko 3D", "§a🎯 Máquina de Plinko 3D", Material.SLIME_BALL, p.getCenter(), p));
        }

        // 11. Baccarat Tables
        for (BaccaratStructure bac : sm.getAllBaccarat().values()) {
            list.add(new StructureEntry("Baccarat Punto Banco", "§9🎴 Mesa de Baccarat", Material.PAPER, bac.getCenter(), bac));
        }

        // 12. Lottery Booths
        for (LotteryStructure lot : sm.getAllLottery().values()) {
            list.add(new StructureEntry("Lotería Nacional", "§e🎫 Quiosco de Lotería", Material.NAME_TAG, lot.getCenter(), lot));
        }

        // 13. VIP Lounges
        for (VIPLoungeStructure vip : sm.getAllVIPLounge().values()) {
            list.add(new StructureEntry("VIP Lounge Bar", "§5🍸 Barra VIP Lounge", Material.POTION, vip.getCenter(), vip));
        }

        // Sort by distance to player
        list.sort((a, b) -> {
            if (a.location.getWorld() == null || b.location.getWorld() == null) return 0;
            if (!a.location.getWorld().equals(player.getWorld())) return 1;
            if (!b.location.getWorld().equals(player.getWorld())) return -1;
            return Double.compare(a.location.distance(player.getLocation()), b.location.distance(player.getLocation()));
        });

        return list;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (event.getInventory().getHolder() instanceof ManagerHolder) {
            event.setCancelled(true);
            if (!(event.getWhoClicked() instanceof Player player)) return;

            int slot = event.getRawSlot();
            int page = playerPages.getOrDefault(player.getUniqueId(), 0);

            // Bottom action buttons
            if (slot == 45) { // Generar Resort
                player.closeInventory();
                plugin.getStructureManager().generateCasinoPlaza(player);
                return;
            }
            if (slot == 47) { // Spawnear Nueva Estructura
                new StructureSpawnSelectorGUI(plugin).open(player);
                return;
            }
            if (slot == 49) { // Refrescar
                openPage(player, page);
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.8f, 1.2f);
                return;
            }
            if (slot == 51) { // Eliminar Todo
                player.closeInventory();
                plugin.getStructureManager().removeAll();
                player.sendMessage("§c🗑️ [VieGambling] Todas las estructuras han sido eliminadas.");
                player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1f, 1f);
                return;
            }
            if (slot == 53) { // Cerrar
                player.closeInventory();
                return;
            }
            if (slot == 46 && page > 0) {
                openPage(player, page - 1);
                return;
            }
            if (slot == 52) {
                openPage(player, page + 1);
                return;
            }

            // Clicked on a structure entry (slots 0-44)
            if (slot >= 0 && slot < 45) {
                List<StructureEntry> entries = collectAllStructures(player);
                int index = page * 45 + slot;
                if (index < entries.size()) {
                    StructureEntry entry = entries.get(index);

                    if (event.isShiftClick()) {
                        // Respawn
                        plugin.getStructureManager().removeStructureAt(entry.location);
                        plugin.getStructureManager().spawnStructureByType(entry.type, entry.location);
                        player.sendMessage("§e🔄 Estructura '" + entry.displayName + "' re-spawneada!");
                        openPage(player, page);
                        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 0.7f, 1.4f);
                    } else if (event.isRightClick()) {
                        // Delete
                        plugin.getStructureManager().removeStructureAt(entry.location);
                        player.sendMessage("§c🗑️ Estructura '" + entry.displayName + "' eliminada!");
                        openPage(player, page);
                        player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 0.8f, 1.2f);
                    } else {
                        // Teleport
                        player.closeInventory();
                        Location tpLoc = entry.location.clone().add(0, 0.5, 0);
                        player.teleport(tpLoc);
                        player.sendMessage("§a✦ Teleportado a " + entry.displayName + "!");
                        player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (event.getInventory().getHolder() instanceof ManagerHolder) {
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
