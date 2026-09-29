package com.gamblingplugin.games.blackjack;

public class Card {

    private final Suit suit;
    private final Rank rank;

    public Card(Suit suit, Rank rank) {
        this.suit = suit;
        this.rank = rank;
    }

    public Suit getSuit() {
        return suit;
    }

    public Rank getRank() {
        return rank;
    }

    public int getValue() {
        return rank.getValue();
    }

    public String getDisplayName() {
        String color = (suit == Suit.HEARTS || suit == Suit.DIAMONDS) ? "§c" : "§0";
        return color + suit.getSymbol() + " " + rank.getDisplayName();
    }

    @Override
    public String toString() {
        return rank.getDisplayName() + " of " + suit.name();
    }

    public enum Suit {
        HEARTS("♥"),
        DIAMONDS("♦"),
        CLUBS("♣"),
        SPADES("♠");

        private final String symbol;

        Suit(String symbol) {
            this.symbol = symbol;
        }

        public String getSymbol() {
            return symbol;
        }
    }

    public enum Rank {
        TWO("2", 2),
        THREE("3", 3),
        FOUR("4", 4),
        FIVE("5", 5),
        SIX("6", 6),
        SEVEN("7", 7),
        EIGHT("8", 8),
        NINE("9", 9),
        TEN("10", 10),
        JACK("J", 10),
        QUEEN("Q", 10),
        KING("K", 10),
        ACE("A", 11);

        private final String displayName;
        private final int value;

        Rank(String displayName, int value) {
            this.displayName = displayName;
            this.value = value;
        }

        public String getDisplayName() {
            return displayName;
        }

        public int getValue() {
            return value;
        }
    }
}
