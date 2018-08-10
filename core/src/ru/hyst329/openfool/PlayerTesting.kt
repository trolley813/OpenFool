package ru.hyst329.openfool

import java.awt.Event
import java.util.*
import com.badlogic.gdx.scenes.scene2d.EventListener
import ru.hyst329.openfool.PlayerTesting.GameState.BEATEN
import ru.hyst329.openfool.PlayerTesting.GameState.FINISHED
import ru.hyst329.openfool.PlayerTesting.GameState.READY
import ru.hyst329.openfool.PlayerTesting.GameState.THROWN

class PlayerTesting(val ruleSet: RuleSet, val lowestRank: Rank) : EventListener {

    // The same as in GameScreen, but without transition states
    internal enum class GameState {
        READY,
        THROWN,
        BEATEN,
        FINISHED
    }

    private lateinit var players: Array<Player>
    private lateinit var trumpSuit: Suit
    val DEAL_LIMIT = 6
    internal var attackCards = arrayOfNulls<Card>(DEAL_LIMIT)
        private set
    internal var defenseCards = arrayOfNulls<Card>(DEAL_LIMIT)
        private set
    private var deck = Deck()
    private var playersSaidDone: Int = 0
    private var playerDoneStatuses: BooleanArray
    private val outOfPlay: BooleanArray
    private var currentAttackerIndex: Int = 0
    private var currentThrowerIndex: Int = 0
    private var isPlayerTaking: Boolean = false
    private var gameState = READY
    private var oldGameState = FINISHED
    private val discardPile = ArrayList<Card>()
    private var throwLimit = DEAL_LIMIT

    init {
        playerDoneStatuses = BooleanArray(ruleSet.playerCount)
        outOfPlay = BooleanArray(ruleSet.playerCount)
    }

