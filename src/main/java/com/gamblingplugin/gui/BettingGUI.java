package com.gamblingplugin.gui;

import com.gamblingplugin.GamblingPlugin;
import com.gamblingplugin.games.Dice;
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

public class BettingGUI implements Listener {

    private final GamblingPlugin plugin;
    private final Map<UUID, Double> currentBets = new HashMap<>();
    private final Map<UUID, String> activeGame = new HashMap<>(); // "roulette", "dice", "coinflip"
    private final Map<UUID, String> activeSelection = new HashMap<>(); // For games with selections (e.g., coinflip
                                                                       // side, roulette color)

    public BettingGUI(GamblingPlugin plugin) {
        this.plugin = plugin;
    }

    public void open(Player player, String gameType) {
        open(player, gameType, null);
    }

    public void open(Player player, String gameType, String selection) {
        activeGame.put(player.getUniqueId(), gameType);
        if (selection != null) {
            activeSelection.put(player.getUniqueId(), selection);
        }
        currentBets.put(player.getUniqueId(), 0.0);

        Inventory inv = Bukkit.createInventory(null, 36, "Realiza tu Apuesta");

        // Fill background
        fillBackground(inv);

        // Chip items
        inv.setItem(10, createChip(10, Material.IRON_NUGGET));
        inv.setItem(11, createChip(50, Material.GOLD_NUGGET));
        inv.setItem(12, createChip(100, Material.DIAMOND));
        inv.setItem(13, createChip(500, Material.EMERALD));
        inv.setItem(14, createChip(1000, Material.NETHER_STAR));

        // Reset button
        inv.setItem(16, createItem(Material.RED_WOOL, "§cReiniciar Apuesta", "§7Actual: §e0.0"));

        // Custom bet button
        ItemStack customBet = new ItemStack(Material.WRITABLE_BOOK);
        ItemMeta customMeta = customBet.getItemMeta();
        customMeta.setDisplayName("§d§lApuesta Personalizada");
        customMeta.setLore(Arrays.asList(
                "",
                "§7Escribe tu propia cantidad",
                "§7Máximo: §a$1,000,000",
                "",
                "§e§lClick para abrir chat!"));
        customBet.setItemMeta(customMeta);
        // Guide book on slot 27 based on gameType
        ItemStack guideBook = null;
        if ("dice".equalsIgnoreCase(gameType)) guideBook = com.gamblingplugin.utils.TutorialBookUtils.getDiceGuide();
        else if ("coinflip".equalsIgnoreCase(gameType)) guideBook = com.gamblingplugin.utils.TutorialBookUtils.getCoinflipGuide();
        else if ("roulette".equalsIgnoreCase(gameType)) guideBook = com.gamblingplugin.utils.TutorialBookUtils.getRouletteGuide();
        else if ("mines".equalsIgnoreCase(gameType)) guideBook = com.gamblingplugin.utils.TutorialBookUtils.getMinesGuide();
        else if ("slots".equalsIgnoreCase(gameType)) guideBook = com.gamblingplugin.utils.TutorialBookUtils.getSlotsGuide();
        else if ("plinko".equalsIgnoreCase(gameType)) guideBook = com.gamblingplugin.utils.TutorialBookUtils.getPlinkoGuide();
        if (guideBook != null) {
            inv.setItem(27, guideBook);
        }

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

    private ItemStack createChip(int amount, Material material) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§aAñadir " + amount);
        meta.setLore(Arrays.asList("§7Clic para añadir a la apuesta"));
        item.setItemMeta(meta);
        return item;
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

    private void updateConfirmButton(Inventory inv, double amount) {
        ItemStack item = new ItemStack(Material.LIME_WOOL);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§a§lConfirmar Apuesta: " + amount);
        meta.setLore(Arrays.asList("§7Clic para empezar"));
        item.setItemMeta(meta);
        inv.setItem(22, item);

        // Update Reset Button Lore
        ItemStack reset = inv.getItem(16);
        if (reset != null) {
            ItemMeta resetMeta = reset.getItemMeta();
            resetMeta.setLore(Arrays.asList("§7Actual: §e" + amount));
            reset.setItemMeta(resetMeta);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals("Realiza tu Apuesta"))
            return;

        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();

        if (clicked == null || clicked.getType() == Material.AIR
                || clicked.getType() == Material.GRAY_STAINED_GLASS_PANE)
            return;

        double currentBet = currentBets.getOrDefault(player.getUniqueId(), 0.0);
        boolean updated = false;

        if (clicked.getType() == Material.IRON_NUGGET) {
            currentBet += 10;
            updated = true;
        } else if (clicked.getType() == Material.GOLD_NUGGET) {
            currentBet += 50;
            updated = true;
        } else if (clicked.getType() == Material.DIAMOND) {
            currentBet += 100;
            updated = true;
        } else if (clicked.getType() == Material.EMERALD) {
            currentBet += 500;
            updated = true;
        } else if (clicked.getType() == Material.NETHER_STAR) {
            currentBet += 1000;
            updated = true;
        } else if (clicked.getType() == Material.WRITABLE_BOOK) {
            // Custom bet button
            String gameType = activeGame.get(player.getUniqueId());
            String selection = activeSelection.get(player.getUniqueId());

            // Request custom bet via chat
            com.gamblingplugin.listeners.CustomBetListener customBetListener = plugin.getServer().getServicesManager()
                    .load(com.gamblingplugin.listeners.CustomBetListener.class);
            if (customBetListener != null) {
                customBetListener.requestBet(player, gameType, selection);
            }
            return;
        } else if (clicked.getType() == Material.RED_WOOL) {
            currentBet = 0;
            updated = true;
            player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 1f, 1f);
        } else if (clicked.getType() == Material.LIME_WOOL) {
            // Confirm bet
            if (currentBet <= 0) {
                player.sendMessage("§c¡Debes realizar una apuesta primero!");
                player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_VILLAGER_NO, 1f, 1f);
                return;
            }

            String gameType = activeGame.get(player.getUniqueId());
            player.closeInventory();
            player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);

