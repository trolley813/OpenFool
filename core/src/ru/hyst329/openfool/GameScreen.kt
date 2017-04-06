package ru.hyst329.openfool

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.scenes.scene2d.Action
import com.badlogic.gdx.scenes.scene2d.Event
import com.badlogic.gdx.scenes.scene2d.EventListener
import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.InputListener
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.utils.viewport.FitViewport

import java.security.SecureRandom
import java.util.ArrayList
import java.util.HashMap
import java.util.Locale
import java.util.Random

import ru.hyst329.openfool.GameScreen.GameState.BEATEN
import ru.hyst329.openfool.GameScreen.GameState.BEATING
import ru.hyst329.openfool.GameScreen.GameState.DRAWING
import ru.hyst329.openfool.GameScreen.GameState.FINISHED
import ru.hyst329.openfool.GameScreen.GameState.READY
import ru.hyst329.openfool.GameScreen.GameState.THROWING
import ru.hyst329.openfool.ResultScreen.Result.DRAW
import ru.hyst329.openfool.ResultScreen.Result.LOST
import ru.hyst329.openfool.ResultScreen.Result.PARTNER_LOST
import ru.hyst329.openfool.ResultScreen.Result.WON

/**
 * Created by main on 13.03.2017.
 * Licensed under MIT License.
 */

class GameScreen(private val game: OpenFoolGame) : Screen, EventListener {
    private val background: Texture
    private val backgroundColor: Color

    internal enum class GameState {
        READY,
        DRAWING,
        THROWING,
        THROWN,
        BEATING,
        BEATEN,
        FINISHED
    }

    private inner class GameStateChangedAction internal constructor(private val newState: GameState) : Action() {

        override fun act(delta: Float): Boolean {
            gameState = newState
            return true
        }
    }

    private inner class SortAction internal constructor(private val playerIndex: Int) : Action() {

        override fun act(delta: Float): Boolean {
            if (playerIndex == 0)
                sortPlayerCards()
            return true
        }
    }

    private val stage: Stage
    private val discardPileGroup: Group
    private val tableGroup: Group
    private val playerGroups: Array<Group>
    internal val trumpSuit: Suit
    internal val players: Array<Player>
    internal var attackCards = arrayOfNulls<Card>(DEAL_LIMIT)
        private set
    internal var defenseCards = arrayOfNulls<Card>(DEAL_LIMIT)
        private set
    private val cardActors = HashMap<Card, CardActor>()
    private val deck = Deck()
    private var currentAttackerIndex: Int = 0
    private var currentThrowerIndex: Int = 0
    private var playersSaidDone: Int = 0
    private var isPlayerTaking: Boolean = false
    private val random = SecureRandom()
    private val outOfPlay = BooleanArray(PLAYER_COUNT)
    private val discardPile = ArrayList<Card>()
    private var gameState = DRAWING
    private var oldGameState = FINISHED
    private val sortingMode: Player.SortingMode
    private var throwLimit = DEAL_LIMIT

