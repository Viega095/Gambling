package com.gamblingplugin.gui;

import com.gamblingplugin.GamblingPlugin;
import com.gamblingplugin.games.blackjack.BlackjackGame;
import com.gamblingplugin.games.blackjack.Card;
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
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class BlackjackGameGUI implements Listener {

    private final GamblingPlugin plugin;
    private static final String TITLE_PREFIX = "§0§l♠ BLACKJACK ♥ - ";

    // Custom heads for card numbers (using player UUIDs for textures)
    private static final Map<String, String> CARD_NUMBER_HEADS = new HashMap<>();
    static {
        // Using Minecraft player names that have number skins
        CARD_NUMBER_HEADS.put("A", "MHF_ArrowUp"); // Ace
        CARD_NUMBER_HEADS.put("2", "MHF_2");
        CARD_NUMBER_HEADS.put("3", "MHF_3");
        CARD_NUMBER_HEADS.put("4", "MHF_4");
        CARD_NUMBER_HEADS.put("5", "MHF_5");
        CARD_NUMBER_HEADS.put("6", "MHF_6");
        CARD_NUMBER_HEADS.put("7", "MHF_7");
        CARD_NUMBER_HEADS.put("8", "MHF_8");
        CARD_NUMBER_HEADS.put("9", "MHF_9");
        CARD_NUMBER_HEADS.put("10", "MHF_Exclamation");
        CARD_NUMBER_HEADS.put("J", "MHF_Chest"); // Jack
        CARD_NUMBER_HEADS.put("Q", "MHF_Cake"); // Queen
        CARD_NUMBER_HEADS.put("K", "MHF_TNT"); // King
    }

    public BlackjackGameGUI(GamblingPlugin plugin) {
        this.plugin = plugin;
    }

    public void openGame(Player player) {
        BlackjackGame game = plugin.getBlackjackManager().getPlayerGame(player);
        if (game == null)
            return;

        String title = TITLE_PREFIX + "Repartiendo...";
        Inventory inv = Bukkit.createInventory(null, 54, title);

        // Start with empty board
        fillBackground(inv);
        player.openInventory(inv);

        // Animate initial card dealing
        dealInitialCardsAnimation(game, player);
    }

    private void dealInitialCardsAnimation(BlackjackGame game, Player viewer) {
        List<Player> players = new ArrayList<>(game.getPlayers().keySet());

        new BukkitRunnable() {
            int cardIndex = 0;
            int round = 0; // 0 = first card, 1 = second card

            @Override
            public void run() {
                if (round >= 2) {
                    this.cancel();
                    // Done dealing, show game GUI
                    updateAllPlayers(game);
                    return;
                }

                // Deal one card to current player
                if (cardIndex < players.size()) {
                    Player currentPlayer = players.get(cardIndex);
                    viewer.playSound(viewer.getLocation(), Sound.ENTITY_ITEM_PICKUP, 0.5f, 1.2f + (cardIndex * 0.1f));
                    cardIndex++;
                } else {
                    // Deal to dealer
                    viewer.playSound(viewer.getLocation(), Sound.ENTITY_ITEM_PICKUP, 0.5f, 0.8f);
                    cardIndex = 0;
                    round++;
                }
            }
        }.runTaskTimer(plugin, 5L, 8L); // 0.4s between cards
    }

    private void updateGUI(Inventory inv, BlackjackGame game, Player viewer) {
        inv.clear();
        fillBackground(inv);

        // Dealer section (row 0-1)
        renderDealer(inv, game);

        // Players section (rows 2-5, max 4 players)
        List<Player> players = new ArrayList<>(game.getPlayers().keySet());
        for (int i = 0; i < players.size() && i < 4; i++) {
            renderPlayer(inv, game, players.get(i), i + 2, viewer);
        }

        // Update title based on game state
        if (game.getPhase() == BlackjackGame.GamePhase.DEALER_TURN) {
            ((org.bukkit.inventory.InventoryView) viewer.getOpenInventory()).setTitle(TITLE_PREFIX + "Dealer juega...");
        } else if (game.getPhase() == BlackjackGame.GamePhase.FINISHED) {
            ((org.bukkit.inventory.InventoryView) viewer.getOpenInventory()).setTitle(TITLE_PREFIX + "¡Finalizado!");
        }
    }

    private void fillBackground(Inventory inv) {
        ItemStack bg = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta bgMeta = bg.getItemMeta();
        bgMeta.setDisplayName(" ");
        bg.setItemMeta(bgMeta);
        for (int i = 0; i < 54; i++) {
            inv.setItem(i, bg);
        }
    }

    private void renderDealer(Inventory inv, BlackjackGame game) {
        boolean showAll = game.getPhase() == BlackjackGame.GamePhase.DEALER_TURN ||
                game.getPhase() == BlackjackGame.GamePhase.FINISHED;

        int dealerScore = showAll ? game.getDealerScore() : 0;

        // Dealer head at slot 0
        ItemStack dealerHead = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta skullMeta = (SkullMeta) dealerHead.getItemMeta();
        skullMeta.setOwner("Viega095");
        skullMeta.setDisplayName("§6§l♣ DEALER ♣" + (showAll ? " §7(§e" + dealerScore + "§7)" : ""));
        skullMeta.setLore(Arrays.asList(
                "",
                showAll ? "§7Puntaje: §e" + dealerScore : "§7Cartas ocultas...",
                ""));
        dealerHead.setItemMeta(skullMeta);
        inv.setItem(0, dealerHead);

        // Dealer cards starting at slot 1
        List<Card> dealerCards = game.getDealerHand();
        for (int i = 0; i < dealerCards.size() && i < 8; i++) {
            Card card = dealerCards.get(i);

            // Hide 2nd card until dealer's turn
            if (i == 1 && !showAll) {
                ItemStack hidden = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
                ItemMeta meta = hidden.getItemMeta();
                meta.setDisplayName("§8§l???");
                meta.setLore(Arrays.asList("§7Carta oculta"));
                hidden.setItemMeta(meta);
                inv.setItem(1 + i, hidden);
            } else {
                inv.setItem(1 + i, createEnhancedCardItem(card));
            }
        }
    }

    private void renderPlayer(Inventory inv, BlackjackGame game, Player player, int row, Player viewer) {
        int startSlot = row * 9;
        List<BlackjackGame.PlayerHand> hands = game.getPlayers().get(player);
        if (hands == null || hands.isEmpty())
            return;

        // Check if player has multiple hands (split)
        boolean hasSplit = hands.size() > 1;
        int currentHandIndex = game.getCurrentHandIndex();

        // Player status head (show combined info)
        ItemStack playerHead = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta skullMeta = (SkullMeta) playerHead.getItemMeta();
        skullMeta.setOwningPlayer(player);

        if (hasSplit) {
            skullMeta.setDisplayName("§d§l" + player.getName() + " §7(SPLIT)");
            skullMeta.setLore(Arrays.asList(
                    "",
                    "§7Manos: §d" + hands.size(),
                    "§7Mano 1: §e" + hands.get(0).getScore() + " §7(§a$" + String.format("%.0f", hands.get(0).getBet())
                            + "§7)",
                    "§7Mano 2: §e" + (hands.size() > 1 ? hands.get(1).getScore() : "?") + " §7(§a$"
                            + String.format("%.0f", hands.size() > 1 ? hands.get(1).getBet() : 0) + "§7)",
                    ""));
        } else {
            BlackjackGame.PlayerHand hand = hands.get(0);
            int score = hand.getScore();
            String statusIcon = "";
            String statusColor = "§e";

            if (hand.isBusted()) {
                statusIcon = "§c§l💀 BUST!";
                statusColor = "§c";
            } else if (hand.isBlackjack()) {
                statusIcon = "§6§l★ 21!";
                statusColor = "§6";
            } else if (hand.isStanding()) {
                statusIcon = "§7(✓ Stand)";
            }

            skullMeta.setDisplayName(statusColor + "§l" + player.getName() + " §7(§e" + score + "§7)");
            skullMeta.setLore(Arrays.asList(
                    "",
                    "§7Apuesta: §a$" + String.format("%.0f", hand.getBet()),
                    statusIcon.isEmpty() ? "" : statusIcon,
                    ""));
        }
        playerHead.setItemMeta(skullMeta);
        inv.setItem(startSlot, playerHead);

        // Display cards - if split, show both hands
        if (hasSplit) {
            // Hand 1 (slots 1-2)
            BlackjackGame.PlayerHand hand1 = hands.get(0);
            boolean isHand1Active = (currentHandIndex == 0 && player.equals(game.getCurrentPlayer()));

            for (int i = 0; i < Math.min(hand1.getCards().size(), 2); i++) {
                ItemStack card = createEnhancedCardItem(hand1.getCards().get(i));
                if (isHand1Active && i == 0) {
                    // Add glowing effect to first card of active hand
                    ItemMeta meta = card.getItemMeta();
                    List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
                    lore.add(0, "§d§l▶ MANO ACTIVA");
                    meta.setLore(lore);
                    card.setItemMeta(meta);
                }
                inv.setItem(startSlot + 1 + i, card);
            }

            // Hand 2 (slots 3-4)
            if (hands.size() > 1) {
                BlackjackGame.PlayerHand hand2 = hands.get(1);
                boolean isHand2Active = (currentHandIndex == 1 && player.equals(game.getCurrentPlayer()));

                for (int i = 0; i < Math.min(hand2.getCards().size(), 2); i++) {
                    ItemStack card = createEnhancedCardItem(hand2.getCards().get(i));
                    if (isHand2Active && i == 0) {
                        // Add glowing effect to first card of active hand
                        ItemMeta meta = card.getItemMeta();
                        List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
                        lore.add(0, "§d§l▶ MANO ACTIVA");
                        meta.setLore(lore);
                        card.setItemMeta(meta);
                    }
                    inv.setItem(startSlot + 3 + i, card);
                }
            }

            // Hand indicators
            ItemStack hand1Indicator = new ItemStack(Material.PAPER);
            ItemMeta meta1 = hand1Indicator.getItemMeta();
            meta1.setDisplayName(isHand1Active ? "§d§l▶ MANO 1" : "§7Mano 1");
            meta1.setLore(Arrays.asList(
                    "§7Puntos: §e" + hand1.getScore(),
                    hand1.isBusted() ? "§c§lBUST!" : (hand1.isBlackjack() ? "§6§l★ 21!" : ""),
                    isHand1Active ? "§d§lJugando ahora" : ""));
            hand1Indicator.setItemMeta(meta1);
            inv.setItem(startSlot + 5, hand1Indicator);

            if (hands.size() > 1) {
                BlackjackGame.PlayerHand hand2 = hands.get(1);
                boolean isHand2Active = (currentHandIndex == 1 && player.equals(game.getCurrentPlayer()));
                ItemStack hand2Indicator = new ItemStack(Material.PAPER);
                ItemMeta meta2 = hand2Indicator.getItemMeta();
                meta2.setDisplayName(isHand2Active ? "§d§l▶ MANO 2" : "§7Mano 2");
                meta2.setLore(Arrays.asList(
                        "§7Puntos: §e" + hand2.getScore(),
                        hand2.isBusted() ? "§c§lBUST!" : (hand2.isBlackjack() ? "§6§l★ 21!" : ""),
                        isHand2Active ? "§d§lJugando ahora" : ""));
                hand2Indicator.setItemMeta(meta2);
                inv.setItem(startSlot + 6, hand2Indicator);
            }
        } else {
            // Single hand - show normally (up to 5 cards)
            BlackjackGame.PlayerHand hand = hands.get(0);
            for (int i = 0; i < hand.getCards().size() && i < 5; i++) {
                inv.setItem(startSlot + 1 + i, createEnhancedCardItem(hand.getCards().get(i)));
            }
        }

        // Action buttons (only for current player and if it's their turn)
        boolean isCurrentPlayer = player.equals(game.getCurrentPlayer());
        boolean canAct = isCurrentPlayer && game.getPhase() == BlackjackGame.GamePhase.PLAYING;

        if (canAct && player.equals(viewer)) {
            // Get current hand for this player (use hands list directly)
            int handIndex = player.equals(game.getCurrentPlayer()) ? game.getCurrentHandIndex() : 0;
            BlackjackGame.PlayerHand hand = hands.get(handIndex);

            // HIT button
            ItemStack hitBtn = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
            ItemMeta hitMeta = hitBtn.getItemMeta();
            hitMeta.setDisplayName("§a§l⬇ HIT");
            hitMeta.setLore(Arrays.asList("", "§7Pedir otra carta", "", "§e§l▶ CLICK AQUÍ"));
            hitBtn.setItemMeta(hitMeta);
            inv.setItem(startSlot + (hasSplit ? 7 : 6), hitBtn);

            // STAND button
            ItemStack standBtn = new ItemStack(Material.RED_STAINED_GLASS_PANE);
            ItemMeta standMeta = standBtn.getItemMeta();
            standMeta.setDisplayName("§c§l✋ STAND");
            standMeta.setLore(Arrays.asList("", "§7Plantarte con", "§7tu mano actual", "", "§e§l▶ CLICK AQUÍ"));
            standBtn.setItemMeta(standMeta);
            inv.setItem(startSlot + (hasSplit ? 8 : 7), standBtn);

            // DOUBLE button (only if 2 cards)
            if (hand.getCards().size() == 2) {
                ItemStack doubleBtn = new ItemStack(Material.GOLD_INGOT);
                ItemMeta doubleMeta = doubleBtn.getItemMeta();
                doubleMeta.setDisplayName("§6§l⚡ DOUBLE DOWN");
                doubleMeta.setLore(
                        Arrays.asList("", "§7Doblar apuesta a", "§a§l$" + String.format("%.0f", hand.getBet() * 2),
                                "§7y tomar §eUNA carta", "", "§e§l▶ CLICK AQUÍ"));
                doubleBtn.setItemMeta(doubleMeta);
                inv.setItem(startSlot + 8, doubleBtn);

                // SPLIT button (only if can split)
                if (game.canSplit(player)) {
                    ItemStack splitBtn = new ItemStack(Material.SHEARS);
                    ItemMeta splitMeta = splitBtn.getItemMeta();
                    splitMeta.setDisplayName("§d§lSPLIT");
                    splitMeta.setLore(
                            Arrays.asList("", "§7Dividir par en 2 manos",
                                    "§7Costo: §a$" + String.format("%.0f", hand.getBet()),
                                    "",
                                    "§d§l▶ CLICK PARA SEPARAR"));
                    splitBtn.setItemMeta(splitMeta);
                    inv.setItem(startSlot + 6, splitBtn);
                }
            }
        } else if (isCurrentPlayer) {
            // Turn indicator
            ItemStack turnIndicator = new ItemStack(Material.YELLOW_STAINED_GLASS_PANE);
            ItemMeta meta = turnIndicator.getItemMeta();
            meta.setDisplayName("§e§l⟳ SU TURNO ⟳");
            turnIndicator.setItemMeta(meta);
            inv.setItem(startSlot + 7, turnIndicator);
        }
    }

    private ItemStack createEnhancedCardItem(Card card) {
        // Use player head for card number
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) item.getItemMeta();

        // Set skull owner based on card rank
        String headOwner = CARD_NUMBER_HEADS.get(card.getRank().getDisplayName());
        if (headOwner != null) {
            meta.setOwner(headOwner);
        }

        // Color based on suit
        String suitColor = (card.getSuit() == Card.Suit.HEARTS || card.getSuit() == Card.Suit.DIAMONDS) ? "§c" : "§0";
        String suitSymbol = card.getSuit().getSymbol();

        meta.setDisplayName(suitColor + "§l" + card.getRank().getDisplayName() + suitSymbol);
        meta.setLore(Arrays.asList(
                "",
                "§7Valor: §e" + card.getValue(),
                "§7Palo: " + suitColor + card.getSuit().name(),
                ""));
        item.setItemMeta(meta);
        return item;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        String title = event.getView().getTitle();
        if (!title.startsWith(TITLE_PREFIX.substring(0, 15))) { // Check for "♠ BLACKJACK ♥"
            return;
        }

        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        BlackjackGame game = plugin.getBlackjackManager().getPlayerGame(player);

        if (game == null || event.getCurrentItem() == null) {
            return;
        }

        ItemStack clicked = event.getCurrentItem();
        String displayName = clicked.hasItemMeta() ? clicked.getItemMeta().getDisplayName() : "";

        // Action buttons
        if (displayName.contains("HIT")) {
            game.hit(player);
            player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1f, 1.3f);
            updateAllPlayers(game);
            checkGameEnd(game);
        } else if (displayName.contains("STAND")) {
            game.stand(player);
            player.playSound(player.getLocation(), Sound.BLOCK_LEVER_CLICK, 1f, 1.2f);
            updateAllPlayers(game);
            checkGameEnd(game);
        } else if (displayName.contains("DOUBLE")) {
            BlackjackGame.PlayerHand hand = game.getPlayers().get(player).get(0);
            if (plugin.getEconomyManager().has(player, hand.getBet())) {
                plugin.getEconomyManager().withdraw(player, hand.getBet());
                game.doubleDown(player);
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.5f);
                updateAllPlayers(game);
                checkGameEnd(game);
            } else {
                player.sendMessage("§c[Blackjack] No tienes suficiente dinero para doblar.");
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            }
        } else if (displayName.contains("SPLIT")) {
            // Handle SPLIT
            BlackjackGame.PlayerHand hand = game.getPlayers().get(player).get(0);

            // Double check can still split
            if (!game.canSplit(player)) {
                player.sendMessage("§c[Blackjack] No puedes hacer split en este momento.");
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                return;
            }

            // Check balance
            if (plugin.getEconomyManager().has(player, hand.getBet())) {
                plugin.getEconomyManager().withdraw(player, hand.getBet());
                game.split(player);

                player.sendMessage("§d[Blackjack] ¡Par dividido! Ahora tienes 2 manos separadas.");
                player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 0.6f, 2.0f);

                updateAllPlayers(game);
            } else {
                player.sendMessage("§c[Blackjack] No tienes suficiente dinero para hacer split.");
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            }
        }
    }

    private void updateAllPlayers(BlackjackGame game) {
        for (Player player : game.getPlayers().keySet()) {
            if (player.getOpenInventory().getTitle().contains("BLACKJACK")) {
                updateGUI(player.getOpenInventory().getTopInventory(), game, player);
            }
        }
    }

    private void checkGameEnd(BlackjackGame game) {
        if (game.getPhase() == BlackjackGame.GamePhase.FINISHED) {
            // Game ended, show results after delay
            new BukkitRunnable() {
                @Override
                public void run() {
                    processResults(game);
                }
            }.runTaskLater(plugin, 40L);
        } else if (game.getPhase() == BlackjackGame.GamePhase.DEALER_TURN) {
            // Dealer is playing, update GUI
            updateAllPlayers(game);

            // Animate dealer drawing cards
            new BukkitRunnable() {

                @Override
                public void run() {
                    processResults(game);
                }

            }.runTaskLater(plugin, 60L);
        }
    }

    private void processResults(BlackjackGame game) {
        Map<Player, Double> winnings = game.calculateWinnings();

        for (Map.Entry<Player, Double> entry : winnings.entrySet()) {
            Player player = entry.getKey();
            double amount = entry.getValue();
            BlackjackGame.PlayerHand hand = game.getPlayers().get(player).get(0);

            // Pay winnings
            if (amount > 0) {
                plugin.getEconomyManager().deposit(player, amount);
            }

            // Stats
            double profit = amount - hand.getBet();
            if (profit > 0) {
                plugin.getStatsManager().addWin(player, profit);
            } else if (profit < 0) {
                plugin.getStatsManager().addLoss(player, -profit);
            }

            // Messages
            player.closeInventory();
            player.sendMessage("");
            player.sendMessage("§0§l♠§c§l♥§6§l══════════ BLACKJACK ══════════§c§l♥§0§l♠");
            player.sendMessage("§7Tu mano: §e" + hand.getScore()
                    + (hand.isBusted() ? " §c§l[BUST!]" : hand.isBlackjack() ? " §6§l[21!]" : ""));
            player.sendMessage("§7Dealer: §e" + game.getDealerScore());
            player.sendMessage("");

            if (hand.isBusted()) {
                player.sendMessage("§c§l💀 ¡TE PASASTE! §cPerdiste $" + String.format("%.0f", hand.getBet()));
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 0.8f);
            } else if (hand.isBlackjack() && game.getDealerScore() != 21) {
                player.sendMessage("§6§l★ ¡BLACKJACK! §aGanaste §l$" + String.format("%.0f", profit));
                player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1.3f);
            } else if (profit > 0) {
                player.sendMessage("§a§l✓ ¡GANASTE! §a+$" + String.format("%.0f", profit));
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.2f);
            } else if (profit == 0) {
                player.sendMessage("§e§l= EMPATE §7- Apuesta devuelta");
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1f);
            } else {
                player.sendMessage("§c§l✗ PERDISTE §c$" + String.format("%.0f", hand.getBet()));
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
            }

            player.sendMessage("§6§l═══════════════════════════════════");
            player.sendMessage("");

            // Jackpot check
            if (profit > 0 && plugin.getJackpotManager().tryWinJackpot(player, hand.getBet())) {
                player.sendMessage("§6§l★★★ ¡BONUS JACKPOT! ★★★");
            }

            // Contribute to jackpot
            plugin.getJackpotManager().contributeBet(hand.getBet());
        }

        // End game
        plugin.getBlackjackManager().endGame(game);
    }
}
