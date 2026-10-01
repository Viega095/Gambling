package com.gamblingplugin.games.poker;

import com.gamblingplugin.GamblingPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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
import java.util.concurrent.ThreadLocalRandom;

public class MultiplayerPokerEngine implements Listener {

    public static class PokerHolder implements InventoryHolder {
        @Override
        public Inventory getInventory() {
            return null;
        }
    }

    public enum PokerPhase {
        WAITING, PRE_FLOP, FLOP, TURN, RIVER, SHOWDOWN
    }

    public static class PokerSeat {
        public UUID playerId;
        public String name;
        public double currentBet = 0.0;
        public boolean folded = false;
        public String[] holeCards = new String[2];

        public PokerSeat(Player player) {
            this.playerId = player.getUniqueId();
            this.name = player.getName();
        }
    }

    private final GamblingPlugin plugin;
    private final List<PokerSeat> seats = new ArrayList<>();
    private final List<String> communityCards = new ArrayList<>();
    private double pot = 0.0;
    private double currentHighestBet = 100.0;
    private PokerPhase phase = PokerPhase.WAITING;

    public MultiplayerPokerEngine(GamblingPlugin plugin) {
        this.plugin = plugin;
    }

    public void openPokerTableGUI(Player player) {
        // Auto-join seat if not seated and seats < 6
        PokerSeat seat = getSeat(player.getUniqueId());
        if (seat == null && seats.size() < 6) {
            seat = new PokerSeat(player);
            seats.add(seat);
            dealHoleCards(seat);
        }

        Inventory inv = Bukkit.createInventory(new PokerHolder(), 54, "§8🀄 §2Texas Hold'em - Bote: §6$" + (int) pot);

        // Green poker felt border
        for (int i = 0; i < 54; i++) {
            inv.setItem(i, createPane(Material.GREEN_STAINED_GLASS_PANE));
        }

        // Pot & Phase info (Slot 4)
        inv.setItem(4, createBtn(Material.GOLD_BLOCK, "§6💰 BOTE TOTAL: §e$" + (int) pot,
                Arrays.asList("§7Fase: §a" + phase.name(), "§7Apuesta Actual: §e$" + (int) currentHighestBet, "§7Jugadores Sentados: §f" + seats.size() + "/6")));

        // Community Cards (Slots 20, 21, 22, 23, 24)
        int[] commSlots = { 20, 21, 22, 23, 24 };
        for (int i = 0; i < 5; i++) {
            if (i < communityCards.size()) {
                inv.setItem(commSlots[i], createCardItem(communityCards.get(i)));
            } else {
                inv.setItem(commSlots[i], createBtn(Material.WHITE_STAINED_GLASS_PANE, "§7[Carta Oculta]", Arrays.asList("§8Se revelará en la siguiente ronda.")));
            }
        }

        // Player's Hole Cards (Slots 39, 41)
        if (seat != null) {
            inv.setItem(39, createCardItem(seat.holeCards[0]));
            inv.setItem(41, createCardItem(seat.holeCards[1]));
        }

        // Action Buttons: Check/Call (Slot 47), Raise (Slot 49), Fold (Slot 51)
        inv.setItem(47, createBtn(Material.LIME_DYE, "§a✔ IGUALAR / CHECK",
                Arrays.asList("§7Iguala la apuesta actual de §6$" + (int) currentHighestBet, "", "§a▶ Clic para igualar")));

        inv.setItem(49, createBtn(Material.GOLD_INGOT, "§e⚡ SUBIR APUESTA (+$100)",
                Arrays.asList("§7Aumenta la apuesta a §6$" + (int) (currentHighestBet + 100), "", "§e▶ Clic para subir")));

        inv.setItem(51, createBtn(Material.RED_DYE, "§c✖ RETIRARSE (FOLD)",
                Arrays.asList("§7Abandona esta mano sin apostar más.", "", "§c▶ Clic para retirarte")));

        player.openInventory(inv);
        player.playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_LEATHER, 0.7f, 1.2f);
    }

    private void dealHoleCards(PokerSeat seat) {
        String[] ranks = { "A", "K", "Q", "J", "10", "9", "8", "7" };
        String[] suits = { "♠", "♥", "♦", "♣" };
        seat.holeCards[0] = ranks[ThreadLocalRandom.current().nextInt(ranks.length)] + suits[ThreadLocalRandom.current().nextInt(suits.length)];
        seat.holeCards[1] = ranks[ThreadLocalRandom.current().nextInt(ranks.length)] + suits[ThreadLocalRandom.current().nextInt(suits.length)];
    }

    private void progressPokerRound() {
        String[] ranks = { "A", "K", "Q", "J", "10", "9", "8", "7", "6" };
        String[] suits = { "♠", "♥", "♦", "♣" };

        if (phase == PokerPhase.WAITING || phase == PokerPhase.PRE_FLOP) {
            phase = PokerPhase.FLOP;
            communityCards.clear();
            for (int i = 0; i < 3; i++) {
                communityCards.add(ranks[ThreadLocalRandom.current().nextInt(ranks.length)] + suits[ThreadLocalRandom.current().nextInt(suits.length)]);
            }
        } else if (phase == PokerPhase.FLOP) {
            phase = PokerPhase.TURN;
            communityCards.add(ranks[ThreadLocalRandom.current().nextInt(ranks.length)] + suits[ThreadLocalRandom.current().nextInt(suits.length)]);
        } else if (phase == PokerPhase.TURN) {
            phase = PokerPhase.RIVER;
            communityCards.add(ranks[ThreadLocalRandom.current().nextInt(ranks.length)] + suits[ThreadLocalRandom.current().nextInt(suits.length)]);
        } else if (phase == PokerPhase.RIVER) {
            phase = PokerPhase.SHOWDOWN;
            // Award pot to active non-folded player
            for (PokerSeat s : seats) {
                if (!s.folded) {
                    Player winner = Bukkit.getPlayer(s.playerId);
                    if (winner != null && winner.isOnline()) {
                        plugin.getEconomyManager().deposit(winner, pot);
                        winner.sendTitle("§6🏆 ¡GANASTE EL BOTE DE POKER!", "§a+" + plugin.getEconomyManager().format(pot), 10, 60, 20);
                        winner.playSound(winner.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
                    }
                    break;
                }
            }
            // Reset table
            pot = 0;
            currentHighestBet = 100;
            phase = PokerPhase.PRE_FLOP;
            communityCards.clear();
        }
    }

    private PokerSeat getSeat(UUID uuid) {
        for (PokerSeat s : seats) {
            if (s.playerId.equals(uuid)) return s;
        }
        return null;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (event.getInventory().getHolder() instanceof PokerHolder) {
            event.setCancelled(true);
            if (!(event.getWhoClicked() instanceof Player player)) return;

            int slot = event.getRawSlot();
            PokerSeat seat = getSeat(player.getUniqueId());
            if (seat == null) return;

            if (slot == 47) { // Call
                double bet = currentHighestBet;
                if (plugin.getEconomyManager().getBalance(player) < bet) {
                    player.sendMessage(ChatColor.RED + "Saldo insuficiente para igualar la apuesta.");
                    return;
                }
                plugin.getEconomyManager().withdraw(player, bet);
                pot += bet;
                seat.currentBet = bet;
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 0.8f, 1.4f);
                progressPokerRound();
                openPokerTableGUI(player);
            } else if (slot == 49) { // Raise
                double raise = currentHighestBet + 100.0;
                if (plugin.getEconomyManager().getBalance(player) < raise) {
                    player.sendMessage(ChatColor.RED + "Saldo insuficiente para subir la apuesta.");
                    return;
                }
                plugin.getEconomyManager().withdraw(player, raise);
                pot += raise;
                currentHighestBet = raise;
                seat.currentBet = raise;
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.8f, 1.5f);
                progressPokerRound();
                openPokerTableGUI(player);
            } else if (slot == 51) { // Fold
                seat.folded = true;
                player.closeInventory();
                player.sendMessage(ChatColor.YELLOW + "Te has retirado de la mano actual.");
                player.playSound(player.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 0.7f, 1.2f);
            }
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (event.getInventory().getHolder() instanceof PokerHolder) {
            event.setCancelled(true);
        }
    }

    private ItemStack createCardItem(String cardName) {
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            boolean isRed = cardName.contains("♥") || cardName.contains("♦");
            ChatColor color = isRed ? ChatColor.RED : ChatColor.DARK_GRAY;
            meta.setDisplayName(color + "§l[" + cardName + "]");
            meta.setLore(Arrays.asList("§7Carta Comunitaria de Mesa", "§8Texas Hold'em Poker"));
            item.setItemMeta(meta);
        }
        return item;
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
