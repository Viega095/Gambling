package com.gamblingplugin.games.blackjack;

import org.bukkit.entity.Player;

import java.util.*;

public class BlackjackManager {

    private final Map<UUID, BlackjackGame> activeGames;
    private final Map<Player, BlackjackGame> playerGames;
    private final Map<UUID, BlackjackLobby> activeLobbies;

    public BlackjackManager() {
        this.activeGames = new HashMap<>();
        this.playerGames = new HashMap<>();
        this.activeLobbies = new HashMap<>();
    }

    public BlackjackLobby createLobby(Player host, double bet) {
        // Remove from existing lobby/game
        removePlayer(host);

        UUID lobbyId = UUID.randomUUID();
        BlackjackLobby lobby = new BlackjackLobby(lobbyId, host, bet);
        activeLobbies.put(lobbyId, lobby);

        return lobby;
    }

    public void startGame(BlackjackLobby lobby) {
        BlackjackGame game = new BlackjackGame();

        // Add all players from lobby
        for (Map.Entry<Player, Double> entry : lobby.getPlayers().entrySet()) {
            game.addPlayer(entry.getKey(), entry.getValue());
            playerGames.put(entry.getKey(), game);
        }

        game.startGame();
        activeGames.put(game.getGameId(), game);
        activeLobbies.remove(lobby.getLobbyId());
    }

    public BlackjackGame getPlayerGame(Player player) {
        return playerGames.get(player);
    }

    public void removePlayer(Player player) {
        playerGames.remove(player);
    }

    public void endGame(BlackjackGame game) {
        activeGames.remove(game.getGameId());
        for (Player player : game.getPlayers().keySet()) {
            playerGames.remove(player);
        }
    }

    public static class BlackjackLobby {
        private final UUID lobbyId;
        private final Player host;
        private final Map<Player, Double> players;
        private final long startTime;

        public BlackjackLobby(UUID lobbyId, Player host, double bet) {
            this.lobbyId = lobbyId;
            this.host = host;
            this.players = new LinkedHashMap<>();
            this.players.put(host, bet);
            this.startTime = System.currentTimeMillis();
        }

        public void addPlayer(Player player, double bet) {
            if (players.size() >= 4) {
                throw new IllegalStateException("Lobby is full!");
            }
            players.put(player, bet);
        }

        public boolean isExpired() {
            return System.currentTimeMillis() - startTime > 5000; // 5 seconds
        }

        public long getTimeRemaining() {
            long elapsed = System.currentTimeMillis() - startTime;
            return Math.max(0, 5000 - elapsed);
        }

        public UUID getLobbyId() {
            return lobbyId;
        }

        public Player getHost() {
            return host;
        }

        public Map<Player, Double> getPlayers() {
            return players;
        }
    }
}
