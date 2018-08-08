package ru.hyst329.openfool

class PlayerTesting(ruleSet: RuleSet, lowestRank: Rank) {
    private val players: Array<Player>
    private var deck = Deck()
    init {
        players = Array(ruleSet.playerCount) { i -> Player(ruleSet, "Player $i", i) }
    }

    fun runGames(gameCount: Int) {

    }
}