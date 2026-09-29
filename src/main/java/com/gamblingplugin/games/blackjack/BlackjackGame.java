package com.gamblingplugin.games.blackjack;

import org.bukkit.entity.Player;

import java.util.*;

public class BlackjackGame {

    private final UUID gameId;
    private final Map<Player, List<PlayerHand>> players; // Support multiple hands per player
    private final List<Card> dealerHand;
    private final Deck deck;
    private GamePhase phase;
    private int currentPlayerIndex;
    private int currentHandIndex; // Track which hand of current player
    private final List<Player> playerOrder;

    public BlackjackGame() {
        this.gameId = UUID.randomUUID();
        this.players = new LinkedHashMap<>();
        this.dealerHand = new ArrayList<>();
        this.deck = new Deck();
        this.phase = GamePhase.BETTING;
        this.currentPlayerIndex = 0;
        this.currentHandIndex = 0;
        this.playerOrder = new ArrayList<>();
    }

    public void addPlayer(Player player, double bet) {
        if (players.size() >= 4) {
            throw new IllegalStateException("Game is full!");
        }
        List<PlayerHand> hands = new ArrayList<>();
        hands.add(new PlayerHand(bet));
        players.put(player, hands);
        playerOrder.add(player);
    }

    public void startGame() {
        if (players.isEmpty()) {
            throw new IllegalStateException("No players in game!");
        }

        phase = GamePhase.PLAYING;

        // Deal 2 cards to each player's first hand
        for (int i = 0; i < 2; i++) {
            for (Player player : playerOrder) {
                players.get(player).get(0).addCard(deck.draw());
            }
            // Deal to dealer
            dealerHand.add(deck.draw());
        }

        currentPlayerIndex = 0;
        currentHandIndex = 0;
    }

    public void hit(Player player) {
        PlayerHand hand = getCurrentHand(player);
        if (hand == null || hand.isStanding())
            return;

        hand.addCard(deck.draw());

        if (hand.getScore() > 21) {
            hand.setBusted(true);
            hand.setStanding(true);
            nextHand();
        }
    }

    public void stand(Player player) {
        PlayerHand hand = getCurrentHand(player);
        if (hand == null)
            return;

        hand.setStanding(true);
        nextHand();
    }

    public void doubleDown(Player player) {
        PlayerHand hand = getCurrentHand(player);
        if (hand == null || hand.getCards().size() != 2)
            return;

        hand.setBet(hand.getBet() * 2);
        hand.addCard(deck.draw());
        hand.setStanding(true);

        if (hand.getScore() > 21) {
            hand.setBusted(true);
        }

        nextHand();
    }

    // NEW: Check if player can split current hand
    public boolean canSplit(Player player) {
        PlayerHand hand = getCurrentHand(player);
        if (hand == null || hand.getCards().size() != 2) {
            return false;
        }

        // Check if both cards have same rank
        Card card1 = hand.getCards().get(0);
        Card card2 = hand.getCards().get(1);
        return card1.getRank() == card2.getRank();
    }

    // NEW: Split current hand into two hands
    public void split(Player player) {
        if (!canSplit(player)) {
            return;
        }

        List<PlayerHand> hands = players.get(player);
        PlayerHand originalHand = hands.get(currentHandIndex);

        // Create new hand with same bet
        PlayerHand newHand = new PlayerHand(originalHand.getBet());
        newHand.setFromSplit(true);

        // Move second card to new hand
        Card secondCard = originalHand.getCards().remove(1);
        newHand.addCard(secondCard);

        // Deal one new card to each hand
        originalHand.addCard(deck.draw());
        newHand.addCard(deck.draw());

        // Mark original as split too
        originalHand.setFromSplit(true);

        // Insert new hand right after current hand
        hands.add(currentHandIndex + 1, newHand);
    }

    private PlayerHand getCurrentHand(Player player) {
        List<PlayerHand> hands = players.get(player);
        if (hands == null || currentHandIndex >= hands.size()) {
            return null;
        }
        return hands.get(currentHandIndex);
    }

    private void nextHand() {
        currentHandIndex++;

        // Check if current player has more hands
        if (currentHandIndex >= players.get(playerOrder.get(currentPlayerIndex)).size()) {
            // Move to next player
            currentPlayerIndex++;
            currentHandIndex = 0;

            if (currentPlayerIndex >= playerOrder.size()) {
                // All players done, dealer's turn
                phase = GamePhase.DEALER_TURN;
                dealerPlay();
            }
        }
    }

