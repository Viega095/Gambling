package com.gamblingplugin.games;

import com.gamblingplugin.GamblingPlugin;
import com.gamblingplugin.games.blackjack.Card;
import com.gamblingplugin.games.blackjack.Deck;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.*;

public class TexasHoldemPoker extends Game {

    public enum RoundPhase {
        WAITING, PREFLOP, FLOP, TURN, RIVER, SHOWDOWN
    }

    public static class PokerPlayer {
        public final Player player;
        public final List<Card> holeCards = new ArrayList<>();
        public double currentBet = 0;
        public double totalContributed = 0;
        public boolean folded = false;
        public boolean allIn = false;

        public PokerPlayer(Player player) {
            this.player = player;
        }
    }

    private final List<PokerPlayer> seats = new ArrayList<>();
    private final List<Card> communityCards = new ArrayList<>();
    private Deck deck;
    private double pot = 0;
    private double currentHighestBet = 0;
    private int currentTurnIndex = 0;
    private RoundPhase phase = RoundPhase.WAITING;
    private final double smallBlind = 10.0;
    private final double bigBlind = 20.0;

    public TexasHoldemPoker(GamblingPlugin plugin) {
        super(plugin, "poker");
        this.deck = new Deck();
    }

    @Override
    public void play(Player player, double bet, String[] args) {
        if (args != null && args.length > 0) {
            String sub = args[0].toLowerCase();
            switch (sub) {
                case "join" -> joinTable(player);
                case "leave" -> leaveTable(player);
                case "fold" -> handleFold(player);
                case "check" -> handleCheck(player);
                case "call" -> handleCall(player);
                case "raise" -> {
                    double raiseAmt = args.length > 1 ? parseDouble(args[1], bigBlind) : bigBlind;
                    handleRaise(player, raiseAmt);
                }
                default -> player.sendMessage(ChatColor.GOLD + "Comandos de Poker: /gamble poker [join|leave|fold|check|call|raise]");
            }
            return;
        }
        joinTable(player);
    }

    public synchronized void joinTable(Player player) {
        if (getSeat(player) != null) {
            player.sendMessage(ChatColor.RED + "✖ Ya estás sentado en la mesa de Poker.");
            return;
        }

        if (seats.size() >= 6) {
            player.sendMessage(ChatColor.RED + "✖ La mesa de Poker está llena (máx 6 jugadores).");
            return;
        }

        if (!plugin.getEconomyManager().hasEnough(player, bigBlind * 5)) {
            player.sendMessage(ChatColor.RED + "✖ Necesitas al menos " + plugin.getEconomyManager().format(bigBlind * 5) + " para entrar.");
            return;
        }

        seats.add(new PokerPlayer(player));
        broadcast(ChatColor.GOLD + "♠ [Poker] " + ChatColor.YELLOW + player.getName() + ChatColor.GREEN + " se sentó en la mesa (" + seats.size() + "/6)");

        if (seats.size() >= 2 && phase == RoundPhase.WAITING) {
            startHand();
        }
    }

    public synchronized void leaveTable(Player player) {
        PokerPlayer seat = getSeat(player);
        if (seat == null) {
            player.sendMessage(ChatColor.RED + "✖ No estás en la mesa de Poker.");
            return;
        }

        seats.remove(seat);
        broadcast(ChatColor.GRAY + "♠ [Poker] " + player.getName() + " se levantó de la mesa.");

        if (seats.size() < 2 && phase != RoundPhase.WAITING) {
            broadcast(ChatColor.RED + "♠ [Poker] Mano cancelada: no hay suficientes jugadores.");
            resetTable();
        }
    }

