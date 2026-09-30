package com.gamblingplugin.games;

import com.gamblingplugin.GamblingPlugin;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class BaccaratTable implements Listener {

    public enum BetType {
        PLAYER, BANKER, TIE
    }

    public static class BaccaratSession {
        public final Player player;
        public BetType betType = BetType.PLAYER;
        public double betAmount = 100.0;
        public boolean inProgress = false;
        public final List<Card> playerCards = new ArrayList<>();
        public final List<Card> bankerCards = new ArrayList<>();

        public BaccaratSession(Player player) {
            this.player = player;
        }

        public int calculateHandValue(List<Card> hand) {
            int sum = 0;
            for (Card c : hand) {
                sum += c.getBaccaratValue();
            }
            return sum % 10;
        }
    }

    public static class Card {
        public final String rank;
        public final String suit;
        public final int baccaratValue;

        public Card(String rank, String suit, int baccaratValue) {
            this.rank = rank;
            this.suit = suit;
            this.baccaratValue = baccaratValue;
        }

        public int getBaccaratValue() {
            return baccaratValue;
        }

        public ItemStack toItemStack() {
            ItemStack item = new ItemStack(Material.PAPER);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName("§f§l" + rank + " " + suit);
                meta.setLore(Arrays.asList("§7Valor Baccarat: §e" + baccaratValue + " pts"));
                item.setItemMeta(meta);
            }
            return item;
        }
    }

    public static class BaccaratHolder implements InventoryHolder {
        @Override
        public Inventory getInventory() {
            return null;
        }
    }

    private final GamblingPlugin plugin;
    private final Map<UUID, BaccaratSession> sessions = new ConcurrentHashMap<>();
    private final Random random = new Random();

    public BaccaratTable(GamblingPlugin plugin) {
        this.plugin = plugin;
    }

    private Card drawRandomCard() {
        String[] ranks = {"A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K"};
        int[] vals = {1, 2, 3, 4, 5, 6, 7, 8, 9, 0, 0, 0, 0};
        String[] suits = {"§c♥", "§c♦", "§8♠", "§8♣"};

        int rIdx = random.nextInt(ranks.length);
        int sIdx = random.nextInt(suits.length);
        return new Card(ranks[rIdx], suits[sIdx], vals[rIdx]);
    }

    public void openGUI(Player player) {
        BaccaratSession session = sessions.computeIfAbsent(player.getUniqueId(), k -> new BaccaratSession(player));
        Inventory inv = Bukkit.createInventory(new BaccaratHolder(), 54, "§8♠ §6Mesa de Baccarat Punto Banco §8♠");

        ItemStack border = new ItemStack(Material.GREEN_STAINED_GLASS_PANE);
        ItemMeta bMeta = border.getItemMeta();
        if (bMeta != null) {
            bMeta.setDisplayName(" ");
            border.setItemMeta(bMeta);
        }
        for (int i = 0; i < 54; i++) inv.setItem(i, border);

        // Player Section (Slots 10, 11, 12)
        ItemStack pHeader = new ItemStack(Material.BLUE_WOOL);
        ItemMeta pMeta = pHeader.getItemMeta();
        if (pMeta != null) {
            int pVal = session.calculateHandValue(session.playerCards);
            pMeta.setDisplayName("§9§lMANO DEL JUGADOR §7(Total: §f" + (session.playerCards.isEmpty() ? "-" : pVal) + "§7)");
            pHeader.setItemMeta(pMeta);
        }
        inv.setItem(2, pHeader);

        for (int i = 0; i < session.playerCards.size() && i < 3; i++) {
            inv.setItem(11 + i, session.playerCards.get(i).toItemStack());
        }

        // Banker Section (Slots 14, 15, 16)
        ItemStack bHeader = new ItemStack(Material.RED_WOOL);
        ItemMeta bHeadMeta = bHeader.getItemMeta();
        if (bHeadMeta != null) {
            int bVal = session.calculateHandValue(session.bankerCards);
            bHeadMeta.setDisplayName("§c§lMANO DE LA BANCA §7(Total: §f" + (session.bankerCards.isEmpty() ? "-" : bVal) + "§7)");
            bHeader.setItemMeta(bHeadMeta);
        }
        inv.setItem(6, bHeader);

        for (int i = 0; i < session.bankerCards.size() && i < 3; i++) {
            inv.setItem(15 + i, session.bankerCards.get(i).toItemStack());
        }

        // Betting Options (Slots 29: Player 1:1, 31: Tie 8:1, 33: Banker 0.95:1)
        ItemStack betP = new ItemStack(session.betType == BetType.PLAYER ? Material.BLUE_CONCRETE : Material.BLUE_TERRACOTTA);
        ItemMeta betPMeta = betP.getItemMeta();
        if (betPMeta != null) {
            betPMeta.setDisplayName("§9§lAPOSTAR AL JUGADOR (1:1)");
            betPMeta.setLore(Arrays.asList(
                    "§7Pago: §a1 a 1",
                    session.betType == BetType.PLAYER ? "§a✔ [SELECCIONADO]" : "§e▶ Clic para apostar al Jugador"
            ));
            betP.setItemMeta(betPMeta);
        }
        inv.setItem(29, betP);

        ItemStack betTie = new ItemStack(session.betType == BetType.TIE ? Material.YELLOW_CONCRETE : Material.YELLOW_TERRACOTTA);
        ItemMeta betTieMeta = betTie.getItemMeta();
        if (betTieMeta != null) {
            betTieMeta.setDisplayName("§e§lAPOSTAR AL EMPATE (8:1)");
            betTieMeta.setLore(Arrays.asList(
                    "§7Pago: §a8 a 1",
                    session.betType == BetType.TIE ? "§a✔ [SELECCIONADO]" : "§e▶ Clic para apostar al Empate"
            ));
            betTie.setItemMeta(betTieMeta);
        }
        inv.setItem(31, betTie);

        ItemStack betB = new ItemStack(session.betType == BetType.BANKER ? Material.RED_CONCRETE : Material.RED_TERRACOTTA);
        ItemMeta betBMeta = betB.getItemMeta();
        if (betBMeta != null) {
            betBMeta.setDisplayName("§c§lAPOSTAR A LA BANCA (0.95:1)");
            betBMeta.setLore(Arrays.asList(
                    "§7Pago: §a0.95 a 1 §8(Comisión 5%)",
                    session.betType == BetType.BANKER ? "§a✔ [SELECCIONADO]" : "§e▶ Clic para apostar a la Banca"
            ));
            betB.setItemMeta(betBMeta);
        }
        inv.setItem(33, betB);

        // Bet Amount Controls (Slots 38: -100, 39: -10, 40: Current Bet, 41: +10, 42: +100)
        inv.setItem(38, createControlItem(Material.REDSTONE_BLOCK, "§c- $100", "§7Disminuir apuesta"));
        inv.setItem(39, createControlItem(Material.RED_CANDLE, "§c- $10", "§7Disminuir apuesta"));

        ItemStack currBet = new ItemStack(Material.GOLD_INGOT);
        ItemMeta cMeta = currBet.getItemMeta();
        if (cMeta != null) {
            cMeta.setDisplayName("§6§lAPUESTA: §a$" + String.format("%.0f", session.betAmount));
            cMeta.setLore(Arrays.asList("§7Ajusta tu monto con los botones laterales"));
            currBet.setItemMeta(cMeta);
        }
        inv.setItem(40, currBet);

        inv.setItem(41, createControlItem(Material.LIME_CANDLE, "§a+ $10", "§7Aumentar apuesta"));
        inv.setItem(42, createControlItem(Material.EMERALD_BLOCK, "§a+ $100", "§7Aumentar apuesta"));

        // Deal Button (Slot 49)
        ItemStack deal = new ItemStack(session.inProgress ? Material.BARRIER : Material.NETHER_STAR);
        ItemMeta dealMeta = deal.getItemMeta();
        if (dealMeta != null) {
            dealMeta.setDisplayName(session.inProgress ? "§c§lREPARTIENDO CARTAS..." : "§a§l▶ REPARTIR (DEAL)");
            dealMeta.setLore(Arrays.asList("§7Costo de la mano: §a$" + String.format("%.0f", session.betAmount)));
            deal.setItemMeta(dealMeta);
        }
        inv.setItem(49, deal);

        // Guide book (Slot 53)
        inv.setItem(53, com.gamblingplugin.utils.TutorialBookUtils.getBaccaratGuide());

        player.openInventory(inv);
    }

    private ItemStack createControlItem(Material mat, String name, String lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(Collections.singletonList(lore));
            item.setItemMeta(meta);
        }
        return item;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getView().getTopInventory().getHolder() instanceof BaccaratHolder)) return;
        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) return;
        BaccaratSession session = sessions.get(player.getUniqueId());
        if (session == null || session.inProgress) return;

        int slot = event.getRawSlot();
        if (slot == 29) {
            session.betType = BetType.PLAYER;
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1.2f);
            openGUI(player);
        } else if (slot == 31) {
            session.betType = BetType.TIE;
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1.2f);
            openGUI(player);
        } else if (slot == 33) {
            session.betType = BetType.BANKER;
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1.2f);
            openGUI(player);
        } else if (slot == 38) {
            session.betAmount = Math.max(10.0, session.betAmount - 100.0);
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.8f, 0.8f);
            openGUI(player);
        } else if (slot == 39) {
            session.betAmount = Math.max(10.0, session.betAmount - 10.0);
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.8f, 0.8f);
            openGUI(player);
        } else if (slot == 41) {
            session.betAmount += 10.0;
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.8f, 1.2f);
            openGUI(player);
        } else if (slot == 42) {
            session.betAmount += 100.0;
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.8f, 1.4f);
            openGUI(player);
        } else if (slot == 49) {
            // DEAL
            startRound(player, session);
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (event.getView().getTopInventory().getHolder() instanceof BaccaratHolder) {
            event.setCancelled(true);
        }
    }

    private void startRound(Player player, BaccaratSession session) {
        if (plugin.getEconomyManager().getEconomy() != null) {
            if (!plugin.getEconomyManager().has(player, session.betAmount)) {
                player.sendMessage(ChatColor.RED + "✖ No tienes suficiente dinero para apostar $" + session.betAmount);
                return;
            }
            plugin.getEconomyManager().withdraw(player, session.betAmount);
        }

        session.inProgress = true;
        session.playerCards.clear();
        session.bankerCards.clear();
        openGUI(player);

        new BukkitRunnable() {
            int step = 0;
            @Override
            public void run() {
                if (!player.isOnline()) {
                    session.inProgress = false;
                    cancel();
                    return;
                }

                if (step == 0) {
                    session.playerCards.add(drawRandomCard());
                    player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1f, 1f);
                    openGUI(player);
                } else if (step == 1) {
                    session.bankerCards.add(drawRandomCard());
                    player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1f, 1f);
                    openGUI(player);
                } else if (step == 2) {
                    session.playerCards.add(drawRandomCard());
                    player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1f, 1.2f);
                    openGUI(player);
                } else if (step == 3) {
                    session.bankerCards.add(drawRandomCard());
                    player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1f, 1.2f);
                    openGUI(player);
                } else if (step == 4) {
                    // Third card rules
                    int pVal = session.calculateHandValue(session.playerCards);
                    int bVal = session.calculateHandValue(session.bankerCards);

                    // If natural (8 or 9), no third card
                    if (pVal < 8 && bVal < 8) {
                        if (pVal <= 5) {
                            session.playerCards.add(drawRandomCard());
                        }
                        if (bVal <= 5) {
                            session.bankerCards.add(drawRandomCard());
                        }
                    }
                    openGUI(player);
                } else if (step == 5) {
                    finishRound(player, session);
                    cancel();
                }
                step++;
            }
        }.runTaskTimer(plugin, 10L, 15L);
    }

    private void finishRound(Player player, BaccaratSession session) {
        session.inProgress = false;
        int pVal = session.calculateHandValue(session.playerCards);
        int bVal = session.calculateHandValue(session.bankerCards);

        BetType winner;
        if (pVal > bVal) winner = BetType.PLAYER;
        else if (bVal > pVal) winner = BetType.BANKER;
        else winner = BetType.TIE;

        if (session.betType == winner) {
            double payout = 0;
            if (winner == BetType.PLAYER) payout = session.betAmount * 2.0;
            else if (winner == BetType.BANKER) payout = session.betAmount * 1.95;
            else if (winner == BetType.TIE) payout = session.betAmount * 9.0;

            if (plugin.getEconomyManager().getEconomy() != null) {
                plugin.getEconomyManager().deposit(player, payout);
            }

            player.sendMessage(ChatColor.GOLD + "╔════════════════════════════════════════════════╗");
            player.sendMessage(ChatColor.GOLD + "║ " + ChatColor.GREEN + "🎉 ¡VICTORIA EN BACCARAT! (" + winner + ")" + ChatColor.GOLD + " ║");
            player.sendMessage(ChatColor.GOLD + "║ " + ChatColor.YELLOW + "Puntajes: Jugador (" + pVal + ") vs Banca (" + bVal + ")");
            player.sendMessage(ChatColor.GOLD + "║ " + ChatColor.GREEN + "Ganancia neta: " + (plugin.getEconomyManager().getEconomy() != null ? plugin.getEconomyManager().format(payout) : "$" + payout));
            player.sendMessage(ChatColor.GOLD + "╚════════════════════════════════════════════════╝");
            player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1.4f);
        } else {
            player.sendMessage(ChatColor.RED + "✖ Has perdido en Baccarat. Ganador de la mano: " + winner + " (Jugador: " + pVal + " vs Banca: " + bVal + ")");
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 0.8f);
        }

        openGUI(player);
    }
}
