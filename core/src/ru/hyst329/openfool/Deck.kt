package ru.hyst329.openfool

import java.security.SecureRandom
import java.util.ArrayList
import java.util.Collections

/**
 * Created by main on 13.03.2017.
 * Licensed under MIT License.
 */

internal class Deck(private val lowestRank: Rank = Rank.TWO) {
    var cards: ArrayList<Card>? = null
        private set

    init {
        this.cards = ArrayList()
        this.reset()
        this.shuffle()
    }

    private fun reset() {
        this.cards!!.clear()
        this.cards = ArrayList()
        for (r in Rank.values())
            for (s in Suit.values())
                if (r >= lowestRank || r == Rank.ACE)
                    cards!!.add(Card(s, r))
    }

    private fun shuffle() {
        val random = SecureRandom()
        cards!!.shuffle(random)
    }

    fun draw(): Card? {
        return try {
            cards!!.removeAt(cards!!.size - 1)
        } catch (e: IndexOutOfBoundsException) {
            null
        }

    }
}