    private void dealerPlay() {
        // Dealer hits on 16 or less, stands on 17 or more
        while (calculateScore(dealerHand) < 17) {
            dealerHand.add(deck.draw());
        }

        phase = GamePhase.FINISHED;
    }

    public Map<Player, Double> calculateWinnings() {
        Map<Player, Double> winnings = new HashMap<>();
        int dealerScore = calculateScore(dealerHand);
        boolean dealerBust = dealerScore > 21;

        for (Map.Entry<Player, List<PlayerHand>> entry : players.entrySet()) {
            Player player = entry.getKey();
            List<PlayerHand> hands = entry.getValue();
            double totalWinnings = 0.0;

            for (PlayerHand hand : hands) {
                double bet = hand.getBet();
                int playerScore = hand.getScore();

                if (hand.isBusted()) {
                    // Player busted - loses
                    // No winnings
                } else if (hand.isBlackjack() && !hand.isFromSplit() && !isBlackjack(dealerHand)) {
                    // Natural blackjack (not from split), dealer doesn't - 2.5x payout
                    totalWinnings += bet * 2.5;
                } else if (dealerBust) {
                    // Dealer busted, player didn't - player wins
                    totalWinnings += bet * 2;
                } else if (playerScore > dealerScore) {
                    // Player has higher score - wins
                    totalWinnings += bet * 2;
                } else if (playerScore == dealerScore) {
                    // Push - return bet
                    totalWinnings += bet;
                }
                // Dealer wins - player loses (no winnings added)
            }

            winnings.put(player, totalWinnings);
        }

        return winnings;
    }

    private int calculateScore(List<Card> hand) {
        int score = 0;
        int aces = 0;

        for (Card card : hand) {
            if (card.getRank() == Card.Rank.ACE) {
                aces++;
            }
            score += card.getValue();
        }

        // Adjust for aces
        while (score > 21 && aces > 0) {
            score -= 10; // Convert ace from 11 to 1
            aces--;
        }

        return score;
    }

    private boolean isBlackjack(List<Card> hand) {
        return hand.size() == 2 && calculateScore(hand) == 21;
    }

    // Getters
    public UUID getGameId() {
        return gameId;
    }

    public Map<Player, List<PlayerHand>> getPlayers() {
        return players;
    }

    public List<Card> getDealerHand() {
        return dealerHand;
    }

    public GamePhase getPhase() {
        return phase;
    }

    public Player getCurrentPlayer() {
        if (currentPlayerIndex < playerOrder.size()) {
            return playerOrder.get(currentPlayerIndex);
        }
        return null;
    }

    public int getCurrentHandIndex() {
        return currentHandIndex;
    }

    public int getDealerScore() {
        return calculateScore(dealerHand);
    }

    public enum GamePhase {
        BETTING,
        PLAYING,
        DEALER_TURN,
        FINISHED
    }

    public static class PlayerHand {
        private final List<Card> cards;
        private double bet;
        private boolean standing;
        private boolean busted;
        private boolean fromSplit; // Track if this hand came from a split

        public PlayerHand(double bet) {
            this.cards = new ArrayList<>();
            this.bet = bet;
            this.standing = false;
            this.busted = false;
            this.fromSplit = false;
        }

        public void addCard(Card card) {
            cards.add(card);
        }

        public int getScore() {
            int score = 0;
            int aces = 0;

            for (Card card : cards) {
                if (card.getRank() == Card.Rank.ACE) {
                    aces++;
                }
                score += card.getValue();
            }

            while (score > 21 && aces > 0) {
                score -= 10;
                aces--;
            }

            return score;
        }

        public boolean isBlackjack() {
            return cards.size() == 2 && getScore() == 21;
        }

        public List<Card> getCards() {
            return cards;
        }

        public double getBet() {
            return bet;
        }

        public void setBet(double bet) {
            this.bet = bet;
        }

        public boolean isStanding() {
            return standing;
        }

        public void setStanding(boolean standing) {
            this.standing = standing;
        }

        public boolean isBusted() {
            return busted;
        }

        public void setBusted(boolean busted) {
            this.busted = busted;
        }

        public boolean isFromSplit() {
            return fromSplit;
        }

        public void setFromSplit(boolean fromSplit) {
            this.fromSplit = fromSplit;
        }
    }
}
