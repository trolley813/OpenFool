package ru.hyst329.openfool

import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.Event

import java.util.ArrayList
import java.util.Arrays
import java.util.Collections
import java.util.Comparator

/**
 * Created by main on 13.03.2017.
 * Licensed under MIT License.
 */

class Player internal constructor(private val ruleSet: RuleSet, private var name: String?, val index: Int) : Actor() {
    val hand = ArrayList<Card>()

    internal inner class CardThrownEvent(val card: Card) : Event()

    internal inner class CardBeatenEvent(val card: Card) : Event()

    internal inner class DoneEvent : Event()

    internal inner class TakeEvent : Event()

    enum class SortingMode constructor(val value: Int) {
        UNSORTED(0),
        SUIT_ASCENDING(1),
        SUIT_DESCENDING(2),
        RANK_ASCENDING(3),
        RANK_DESCENDING(4);


        companion object {

            fun fromInt(value: Int, default: SortingMode = UNSORTED): SortingMode {
                for (type in SortingMode.values()) {
                    if (type.value == value) {
                        return type
                    }
                }
                return default
            }
        }
    }

    private fun handValue(hand: ArrayList<Card>, trumpSuit: Suit, cardsRemaining: Int, playerHands: Array<Int>): Int {

        if (cardsRemaining == 0 && hand.size == 0) {
            return OUT_OF_PLAY
        }
        val bonuses = doubleArrayOf(0.0, 0.0, 0.5, 0.75, 1.25) // for cards of same rank
        var res = 0
        val countsByRank = IntArray(13)
        val countsBySuit = IntArray(4)
        for (c in hand) {
            val r = c.rank
            val s = c.suit
            res += (if (r === Rank.ACE) 6 else r.value - 8) * RANK_MULTIPLIER
            if (s === trumpSuit)
                res += 13 * RANK_MULTIPLIER
            countsByRank[r.value - 1]++
            countsBySuit[s.value]++
        }
        for (i in 1..13) {
            res += (Math.max(if (i == 1) 6 else i - 8, 1) * bonuses[countsByRank[i - 1]]).toInt()
        }
        var avgSuit = 0.0
        for (c in hand) {
            if (c.suit !== trumpSuit)
                avgSuit++
        }
        avgSuit /= 3.0
        for (s in Suit.values()) {
            if (s !== trumpSuit) {
                val dev = Math.abs((countsBySuit[s.value] - avgSuit) / avgSuit)
                res -= (UNBALANCED_HAND_PENALTY * dev).toInt()
            }
        }
        var cardsInPlay = cardsRemaining
        for (p in playerHands)
            cardsInPlay += p
        cardsInPlay -= hand.size
        val cardRatio = if (cardsInPlay != 0) (hand.size / cardsInPlay).toDouble() else 10.0
        res += ((0.25 - cardRatio) * MANY_CARDS_PENALTY).toInt()
        return res
    }

    private fun currentHandValue(trumpSuit: Suit, cardsRemaining: Int, playerHands: Array<Int>): Int {
        return handValue(hand, trumpSuit, cardsRemaining, playerHands)
    }


    override fun getName(): String {
        return name ?: ""
    }

    override fun setName(name: String) {
        this.name = name
    }

    fun addCard(c: Card) {
        hand.add(c)
    }

    fun startTurn(trumpSuit: Suit,
                  cardsRemaining: Int,
                  playerHands: Array<Int>) {
        val bonuses = doubleArrayOf(0.0, 0.0, 1.0, 1.5, 2.5)
        val countsByRank = IntArray(13)
        for (c in hand) {
            countsByRank[c.rank.value - 1]++
        }
        var maxVal = Integer.MIN_VALUE
        var cardIdx = -1
        for (i in hand.indices) {
            val newHand = ArrayList(hand)
            val c = hand[i]
            newHand.removeAt(i)
            val r = c.rank
            val newVal = handValue(newHand, trumpSuit, cardsRemaining, playerHands) + Math.round(bonuses[countsByRank[r.value - 1]] * (if (r === Rank.ACE) 6 else r.value - 8).toDouble() * RANK_MULTIPLIER.toDouble()).toInt()
            if (newVal > maxVal) {
                maxVal = newVal
                cardIdx = i
            }
        }
        val c = hand[cardIdx]
        hand.removeAt(cardIdx)
        fire(CardThrownEvent(c))
    }

