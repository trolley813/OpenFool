package ru.hyst329.openfool

import java.util.Locale

class Card(internal val suit: Suit, internal val rank: Rank) {

    internal fun beats(other: Card, trumpSuit: Suit, deuceBeatsAce: Boolean): Boolean {
        val thisRankValue = (this.rank.value + 11) % 13
        val otherRankValue = (other.rank.value + 11) % 13
        if (this.suit === other.suit) {
            // deuce: (2 + 11) % 13 == 0, ace: (1 + 11) % 13 == 12
            return thisRankValue >= otherRankValue || (deuceBeatsAce && thisRankValue == 0 && otherRankValue == 12)
        } else
            return this.suit === trumpSuit
    }

    override fun toString(): String {
        return "${rank.value}${suit.name.toLowerCase()[0]}"
    }
}