    private void startHand() {
        phase = RoundPhase.PREFLOP;
        deck = new Deck();
        deck.shuffle();
        communityCards.clear();
        pot = 0;
        currentHighestBet = bigBlind;

        for (PokerPlayer p : seats) {
            p.holeCards.clear();
            p.folded = false;
            p.allIn = false;
            p.currentBet = 0;
            p.totalContributed = 0;

            p.holeCards.add(deck.dealCard());
            p.holeCards.add(deck.dealCard());

            p.player.sendMessage(ChatColor.GOLD + "♠ [Poker] Tus cartas: " +
                    ChatColor.YELLOW + p.holeCards.get(0).toString() + ChatColor.GRAY + " y " +
                    ChatColor.YELLOW + p.holeCards.get(1).toString());
            p.player.playSound(p.player.getLocation(), Sound.ITEM_ARMOR_EQUIP_LEATHER, 1f, 1f);
        }

        // Deduct Blinds
        if (seats.size() >= 2) {
            deductBet(seats.get(0), smallBlind);
            deductBet(seats.get(1), bigBlind);
            currentTurnIndex = (seats.size() > 2) ? 2 : 0;
        }

        broadcast(ChatColor.AQUA + "♠ [Poker] ¡Nueva mano iniciada! Bote actual: " + plugin.getEconomyManager().format(pot));
        notifyTurn();
    }

    private void deductBet(PokerPlayer p, double amount) {
        plugin.getEconomyManager().withdraw(p.player, amount);
        p.currentBet += amount;
        p.totalContributed += amount;
        pot += amount;
    }

    public synchronized void handleFold(Player player) {
        PokerPlayer seat = getSeat(player);
        if (seat == null || !isPlayerTurn(seat)) return;

        seat.folded = true;
        broadcast(ChatColor.RED + "♠ [Poker] " + player.getName() + " se retiró (Fold).");
        advanceTurn();
    }

    public synchronized void handleCheck(Player player) {
        PokerPlayer seat = getSeat(player);
        if (seat == null || !isPlayerTurn(seat)) return;

        if (seat.currentBet < currentHighestBet) {
            player.sendMessage(ChatColor.RED + "✖ No puedes pasar (Check), debes igualar la apuesta de " +
                    plugin.getEconomyManager().format(currentHighestBet));
            return;
        }

        broadcast(ChatColor.YELLOW + "♠ [Poker] " + player.getName() + " pasó (Check).");
        advanceTurn();
    }

    public synchronized void handleCall(Player player) {
        PokerPlayer seat = getSeat(player);
        if (seat == null || !isPlayerTurn(seat)) return;

        double needed = currentHighestBet - seat.currentBet;
        if (needed <= 0) {
            handleCheck(player);
            return;
        }

        if (!plugin.getEconomyManager().hasEnough(player, needed)) {
            player.sendMessage(ChatColor.RED + "✖ Fondos insuficientes para igualar.");
            return;
        }

        deductBet(seat, needed);
        broadcast(ChatColor.YELLOW + "♠ [Poker] " + player.getName() + " igualó la apuesta (+ " + plugin.getEconomyManager().format(needed) + ")");
        advanceTurn();
    }

    public synchronized void handleRaise(Player player, double raiseAmt) {
        PokerPlayer seat = getSeat(player);
        if (seat == null || !isPlayerTurn(seat)) return;

        double targetBet = currentHighestBet + raiseAmt;
        double needed = targetBet - seat.currentBet;

        if (!plugin.getEconomyManager().hasEnough(player, needed)) {
            player.sendMessage(ChatColor.RED + "✖ No tienes suficiente dinero para subir la apuesta.");
            return;
        }

        deductBet(seat, needed);
        currentHighestBet = targetBet;
        broadcast(ChatColor.GOLD + "♠ [Poker] " + player.getName() + " subió la apuesta a " + plugin.getEconomyManager().format(currentHighestBet) + "!");
        advanceTurn();
    }

    private void advanceTurn() {
        List<PokerPlayer> active = getActivePlayers();
        if (active.size() <= 1) {
            concludeHand();
            return;
        }

        currentTurnIndex = (currentTurnIndex + 1) % seats.size();
        while (seats.get(currentTurnIndex).folded) {
            currentTurnIndex = (currentTurnIndex + 1) % seats.size();
        }

        // Check if betting round completed
        boolean allMatched = true;
        for (PokerPlayer p : active) {
            if (p.currentBet < currentHighestBet) {
                allMatched = false;
                break;
            }
        }

        if (allMatched) {
            advancePhase();
        } else {
            notifyTurn();
        }
    }