    fun throwOrDone(attackCards: Array<Card?>,
                    defenseCards: Array<Card?>,
                    trumpSuit: Suit,
                    cardsRemaining: Int,
                    playerHands: Array<Int>
                    ) {
        val ranksPresent = BooleanArray(13)
        for (c in attackCards) {
            if (c != null)
                ranksPresent[c.rank.value - 1] = true
        }
        for (c in defenseCards) {
            if (c != null)
                ranksPresent[c.rank.value - 1] = true
        }
        // TODO: Remove duplication
        val bonuses = doubleArrayOf(0.0, 0.0, 1.0, 1.5, 2.5)
        val countsByRank = IntArray(13)
        for (c in hand) {
            countsByRank[c.rank.value - 1]++
        }
        var maxVal = Integer.MIN_VALUE
        var cardIdx = -1
        for (i in hand.indices) {
            val c = hand[i]
            val r = c.rank
            if (!ranksPresent[r.value - 1])
                continue
            val newHand = ArrayList(hand)
            newHand.removeAt(i)
            val newVal = handValue(newHand, trumpSuit, cardsRemaining, playerHands) + Math.round(bonuses[countsByRank[r.value - 1]] * (if (r === Rank.ACE) 6 else r.value - 8).toDouble() * RANK_MULTIPLIER.toDouble()).toInt()
            if (newVal > maxVal) {
                maxVal = newVal
                cardIdx = i
            }
        }
        val PENALTY_BASE = 1200
        val PENALTY_DELTA = 50
        if (currentHandValue(trumpSuit, cardsRemaining, playerHands) - maxVal <
                PENALTY_BASE - PENALTY_DELTA * cardsRemaining && cardIdx >= 0) {
            val c = hand[cardIdx]
            hand.removeAt(cardIdx)
            fire(CardThrownEvent(c))
        } else {
            fire(DoneEvent())
        }
    }


    fun tryBeat(attackCards: Array<Card?>,
                defenseCards: Array<Card?>,
                trumpSuit: Suit,
                cardsRemaining: Int,
                playerHands: Array<Int>) {
        val RANK_PRESENT_BONUS = 300
        val ranksPresent = BooleanArray(13)
        val handIfTake = ArrayList(hand)
        for (c in attackCards) {
            if (c != null) {
                ranksPresent[c.rank.value - 1] = true
                handIfTake.add(c)
            }
        }
        for (c in defenseCards) {
            if (c != null) {
                ranksPresent[c.rank.value - 1] = true
                handIfTake.add(c)
            }
        }
        var maxVal = Integer.MIN_VALUE
        var cardIdx = -1
        print("Attack cards: ")
        for (i in 0..attackCards.size - 1) {
            val card = attackCards[i]
            System.out.printf("%s ", card ?: "null")
        }
        println()
        val index = Arrays.asList<Card>(*defenseCards).indexOf(null)
        val attack = attackCards[index]
        System.out.printf("Index = %s attack is %s\n", index, attack ?: "null")
        for (i in hand.indices) {
            val c = hand[i]
            if (c.beats(attack!!, trumpSuit, ruleSet.deuceBeatsAce)) {
                val r = c.rank
                val newHand = ArrayList(hand)
                newHand.removeAt(i)
                val newVal = handValue(newHand, trumpSuit, cardsRemaining, playerHands) + RANK_PRESENT_BONUS * if (ranksPresent[r.value - 1]) 1 else 0
                if (newVal > maxVal) {
                    maxVal = newVal
                    cardIdx = i
                }
            }
        }
        val PENALTY = 800
        val TAKE_PENALTY_BASE = 2000
        val TAKE_PENALTY_DELTA = 40
        if ((currentHandValue(trumpSuit, cardsRemaining, playerHands) - maxVal < PENALTY
                        || handValue(handIfTake, trumpSuit, cardsRemaining, playerHands) - maxVal < TAKE_PENALTY_BASE - TAKE_PENALTY_DELTA * cardsRemaining || cardsRemaining == 0) && cardIdx >= 0) {
            val c = hand[cardIdx]
            hand.removeAt(cardIdx)
            fire(CardBeatenEvent(c))
        } else {
            fire(TakeEvent())
        }
    }