            // Null check for gameType
            if (gameType == null) {
                player.sendMessage("§c[VieGambling] Error: Tipo de juego no encontrado.");
                activeGame.remove(player.getUniqueId());
                currentBets.remove(player.getUniqueId());
                activeSelection.remove(player.getUniqueId());
                return;
            }

            if (gameType.equals("roulette")) {
                plugin.getRouletteSelectionGUI().open(player, currentBet);
            } else if (gameType.equals("dice")) {
                String selection = activeSelection.get(player.getUniqueId());

                // Null check for selection
                if (selection == null) {
                    player.sendMessage("§c[VieGambling] Error: Selección no encontrada.");
                    activeGame.remove(player.getUniqueId());
                    currentBets.remove(player.getUniqueId());
                    return;
                }

                String[] parts = selection.split(":");
                String betType = parts[0];
                int specificNumber = Integer.parseInt(parts[1]);
                if (plugin.getGameManager().getGame("dice") instanceof Dice) {
                    ((Dice) plugin.getGameManager().getGame("dice")).playWithBetType(player, currentBet, betType,
                            specificNumber);
                }
                activeSelection.remove(player.getUniqueId());
            } else if (gameType.equals("coinflip")) {
                String selection = activeSelection.get(player.getUniqueId());
                plugin.getGameManager().getCoinflip().play(player, currentBet, selection);
                activeSelection.remove(player.getUniqueId());
            } else if (gameType.equals("mines")) {
                // Play Mines game
                plugin.getGameManager().getMines().play(player, currentBet);
            } else if (gameType.equals("slots")) {
                com.gamblingplugin.structures.Slots3DStructure slotStruct = plugin.getStructureManager().getSlotsStructure(player.getLocation());
                if (slotStruct != null) {
                    slotStruct.spin(player, currentBet, null);
                } else {
                    new com.gamblingplugin.games.PhysicalSlotsCabinet(plugin, player.getLocation()).spin(player, currentBet);
                }
            } else if (gameType.equals("plinko")) {
                new com.gamblingplugin.gui.PlinkoBallSelectionGUI(plugin, "NORMAL").open(player);
            }
            return;
        }

        if (updated) {
            currentBets.put(player.getUniqueId(), currentBet);
            updateConfirmButton(event.getInventory(), currentBet);
            player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 1f, 2f);
        }
    }

    // Method for custom bet input
    public void openWithCustomBet(Player player, String gameType, String selection, double customBet) {
        activeGame.put(player.getUniqueId(), gameType);
        if (selection != null) {
            activeSelection.put(player.getUniqueId(), selection);
        }
        currentBets.put(player.getUniqueId(), customBet);

        Inventory inv = Bukkit.createInventory(null, 36, "Realiza tu Apuesta");
        fillBackground(inv);

        // Show chips (disabled)
        inv.setItem(10, createChip(10, Material.IRON_NUGGET));
        inv.setItem(11, createChip(50, Material.GOLD_NUGGET));
        inv.setItem(12, createChip(100, Material.DIAMOND));
        inv.setItem(13, createChip(500, Material.EMERALD));
        inv.setItem(14, createChip(1000, Material.NETHER_STAR));

        // Reset button (shows custom amount)
        inv.setItem(16, createItem(Material.RED_WOOL, "§cReiniciar Apuesta",
                "§7Actual: §a$" + String.format("%.2f", customBet)));

        // Confirm button
        updateConfirmButton(inv, customBet);

        player.openInventory(inv);
    }
}