    private void advancePhase() {
        for (PokerPlayer p : seats) {
            p.currentBet = 0;
        }
        currentHighestBet = 0;

        switch (phase) {
            case PREFLOP -> {
                phase = RoundPhase.FLOP;
                communityCards.add(deck.dealCard());
                communityCards.add(deck.dealCard());
                communityCards.add(deck.dealCard());
                broadcastCommunityCards("FLOP");
            }
            case FLOP -> {
                phase = RoundPhase.TURN;
                communityCards.add(deck.dealCard());
                broadcastCommunityCards("TURN");
            }
            case TURN -> {
                phase = RoundPhase.RIVER;
                communityCards.add(deck.dealCard());
                broadcastCommunityCards("RIVER");
            }
            case RIVER -> {
                phase = RoundPhase.SHOWDOWN;
                concludeHand();
                return;
            }
        }

        currentTurnIndex = 0;
        while (seats.get(currentTurnIndex).folded) {
            currentTurnIndex = (currentTurnIndex + 1) % seats.size();
        }
        notifyTurn();
    }

    private void broadcastCommunityCards(String phaseName) {
        StringBuilder sb = new StringBuilder();
        for (Card c : communityCards) {
            sb.append(ChatColor.WHITE).append("[").append(ChatColor.YELLOW).append(c.toString()).append(ChatColor.WHITE).append("] ");
        }
        broadcast(ChatColor.LIGHT_PURPLE + "♠ [Poker " + phaseName + "] " + sb.toString() + ChatColor.GOLD + "| Bote: " + plugin.getEconomyManager().format(pot));
    }

    private void concludeHand() {
        List<PokerPlayer> active = getActivePlayers();
        if (active.isEmpty()) {
            resetTable();
            return;
        }

        PokerPlayer winner = active.get(0); // Simplified high showdown
        plugin.getEconomyManager().deposit(winner.player, pot);
        plugin.getStatsManager().addWin(winner.player, pot);

        broadcast(ChatColor.GREEN + "👑 [Poker] ¡" + winner.player.getName() + " gana el bote de " +
                plugin.getEconomyManager().format(pot) + "!");
        winner.player.playSound(winner.player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
        winner.player.spawnParticle(Particle.TOTEM, winner.player.getLocation().add(0, 1, 0), 30, 0.4, 0.4, 0.4, 0.1);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (seats.size() >= 2) {
                startHand();
            } else {
                resetTable();
            }
        }, 100L);
    }

    private void notifyTurn() {
        PokerPlayer current = seats.get(currentTurnIndex);
        broadcast(ChatColor.AQUA + "⏳ Turno de: " + ChatColor.YELLOW + current.player.getName() +
                ChatColor.GRAY + " (Bote: " + plugin.getEconomyManager().format(pot) + ")");
        current.player.playSound(current.player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1.2f);
    }

    private boolean isPlayerTurn(PokerPlayer p) {
        return seats.indexOf(p) == currentTurnIndex;
    }

    private PokerPlayer getSeat(Player player) {
        for (PokerPlayer p : seats) {
            if (p.player.equals(player)) return p;
        }
        return null;
    }

    private List<PokerPlayer> getActivePlayers() {
        List<PokerPlayer> list = new ArrayList<>();
        for (PokerPlayer p : seats) {
            if (!p.folded) list.add(p);
        }
        return list;
    }

    private void resetTable() {
        phase = RoundPhase.WAITING;
        pot = 0;
        communityCards.clear();
    }

    private void broadcast(String msg) {
        for (PokerPlayer p : seats) {
            p.player.sendMessage(msg);
        }
    }

    private double parseDouble(String s, double fallback) {
        try {
            return Double.parseDouble(s);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }
}