    init {
        // Initialise the stage
        stage = Stage(FitViewport(800f, 480f))
        Gdx.input.inputProcessor = stage
        // Get background color
        backgroundColor = Color(game.preferences.getInteger(SettingsScreen.BACKGROUND_COLOR, 0x33cc4dff))
        background = game.assetManager.get(String.format(Locale.ENGLISH, "backgrounds/background%d.png", 1), Texture::class.java)
        background.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat)
        //background.
        val deckStyle = game.preferences.getString(SettingsScreen.DECK, "rus")
        sortingMode = Player.SortingMode.fromInt(game.preferences.getInteger(SettingsScreen.SORTING_MODE, 0))
        // Initialise groups
        tableGroup = Group()
        stage.addActor(tableGroup)
        val deckGroup = Group()
        stage.addActor(deckGroup)
        discardPileGroup = Group()
        stage.addActor(discardPileGroup)
        playerGroups = Array(PLAYER_COUNT, { i -> Group() })
        for (i in 0..PLAYER_COUNT - 1) {
            playerGroups[i] = Group()
            stage.addActor(playerGroups[i])
        }
        // Add done/take listener
        stage.addListener(object : InputListener() {
            override fun touchDown(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                for (cardActor in cardActors.values) {
                    if (cardActor.x <= x &&
                            x <= cardActor.x + cardActor.width * cardActor.scaleX &&
                            cardActor.y <= y &&
                            y <= cardActor.y + cardActor.height * cardActor.scaleY) {
                        // We're clicked on a card
                        return true
                    }
                }
                if (currentThrower.index == 0 && attackCards[0] != null) {
                    currentThrower.sayDone()
                    return true
                }
                if (currentDefender.index == 0) {
                    currentDefender.sayTake()
                    return true
                }
                return true
            }
        })
        // Initialise players
        // TODO: Replace with settings
        val playerNames = arrayOf("South", "West", "North", "East")
        players = Array(PLAYER_COUNT, { i -> Player(this, playerNames[i], i) })
        for (i in 0..PLAYER_COUNT - 1) {
            players[i].addListener(this)
            stage.addActor(players[i])
        }
        // Initialise card actors
        val cards = deck.cards
        val listener = object : InputListener() {
            override fun touchDown(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                if (event!!.target is CardActor) {
                    val cardActor = event.target as CardActor
                    System.out.printf("Trying to click %s\n", cardActor.card)
                    val card = cardActor.card
                    val user = players[0]
                    if (!user.hand.contains(card)) {
                        System.out.printf("%s is not a user's card\n", cardActor.card)
                        return true
                    }
                    if (currentThrower === user) {
                        user.throwCard(card)
                        return true
                    }
                    if (currentDefender === user) {
                        user.beatWithCard(card)
                        return true
                    }
                    return false
                }
                return super.touchDown(event, x, y, pointer, button)
            }
        }
        for (i in cards!!.indices) {
            val c = cards[i]
            val cardActor = CardActor(game, c, deckStyle)
            cardActors.put(c, cardActor)
            cardActor.addListener(this)
            cardActor.addListener(listener)
            cardActor.touchable = Touchable.enabled
            deckGroup.addActor(cardActor)
            cardActor.zIndex = i
        }
        // Starting the game
        for (cardActor in cardActors.values) {
            cardActor.isFaceUp = false
            cardActor.setScale(CARD_SCALE_TABLE)
            cardActor.setPosition(DECK_POSITION[0], DECK_POSITION[1])
            // cardActor.setDebug(true);
        }
        // Determine trump
        val trumpCard = deck.cards?.get(0)
        val trump = cardActors[trumpCard]
        trump?.rotation = -90.0f
        trump?.isFaceUp = true
        trump?.moveBy(90 * CARD_SCALE_TABLE, 0f)
        trumpSuit = trumpCard!!.suit
        println(String.format("Trump suit is %s", trumpSuit.toString()))
        // Draw cards
        for (i in 0..PLAYER_COUNT - 1) {
            drawCardsToPlayer(i, DEAL_LIMIT)
        }
        // Determine the first attacker and thrower
        var lowestTrump = Rank.ACE
        var lowestTrumpCard = Card(Suit.SPADES, Rank.ACE)
        var firstAttacker = 0
        for (p in players) {
            for (c in p.hand) {
                if (c.suit === trumpSuit && (c.rank !== Rank.ACE && c.rank.value < lowestTrump.value || lowestTrump === Rank.ACE)) {
                    firstAttacker = p.index
                    lowestTrump = c.rank
                    lowestTrumpCard = c
                }
            }
        }