    fun runGame() {
        deck = Deck(lowestRank)
        trumpSuit = deck.cards?.get(0)!!.suit

        println("Trump suit is $trumpSuit")
        players = Array(ruleSet.playerCount) { i -> Player(ruleSet, "Player $i", i) }
        for (p in 0..players.size - 1) {
            drawCardsToPlayer(p, DEAL_LIMIT)
        }
        for (i in 0 until ruleSet.playerCount) {
            players[i].addListener(this)
            //stage.addActor(players[i])
        }
        printPlayerHands()
        val handsSizes = players.map { it.hand.size }.toTypedArray()
        val initialHands = players.map { p -> p.currentHandValue(trumpSuit, deck.cards?.size ?: 0, handsSizes) }
        // determine the lowest trump card and the first attacker
        var lowestTrump = Rank.ACE
        var firstAttacker = 0
        for (p in players) {
            for (c in p.hand) {
                if (c.suit === trumpSuit && (c.rank !== Rank.ACE && c.rank.value < lowestTrump.value || lowestTrump === Rank.ACE)) {
                    firstAttacker = p.index
                    lowestTrump = c.rank
                }
            }
        }
        println(players[firstAttacker].name +
                " (${players[firstAttacker].index})" +
                " has the lowest trump $lowestTrump")
        currentAttackerIndex = firstAttacker
        currentThrowerIndex = firstAttacker
        while(!isGameOver) {
            step()
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

    override fun handle(event: com.badlogic.gdx.scenes.scene2d.Event?): Boolean {
        // TODO: reduce code duplication, don't repeat yourself
        if (event is Player.CardThrownEvent) {
            // Handle when card is thrown
            playersSaidDone = 0
            playerDoneStatuses = BooleanArray(ruleSet.playerCount)
            var throwIndex = 0
            while (attackCards[throwIndex] != null) throwIndex++
            val throwCard = event.card
            attackCards[throwIndex] = throwCard
            val thrower = event.getTarget() as Player
            System.out.printf("%s (%s) throws %s\n", thrower.name, thrower.index, throwCard)
            gameState = THROWN
            return true
        }
        if (event is Player.CardBeatenEvent) {
            // Handle when card is beaten
            playersSaidDone = 0
            playerDoneStatuses = BooleanArray(ruleSet.playerCount)
            var beatIndex = 0
            while (defenseCards[beatIndex] != null) beatIndex++
            val beatCard = event.card
            defenseCards[beatIndex] = beatCard
            val beater = event.getTarget() as Player
            System.out.printf("%s (%s) beats with %s\n", beater.name, beater.index, beatCard)
            gameState = BEATEN
            return true
        }
        if (event is Player.TakeEvent) {
            // Handle when player takes
            playersSaidDone = 0
            playerDoneStatuses = BooleanArray(ruleSet.playerCount)
            isPlayerTaking = true
            val player = event.getTarget() as Player
            System.out.printf("%s (%s) decides to take\n",
                    player.name, player.index)
            return true

        }
        if (event is Player.DoneEvent) {
            // Handle when player says done
            playersSaidDone++
            currentThrowerIndex += 2
            currentThrowerIndex %= ruleSet.playerCount
            val player = event.getTarget() as Player
            playerDoneStatuses[player.index] = true
            System.out.printf("%s (%s) says done\n",
                    player.name, player.index)
            return true
        }
        return false
    }

    val isGameOver: Boolean
        get() {
            // Simply check if only one player remains
            return (outOfPlay.map { if (it) 0 else 1 }.fold(initial = 0) { total, current -> total + current }) <= 1
        }

    fun step() {
        val opponents =
                when {
                    ruleSet.teamPlay -> (if (outOfPlay[currentAttackerIndex]) 0 else 1) + if (outOfPlay[(currentAttackerIndex + 2) % ruleSet.playerCount]) 0 else 1
                    (outOfPlay.map { if (it) 0 else 1 }.fold(initial = 0) { total, current -> total + current }) > 2 -> 2
                    else -> 1
                }
        if (playersSaidDone == opponents) {
            System.out.println("Done - all players said done!")
            gameState = FINISHED
        }
        if (oldGameState != gameState) {
            System.out.printf("Game state is %s\n", gameState)
            oldGameState = gameState
        }
        when (gameState) {
            READY -> {
                throwLimit = Math.min((if (ruleSet.loweredFirstDiscardLimit
                        && discardPile.isEmpty())
                    DEAL_LIMIT else DEAL_LIMIT - 1)
                        , currentDefender.hand.size)
                currentAttacker.startTurn(trumpSuit, cardsRemaining(), players.map { it.hand.size }.toTypedArray())
            }
            THROWN -> {
                if (!isPlayerTaking) {
                    currentDefender.tryBeat(attackCards, defenseCards, trumpSuit, cardsRemaining(), players.map { it.hand.size }.toTypedArray())
                } else {
                    currentDefender.sayTake()
                }
                if (isPlayerTaking)
                    gameState = BEATEN
            }
            BEATEN -> {
                val forcedFinish =
                        if (currentDefender.hand.size == 0 || attackCards[throwLimit - 1] != null) {
                            println("Forced to finish the turn")
                            gameState = FINISHED
                            true
                        } else false
                if (!forcedFinish)
                    currentThrower.throwOrDone(attackCards, defenseCards, trumpSuit, cardsRemaining(), players.map { it.hand.size }.toTypedArray())
                else
                    currentThrower.sayDone()
            }
            FINISHED -> {
                val playerTook = isPlayerTaking
                val currentDefenderIndex = currentDefender.index
                endTurn(if (isPlayerTaking) currentDefender.index else -1)
                currentAttackerIndex += if (playerTook) 2 else 1
                currentAttackerIndex %= ruleSet.playerCount
                if (!ruleSet.teamPlay)
                // Defender who took cannot attack anyway!
                    while (outOfPlay[currentAttackerIndex] ||
                            (playerTook && currentAttackerIndex == currentDefenderIndex)) {
                        currentAttackerIndex++
                        if (currentAttackerIndex == ruleSet.playerCount)
                            currentAttackerIndex = 0
                    }
                currentThrowerIndex = currentAttackerIndex
                System.out.printf("%s (%d) -> %s (%d)\n", currentAttacker.name, currentAttacker.index,
                        currentDefender.name, currentDefender.index)
            }
        }
    }

    private val currentAttacker: Player
        get() {
            if (outOfPlay[currentAttackerIndex]) {
                return players[(currentAttackerIndex + 2) % ruleSet.playerCount]
            }
            return players[currentAttackerIndex]
        }

    private val currentDefender: Player
        get() {
            var currentDefenderIndex = (currentAttackerIndex + 1) % ruleSet.playerCount
            if (!ruleSet.teamPlay)
                while (outOfPlay[currentDefenderIndex]) {
                    currentDefenderIndex++
                    if (currentDefenderIndex == ruleSet.playerCount)
                        currentDefenderIndex = 0
                }
            else if (outOfPlay[currentDefenderIndex]) {
                return players[(currentDefenderIndex + 2) % ruleSet.playerCount]
            }
            return players[currentDefenderIndex]
        }

    private val currentThrower: Player
        get() {
            if (outOfPlay[currentThrowerIndex]) {
                return players[(currentThrowerIndex + 2) % ruleSet.playerCount]
            }
            return players[currentThrowerIndex]
        }

    internal fun cardsRemaining(): Int {
        return deck.cards?.size ?: 0
    }

    private fun endTurn(playerIndex: Int) {
        playersSaidDone = 0
        playerDoneStatuses = BooleanArray(ruleSet.playerCount)
        val tableCards = ArrayList<Card>()
        for (i in attackCards.indices) {
            if (attackCards[i] != null) {
                tableCards.add(attackCards[i] as Card)
                //attackCards[i] = null;
            }
            if (defenseCards[i] != null) {
                tableCards.add(defenseCards[i] as Card)
                //defenseCards[i] = null;
            }
        }
        attackCards = arrayOfNulls(DEAL_LIMIT)
        defenseCards = arrayOfNulls(DEAL_LIMIT)
        if (deck.cards?.isEmpty() == false) {
            for (i in 0 until ruleSet.playerCount) {
                val cardsToDraw = DEAL_LIMIT - players[i].hand.size
                if (cardsToDraw > 0) {
                    drawCardsToPlayer(i, cardsToDraw)
                }
                if (deck.cards?.isEmpty() != false)
                    break
            }
        }
        // Check if someone is out of play
        if (deck.cards?.isEmpty() != false) {
            for (i in 0 until ruleSet.playerCount) {
                outOfPlay[i] = players[i].hand.size == 0
            }
        }
        isPlayerTaking = false
        gameState = READY
        println("Turn ended, remaining ${cardsRemaining()} cards, $isGameOver")
    }

}