package com.gamblingplugin.games;

import com.gamblingplugin.GamblingPlugin;
import com.gamblingplugin.engine.DisplayEntityEngine;
import org.bukkit.*;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.joml.Vector3f;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PhysicalBlackjackTable {

    public static class Seat {
        public final int seatIndex;
        public final Location seatLocation;
        public final Location cardArea;
        public Player occupant;
        public double currentBet = 0;
        public final List<String> hand = new ArrayList<>();
        public final List<ItemDisplay> cardDisplays = new ArrayList<>();

        public Seat(int seatIndex, Location seatLocation, Location cardArea) {
            this.seatIndex = seatIndex;
            this.seatLocation = seatLocation;
            this.cardArea = cardArea;
        }
    }

    private final GamblingPlugin plugin;
    private final Location tableCenter;
    private final List<Seat> seats = new ArrayList<>();
    private final List<String> dealerHand = new ArrayList<>();
    private final List<ItemDisplay> dealerCardDisplays = new ArrayList<>();
    private boolean roundInProgress = false;

    public PhysicalBlackjackTable(GamblingPlugin plugin, Location tableCenter) {
        this.plugin = plugin;
        this.tableCenter = tableCenter;
        setupSeats();
    }

    private void setupSeats() {
        for (int i = 0; i < 4; i++) {
            double angle = Math.PI / 4 * (i + 1);
            Location seatLoc = tableCenter.clone().add(Math.cos(angle) * 2.2, 0, Math.sin(angle) * 2.2);
            Location cardLoc = tableCenter.clone().add(Math.cos(angle) * 1.2, 0.8, Math.sin(angle) * 1.2);
            seats.add(new Seat(i + 1, seatLoc, cardLoc));
        }
    }

    public boolean sitPlayer(Player player, int seatNumber) {
        if (seatNumber < 1 || seatNumber > seats.size()) return false;
        Seat seat = seats.get(seatNumber - 1);
        if (seat.occupant != null) {
            player.sendMessage(ChatColor.RED + "✖ Este asiento ya está ocupado.");
            return false;
        }

        seat.occupant = player;
        player.teleport(seat.seatLocation);
        player.sendMessage(ChatColor.GOLD + "♠ [Mesa de Blackjack] Te has sentado en el Asiento #" + seatNumber);
        return true;
    }

    public void dealCardToSeat(Seat seat, String cardName, Material cardMaterial) {
        seat.hand.add(cardName);
        Location spawnLoc = tableCenter.clone().add(0, 1.2, 0);
        Location targetLoc = seat.cardArea.clone().add((seat.hand.size() - 1) * 0.25, 0, 0);

        ItemDisplay cardDisplay = DisplayEntityEngine.spawnItemDisplay(spawnLoc, new ItemStack(cardMaterial), new Vector3f(0.35f, 0.35f, 0.05f));
        seat.cardDisplays.add(cardDisplay);

        DisplayEntityEngine.interpolateTransformation(cardDisplay, new Vector3f(0.35f, 0.35f, 0.05f), 10);
        cardDisplay.teleport(targetLoc);

        if (seat.occupant != null) {
            seat.occupant.sendMessage(ChatColor.GREEN + "🎴 Has recibido: " + ChatColor.YELLOW + cardName);
            seat.occupant.playSound(seat.occupant.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 1f, 1.4f);
        }
    }

    public void clearTable() {
        for (Seat seat : seats) {
            for (ItemDisplay display : seat.cardDisplays) {
                if (display.isValid()) display.remove();
            }
            seat.cardDisplays.clear();
            seat.hand.clear();
            seat.currentBet = 0;
        }
        for (ItemDisplay display : dealerCardDisplays) {
            if (display.isValid()) display.remove();
        }
        dealerCardDisplays.clear();
        dealerHand.clear();
        roundInProgress = false;
    }
}
