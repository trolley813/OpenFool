package ru.hyst329.openfool;

import java.util.Locale;

public class Card {
    private Suit suit;
    private Rank rank;

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

    public boolean beats(Card other, Suit trumpSuit) {
        int thisRankValue = (this.rank.getValue() + 11) % 13;
        int otherRankValue = (other.rank.getValue() + 11) % 13;
        if (this.suit == other.suit) {
            return thisRankValue >= otherRankValue;
        } else return this.suit == trumpSuit;
    }

    public String toString() {
        return String.format(Locale.ENGLISH, "%d%c", rank.getValue(),
                suit.name().toLowerCase().charAt(0));
    }
}
