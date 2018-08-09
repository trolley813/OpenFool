package ru.hyst329.openfool

class PlayerTesting(val ruleSet: RuleSet, val lowestRank: Rank) {
    private lateinit var players: Array<Player>
    private lateinit var trumpSuit: Suit
    val DEAL_LIMIT = 6
    private var deck = Deck()
    init {

    }

    fun runGame() {
        deck = Deck()
        trumpSuit = deck.cards?.get(0)!!.suit
        println("Trump suit is $trumpSuit")
        players = Array(ruleSet.playerCount) { i -> Player(ruleSet, "Player $i", i) }
        for (p in 0..players.size - 1) {
            drawCardsToPlayer(p, DEAL_LIMIT)
        }
        printPlayerHands()
    }

    fun runGames(gameCount: Int) {
        for (g in 1..gameCount) {
            println("Running game $g")
            runGame()
        }
    }

    private fun drawCardsToPlayer(playerIndex: Int, cardCount: Int) {
        val player = players[playerIndex]
        for (i in 0 until cardCount) {
            if (deck.cards!!.isEmpty())
                break
            val card = deck.draw() as Card
            player.addCard(card)
        }
    }

    private fun printPlayerHands() {
        val hands = players.map { it.hand.size }.toTypedArray()
        for (p in 0..players.size - 1) {
            print("Player $p hand is: ")
            for (c in players[p].hand) {
                print("$c ")
            }
            print("hand value = ${players[p].currentHandValue(trumpSuit, deck.cards?.size ?: 0, hands)}")
            println()
        }
    }
}