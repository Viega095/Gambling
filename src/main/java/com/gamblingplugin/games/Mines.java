package com.gamblingplugin.games;

import com.gamblingplugin.GamblingPlugin;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.*;

public class Mines extends Game {

    private final Map<UUID, MinesSession> activeSessions = new HashMap<>();

    public Mines(GamblingPlugin plugin) {
        super(plugin, "mines");
    }

    @Override
    public void play(Player player, double bet, String[] args) {
        // Called from betting GUI - redirect to our custom play method
        play(player, bet);
    }

    public void play(Player player, double bet) {
        // Validation
        if (!plugin.getEconomyManager().has(player, bet)) {
            player.sendMessage("§c[VieGambling] No tienes suficiente dinero!");
            return;
        }

        if (bet < getMinBet() || bet > getMaxBet()) {
            player.sendMessage("§c[VieGambling] Apuesta debe estar entre $" + String.format("%.2f", getMinBet())
                    + " y $" + String.format("%.2f", getMaxBet()));
            return;
        }

        // Withdraw bet
        plugin.getEconomyManager().withdraw(player, bet);

        // Create new session
        MinesSession session = new MinesSession(player.getUniqueId(), bet);
        activeSessions.put(player.getUniqueId(), session);

        // Open GUI
        player.sendMessage("");
        player.sendMessage("§6§l[VieGambling] Mines");
        player.sendMessage("§7Apuesta: §a$" + String.format("%.2f", bet));
        player.sendMessage("§7Minas: §c3");
        player.sendMessage("§7¡Revela casillas sin tocar minas!");
        player.sendMessage("");

        // Open Mines GUI
        com.gamblingplugin.gui.MinesGUI gui = new com.gamblingplugin.gui.MinesGUI(plugin);
        gui.open(player, session);
    }

    public MinesSession getSession(UUID playerId) {
        return activeSessions.get(playerId);
    }

    public void revealTile(Player player, int position) {
        MinesSession session = activeSessions.get(player.getUniqueId());
        if (session == null) {
            return;
        }

        // Check if already revealed
        if (session.revealedTiles.contains(position)) {
            return;
        }

        // Reveal tile
        session.revealedTiles.add(position);

        // Check if mine
        if (session.minePositions.contains(position)) {
            // LOSS - Hit mine
            handleLoss(player, session);
        } else {
            // Safe tile
            session.updateMultiplier();
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING,
                    1.0f, 1.0f + (session.revealedTiles.size() * 0.1f));

            // Check if won (all safe tiles revealed)
            if (session.revealedTiles.size() == 22) { // 25 - 3 mines = 22
                handleWin(player, session, true);
            }
        }
    }

    public void cashOut(Player player) {
        MinesSession session = activeSessions.get(player.getUniqueId());
        if (session == null) {
            return;
        }

        if (session.revealedTiles.isEmpty()) {
            player.sendMessage("§c[VieGambling] ¡Debes revelar al menos una casilla!");
            return;
        }

        handleWin(player, session, false);
    }

    private void handleWin(Player player, MinesSession session, boolean maxWin) {
        double winnings = session.bet * session.currentMultiplier;

        // Deposit winnings
        plugin.getEconomyManager().deposit(player, winnings);

        // Stats
        plugin.getStatsManager().addWin(player, winnings);

        // Try jackpot (integrated)
        boolean wonJackpot = plugin.getJackpotManager().tryWinJackpot(player, session.bet);
        if (wonJackpot) {
            // Jackpot is already paid and announced by tryWinJackpot
        }

        // Messages
        player.sendMessage("");
        player.sendMessage("§a§l[VieGambling] " + (maxWin ? "¡GANASTE TODO!" : "CASH OUT"));
        player.sendMessage("§7Casillas reveladas: §e" + session.revealedTiles.size());
        player.sendMessage("§7Multiplicador: §6" + String.format("%.2fx", session.currentMultiplier));
        player.sendMessage("§7Ganancia: §a$" + String.format("%.2f", winnings));
        player.sendMessage("");

        // Sound
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);

        // Remove session
        activeSessions.remove(player.getUniqueId());
    }

    private void handleLoss(Player player, MinesSession session) {
        // Stats
        plugin.getStatsManager().addLoss(player, session.bet);

        // Contribute to jackpot
        plugin.getJackpotManager().contributeBet(session.bet);

        // Find nearest structure and play explosion
        com.gamblingplugin.structures.MinesStructure structure = plugin.getStructureManager()
                .getMinesStructure(player.getLocation());
        if (structure != null) {
            structure.playExplosionEffect();
        }

        // Messages
        player.sendMessage("");
        player.sendMessage("§c§l[VieGambling] ¡BOOM! Mina encontrada");
        player.sendMessage("§7Casillas reveladas: §e" + (session.revealedTiles.size() - 1));
        player.sendMessage("§7Pérdida: §c$" + String.format("%.2f", session.bet));
        player.sendMessage("");

        // Sound
        player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.0f);

        // Remove session
        activeSessions.remove(player.getUniqueId());
    }

    public void endSession(UUID playerId) {
        activeSessions.remove(playerId);
    }

    // Inner class for game session
    public static class MinesSession {
        public final UUID playerId;
        public final double bet;
        public final Set<Integer> minePositions;
        public final Set<Integer> revealedTiles;
        public double currentMultiplier;

        public MinesSession(UUID playerId, double bet) {
            this.playerId = playerId;
            this.bet = bet;
            this.minePositions = new HashSet<>();
            this.revealedTiles = new HashSet<>();
            this.currentMultiplier = 1.0;

            // Generate 3 random mine positions (0-24)
            Random random = new Random();
            while (minePositions.size() < 3) {
                minePositions.add(random.nextInt(25));
            }
        }

        public void updateMultiplier() {
            int revealed = revealedTiles.size();
            int totalTiles = 25;
            int mines = 3;
            int safeTiles = totalTiles - mines;
            int remainingSafe = safeTiles - revealed;

            // Exponential multiplier formula
            double base = (double) safeTiles / remainingSafe;
            currentMultiplier = Math.pow(base, 0.9);
        }
    }
}