        if (firstAttacker != 0) {
            val showingTrump = cardActors[lowestTrumpCard]
            val z = showingTrump?.zIndex ?: 0
            showingTrump?.addAction(Actions.sequence(object : Action() {
                override fun act(delta: Float): Boolean {
                    showingTrump.isFaceUp = true
                    showingTrump.zIndex = 100
                    return true
                }
            }, Actions.delay(1.5f), object : Action() {
                override fun act(delta: Float): Boolean {
                    showingTrump.isFaceUp = false
                    showingTrump.zIndex = z
                    return true
                }
            }))
        }
        println(String.format("%s (%s) has the lowest trump %s",
                players[firstAttacker].name, players[firstAttacker].index, lowestTrump))
        currentAttackerIndex = firstAttacker
        currentThrowerIndex = firstAttacker
    }

    override fun show() {

    }

    override fun render(delta: Float) {
        Gdx.gl.glClearColor(backgroundColor.r, backgroundColor.g, backgroundColor.b, backgroundColor.a)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        // TODO: Actual game logic
        val opponents = (if (outOfPlay[currentAttackerIndex]) 0 else 1) + if (outOfPlay[(currentAttackerIndex + 2) % PLAYER_COUNT]) 0 else 1
        if (playersSaidDone == opponents && gameState != DRAWING
                && gameState != BEATING && gameState != THROWING) {
            System.out.println("Done - all players said done!")
            gameState = FINISHED
        }
        if (oldGameState != gameState) {
            System.out.printf("Game state is %s\n", gameState)
            oldGameState = gameState
        }
        var newGameState = gameState
        when (gameState) {
            READY -> if (currentAttacker.index != 0) {
                throwLimit = Math.min(DEAL_LIMIT, currentDefender.hand.size)
                currentAttacker.startTurn()
            }
            DRAWING -> {
            }
            THROWING -> {
            }
            GameScreen.GameState.THROWN -> {
                if (currentDefender.index != 0) {
                    if (!isPlayerTaking) {
                        currentDefender.tryBeat()
                    } else {
                        currentDefender.sayTake()
                    }
                }
                if (isPlayerTaking)
                    newGameState = BEATEN
            }
            BEATING -> {
            }
            BEATEN -> {
                if (currentDefender.hand.size == 0 || attackCards[throwLimit - 1] != null) {
                    println("Forced to finish the turn")
                    newGameState = FINISHED
                    // break
                }
                if (currentThrower.index != 0) {
                    currentThrower.throwOrDone()
                }
            }
            FINISHED -> {
                val playerTook = isPlayerTaking
                endTurn(if (isPlayerTaking) currentDefender.index else -1)
                newGameState = READY
                currentAttackerIndex += if (playerTook) 2 else 1
                currentAttackerIndex %= PLAYER_COUNT
                currentThrowerIndex = currentAttackerIndex
                System.out.printf("%s (%d) -> %s (%d)\n", currentAttacker.name, currentAttacker.index,
                        currentDefender.name, currentDefender.index)
            }
        }
        gameState = newGameState
        // Draw background
        game.batch.begin()
        game.batch.color = backgroundColor
        game.batch.draw(background, 0f, 0f, 0, 0, 800, 480)
        game.batch.color = Color(Color.WHITE)
        game.batch.end()
        // Draw stage
        stage.act(delta)
        stage.draw()
        // Draw player labels
        game.batch.begin()
        for (i in 0..PLAYER_COUNT - 1) {
            val position = (if (i == 0) PLAYER_POSITION else AI_POSITION).clone()
            if (i > 0)
                position[0] += ((i - 1) * 640 / (PLAYER_COUNT - 2)).toFloat()
            position[1] += 600 * if (i == 0) CARD_SCALE_PLAYER else CARD_SCALE_AI
            game.font.draw(game.batch, String.format("%s: %s", players[i].name, players[i].hand.size),
                    position[0], position[1])

        }
        game.font.draw(game.batch, String.format("%s %s", trumpSuit, cardsRemaining()), 20f, 160f)
        game.batch.end()
        // Check if the game is over
        if (isGameOver) {
            var gameResult: ResultScreen.Result = if (outOfPlay[0]) WON else LOST
            if (outOfPlay[1] && outOfPlay[3] && gameResult == WON)
                gameResult = if (outOfPlay[2]) DRAW else PARTNER_LOST
            game.screen = ResultScreen(game, gameResult)
            dispose()
        }
    }

    override fun resize(width: Int, height: Int) {
        stage.viewport.update(width, height, true)
    }

    override fun pause() {

    }

    override fun resume() {

    }

    override fun hide() {

    }

    override fun dispose() {

    }

    private fun endTurn(playerIndex: Int) {
        playersSaidDone = 0
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
        attackCards = arrayOfNulls<Card>(DEAL_LIMIT)
        defenseCards = arrayOfNulls<Card>(DEAL_LIMIT)
        if (playerIndex < 0) {
            for (card in tableCards) {
                val cardActor = cardActors[card]
                discardPile.add(card)
                discardPileGroup.addActor(cardActor)
                cardActor?.isFaceUp = false
                cardActor?.zIndex = discardPile.size - 1
                cardActor?.rotation = random.nextFloat() * 20 - 10
                val dx = random.nextFloat() * 20 - 10
                val dy = random.nextFloat() * 20 - 10
                cardActor?.addAction(
                        Actions.moveTo(DISCARD_PILE_POSITION[0] + dx, DISCARD_PILE_POSITION[1] + dy, 0.6f))
            }
        } else {
            val player = players[playerIndex]
            for (card in tableCards) {
                player.addCard(card)
                val cardActor = cardActors[card]
                cardActor?.isFaceUp = (playerIndex == 0)
                val position = (if (playerIndex == 0) PLAYER_POSITION else AI_POSITION).clone()
                if (playerIndex > 0)
                    position[0] += ((playerIndex - 1) * 640 / (PLAYER_COUNT - 2)).toFloat()
                val delta = (if (playerIndex == 0) PLAYER_DELTA else AI_DELTA).clone()
                val index = player.hand.size - 1
                val posX = position[0] + index * delta[0]
                val posY = position[1] + index * delta[1]
                cardActor?.addAction(Actions.moveTo(posX, posY, 0.4f))
                cardActor?.rotation = 0.0f
                cardActor?.setScale(if (playerIndex == 0) CARD_SCALE_PLAYER else CARD_SCALE_AI)
                playerGroups[playerIndex].addActor(cardActor)
                cardActor?.setZIndex(index)
            }
            player.addAction(Actions.sequence(
                    Actions.delay(0.39f),
                    SortAction(playerIndex)
            ))
        }
        if (!(deck.cards?.isEmpty() ?: true)) {
            for (i in 0..PLAYER_COUNT - 1) {
                val cardsToDraw = DEAL_LIMIT - players[i].hand.size
                if (cardsToDraw > 0) {
                    drawCardsToPlayer(i, cardsToDraw)
                }
                if (deck.cards?.isEmpty() ?: true)
                    break
            }
        }
        // Check if someone is out of play
        if (deck.cards?.isEmpty() ?: true) {
            for (i in 0..PLAYER_COUNT - 1) {
                outOfPlay[i] = players[i].hand.size == 0
            }
        }
        isPlayerTaking = false
        gameState = READY
    }

    private // TODO: Generalise
    val isGameOver: Boolean
        get() = outOfPlay[0] && outOfPlay[2] || outOfPlay[1] && outOfPlay[3]

    internal fun cardsRemaining(): Int {
        return deck.cards?.size ?: 0
    }

    override fun handle(event: Event): Boolean {
        if (event is Player.CardThrownEvent) {
            // Handle when card is thrown
            playersSaidDone = 0
            var throwIndex = 0
            while (attackCards[throwIndex] != null) throwIndex++
            val throwCard = event.card
            attackCards[throwIndex] = throwCard
            val throwCardActor = cardActors[throwCard]
            throwCardActor?.isFaceUp = true
            tableGroup.addActor(throwCardActor)
            throwCardActor?.zIndex = 2 * throwIndex
            throwCardActor?.setScale(CARD_SCALE_TABLE)
            val throwPos = TABLE_POSITION.clone()
            throwPos[0] += (90 * throwIndex).toFloat()
            throwCardActor?.addAction(Actions.sequence(
                    GameStateChangedAction(GameState.THROWING),
                    Actions.moveTo(throwPos[0], throwPos[1], 0.4f),
                    Actions.delay(0.2f),
                    GameStateChangedAction(GameState.THROWN)))
            val thrower = event.getTarget() as Player
            System.out.printf("%s (%s) throws %s\n", thrower.name, thrower.index, throwCard)
            for (i in 0..thrower.hand.size - 1) {
                val cardActor = cardActors[thrower.hand[i]]
                val position = (if (thrower.index == 0) PLAYER_POSITION else AI_POSITION).clone()
                val delta = (if (thrower.index == 0) PLAYER_DELTA else AI_DELTA).clone()
                if (thrower.index > 0)
                    position[0] += ((thrower.index - 1) * 640 / (PLAYER_COUNT - 2)).toFloat()
                val posX = position[0] + i * delta[0]
                val posY = position[1] + i * delta[1]
                //cardActor.addAction(Actions.moveTo(posX, posY, 0.1f));
                cardActor?.setPosition(posX, posY)
                cardActor?.rotation = 0.0f
                cardActor?.setScale(if (thrower.index == 0) CARD_SCALE_PLAYER else CARD_SCALE_AI)
                cardActor?.zIndex = i
            }
            return true
        }
        if (event is Player.CardBeatenEvent) {
            // Handle when card is beaten
            playersSaidDone = 0
            var beatIndex = 0
            while (defenseCards[beatIndex] != null) beatIndex++
            val beatCard = event.card
            defenseCards[beatIndex] = beatCard
            val beatCardActor = cardActors[beatCard]
            beatCardActor?.isFaceUp = true
            tableGroup.addActor(beatCardActor)
            beatCardActor?.zIndex = 2 * beatIndex + 1
            beatCardActor?.setScale(CARD_SCALE_TABLE)
            val beatPos = TABLE_POSITION.clone()
            beatPos[0] += (90 * beatIndex).toFloat()
            beatCardActor?.addAction(Actions.sequence(
                    GameStateChangedAction(GameState.BEATING),
                    Actions.moveTo(beatPos[0] + TABLE_DELTA[0], beatPos[1] + TABLE_DELTA[1], 0.4f),
                    Actions.delay(0.2f),
                    GameStateChangedAction(GameState.BEATEN)))
            val beater = event.getTarget() as Player
            System.out.printf("%s (%s) beats with %s\n", beater.name, beater.index, beatCard)
            for (i in 0..beater.hand.size - 1) {
                val cardActor = cardActors[beater.hand[i]]
                val position = (if (beater.index == 0) PLAYER_POSITION else AI_POSITION).clone()
                val delta = (if (beater.index == 0) PLAYER_DELTA else AI_DELTA).clone()
                if (beater.index > 0)
                    position[0] += ((beater.index - 1) * 640 / (PLAYER_COUNT - 2)).toFloat()
                val posX = position[0] + i * delta[0]
                val posY = position[1] + i * delta[1]
                //cardActor.addAction(Actions.moveTo(posX, posY, 0.1f));
                cardActor?.setPosition(posX, posY)
                cardActor?.rotation = 0.0f
                cardActor?.setScale(if (beater.index == 0) CARD_SCALE_PLAYER else CARD_SCALE_AI)
                cardActor?.zIndex = i
            }
            return true
        }
        if (event is Player.TakeEvent) {
            // Handle when player takes
            playersSaidDone = 0
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
            currentThrowerIndex %= PLAYER_COUNT
            val player = event.getTarget() as Player
            System.out.printf("%s (%s) says done\n",
                    player.name, player.index)
            return true
        }
        return false
    }

    private fun drawCardsToPlayer(playerIndex: Int, cardCount: Int) {
        val player = players[playerIndex]
        if (!deck.cards!!.isEmpty()) {
            player.addAction(Actions.sequence(
                    GameStateChangedAction(GameState.DRAWING),
                    Actions.delay(0.39f),
                    GameStateChangedAction(READY),
                    SortAction(playerIndex)
            ))
        }
        for (i in 0..cardCount - 1) {
            if (deck.cards!!.isEmpty())
                break
            val card = deck.draw() as Card
            player.addCard(card)
            val cardActor = cardActors[card]
            cardActor?.isFaceUp = (playerIndex == 0)
            val position = (if (playerIndex == 0) PLAYER_POSITION else AI_POSITION).clone()
            if (playerIndex > 0)
                position[0] += ((playerIndex - 1) * 640 / (PLAYER_COUNT - 2)).toFloat()
            val delta = (if (playerIndex == 0) PLAYER_DELTA else AI_DELTA).clone()
            val index = player.hand.size - 1
            val posX = position[0] + index * delta[0]
            val posY = position[1] + index * delta[1]
            cardActor?.addAction(Actions.moveTo(posX, posY, 0.4f))
            cardActor?.rotation = 0.0f
            cardActor?.setScale(if (playerIndex == 0) CARD_SCALE_PLAYER else CARD_SCALE_AI)
            playerGroups[playerIndex].addActor(cardActor)
            cardActor?.zIndex = index
        }
    }

    private val currentAttacker: Player
        get() {
            if (outOfPlay[currentAttackerIndex]) {
                return players[(currentAttackerIndex + 2) % PLAYER_COUNT]
            }
            return players[currentAttackerIndex]
        }

    private val currentDefender: Player
        get() {
            val currentDefender = (currentAttackerIndex + 1) % PLAYER_COUNT
            if (outOfPlay[currentDefender]) {
                return players[(currentDefender + 2) % PLAYER_COUNT]
            }
            return players[currentDefender]
        }

    private val currentThrower: Player
        get() {
            if (outOfPlay[currentThrowerIndex]) {
                return players[(currentThrowerIndex + 2) % PLAYER_COUNT]
            }
            return players[currentThrowerIndex]
        }

    private fun sortPlayerCards() {
        // TODO: Generalise to other players
        val player = players[0]
        player.sortCards(sortingMode)
        // Reposition all cards
        for (i in 0..player.hand.size - 1) {
            val cardActor = cardActors[player.hand[i]]
            val position = (if (player.index == 0) PLAYER_POSITION else AI_POSITION).clone()
            val delta = (if (player.index == 0) PLAYER_DELTA else AI_DELTA).clone()
            if (player.index > 0)
                position[0] += ((player.index - 1) * 640 / (PLAYER_COUNT - 2)).toFloat()
            val posX = position[0] + i * delta[0]
            val posY = position[1] + i * delta[1]
            cardActor?.setPosition(posX, posY)
            cardActor?.rotation = 0.0f
            cardActor?.setScale(if (player.index == 0) CARD_SCALE_PLAYER else CARD_SCALE_AI)
            cardActor?.zIndex = i
        }
    }

    companion object {

        private val DEAL_LIMIT = 6
        private val PLAYER_COUNT = 4
        private val CARD_SCALE_TABLE = 0.24f
        private val CARD_SCALE_AI = 0.18f
        private val CARD_SCALE_PLAYER = 0.28f
        // Half-widths and half-heights
        private val HW_TABLE = CARD_SCALE_TABLE * 180
        private val HW_AI = CARD_SCALE_AI * 180
        private val HW_PLAYER = CARD_SCALE_PLAYER * 180
        private val HH_TABLE = CARD_SCALE_TABLE * 270
        private val HH_AI = CARD_SCALE_AI * 270
        private val HH_PLAYER = CARD_SCALE_PLAYER * 270

        private val DECK_POSITION = floatArrayOf(60 - HW_TABLE, 240 - HH_TABLE)
        private val DISCARD_PILE_POSITION = floatArrayOf(680 - HW_TABLE, 180 - HH_TABLE)
        private val PLAYER_POSITION = floatArrayOf(240 - HW_PLAYER, 80 - HH_PLAYER)
        private val AI_POSITION = floatArrayOf(60 - HW_AI, 400 - HH_AI)
        private val TABLE_POSITION = floatArrayOf(200 - HW_TABLE, 280 - HH_TABLE)
        private val TABLE_DELTA = floatArrayOf(10f, -10f)
        private val PLAYER_DELTA = floatArrayOf(40f, 0f)
        private val AI_DELTA = floatArrayOf(5f, -5f)
    }
}
