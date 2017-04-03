package ru.hyst329.openfool

import java.security.SecureRandom
import java.util.ArrayList
import java.util.Collections

/**
 * Created by main on 13.03.2017.
 * Licensed under MIT License.
 */

internal class Deck {
    var cards: ArrayList<Card>? = null
        private set

    init {
        this.cards = ArrayList<Card>()
        this.reset()
        this.shuffle()
    }

    private fun reset() {
        this.cards!!.clear()
        this.cards = ArrayList<Card>()
        for (r in Rank.values())
            for (s in Suit.values())
                cards!!.add(Card(s, r))
    }

    private fun shuffle() {
        val random = SecureRandom()
        Collections.shuffle(cards!!, random)
    }

    fun draw(): Card? {
        try {
            return cards!!.removeAt(cards!!.size - 1)
        } catch (e: IndexOutOfBoundsException) {
            return null
        }

    }
}