    fun clearHand() {
        hand.clear()
    }

    fun throwCard(c: Card,
                  attackCards: Array<Card?>,
                  defenseCards: Array<Card?>,
                  trumpSuit: Suit) {
        val ranksPresent = BooleanArray(13)
        for (card in attackCards) {
            if (card != null)
                ranksPresent[card.rank.value - 1] = true
        }
        for (card in defenseCards) {
            if (card != null)
                ranksPresent[card.rank.value - 1] = true
        }
        if (hand.contains(c) && (ranksPresent[c.rank.value - 1] || Arrays.equals(attackCards, arrayOfNulls<Card>(6)))) {
            hand.remove(c)
            fire(CardThrownEvent(c))
        }
    }

    fun beatWithCard(c: Card,
                     attackCards: Array<Card?>,
                     defenseCards: Array<Card?>,
                     trumpSuit: Suit) {
        val attack = attackCards[Arrays.asList<Card>(*defenseCards).indexOf(null)] ?: return
        if (hand.contains(c) && c.beats(attack, trumpSuit, ruleSet.deuceBeatsAce)) {
            hand.remove(c)
            fire(CardBeatenEvent(c))
        }
    }

    fun sayDone() {
        fire(DoneEvent())
    }

    fun sayTake() {
        fire(TakeEvent())
    }

    fun sortCards(sortingMode: SortingMode,
                  trumpSuit: Suit) {
        if (sortingMode == SortingMode.UNSORTED) {
            return
        }
        Collections.sort(hand, Comparator<Card> { c1, c2 ->
            if (c1 === c2) {
                return@Comparator 0
            } else {
                val v1 = (c1.suit.value + (3 - trumpSuit.value)) % 4
                val v2 = (c2.suit.value + (3 - trumpSuit.value)) % 4
                val r1 = (c1.rank.value + 11) % 13
                val r2 = (c2.rank.value + 11) % 13
                when (sortingMode) {
                    Player.SortingMode.SUIT_ASCENDING -> {
                        if (v1 < v2) return@Comparator -1
                        if (v1 > v2) return@Comparator 1
                        return@Comparator if (r1 < r2) -1 else 1
                    }
                    Player.SortingMode.SUIT_DESCENDING -> {
                        if (v2 < v1) return@Comparator -1
                        if (v2 > v1) return@Comparator 1
                        return@Comparator if (r2 < r1) -1 else 1
                    }
                    Player.SortingMode.RANK_ASCENDING -> {
                        if (r1 < r2) return@Comparator -1
                        if (r1 > r2) return@Comparator 1
                        return@Comparator if (v1 < v2) -1 else 1
                    }
                    Player.SortingMode.RANK_DESCENDING -> {
                        if (r2 < r1) return@Comparator -1
                        if (r2 > r1) return@Comparator 1
                        return@Comparator if (v2 < v1) -1 else 1
                    }
                }
            }
            0
        })
    }

    companion object {

        private val RANK_MULTIPLIER = 100
        private val UNBALANCED_HAND_PENALTY = 200
        private val MANY_CARDS_PENALTY = 600
        private val OUT_OF_PLAY = 30000
    }

}
