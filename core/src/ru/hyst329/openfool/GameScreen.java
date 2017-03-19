package ru.hyst329.openfool;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.sun.scenario.Settings;

import java.util.ArrayList;
import java.util.HashMap;

import static ru.hyst329.openfool.GameScreen.GameState.BEATEN;
import static ru.hyst329.openfool.GameScreen.GameState.BEATING;
import static ru.hyst329.openfool.GameScreen.GameState.DRAWING;
import static ru.hyst329.openfool.GameScreen.GameState.FINISHED;
import static ru.hyst329.openfool.GameScreen.GameState.READY;
import static ru.hyst329.openfool.GameScreen.GameState.THROWING;
import static ru.hyst329.openfool.ResultScreen.Result.DRAW;
import static ru.hyst329.openfool.ResultScreen.Result.LOST;
import static ru.hyst329.openfool.ResultScreen.Result.PARTNER_LOST;
import static ru.hyst329.openfool.ResultScreen.Result.WON;

/**
 * Created by main on 13.03.2017.
 * Licensed under MIT License.
 */

public class GameScreen implements Screen, EventListener {
    private Color backgroundColor;

    enum GameState {
        READY,
        DRAWING,
        THROWING,
        THROWN,
        BEATING,
        BEATEN,
        FINISHED
    }

    class GameStateChangedAction extends Action {
        private GameState newState;

        public GameStateChangedAction(GameState newState) {
            this.newState = newState;
        }

        @Override
        public boolean act(float delta) {
            gameState = newState;
            return true;
        }
    }

    private Stage stage;
    private Group deckGroup, discardPileGroup, tableGroup;
    private Group[] playerGroups;
    private OpenFoolGame game;
    private Suit trumpSuit;
    private Player[] players;
    private Card[] attackCards = new Card[DEAL_LIMIT], defenseCards = new Card[DEAL_LIMIT];
    private HashMap<Card, CardActor> cardActors = new HashMap<Card, CardActor>();
    private Deck deck = new Deck();
    private int currentAttackerIndex, currentThrowerIndex;
    private int playersSaidDone;
    private boolean isPlayerTaking;

    private static final int DEAL_LIMIT = 6;
    private static final int PLAYER_COUNT = 4;
    private static final float CARD_SCALE_TABLE = 0.24f;
    private static final float CARD_SCALE_AI = 0.18f;
    private static final float CARD_SCALE_PLAYER = 0.28f;
    // Half-widths and half-heights
    private static final float HW_TABLE = CARD_SCALE_TABLE * 180;
    private static final float HW_AI = CARD_SCALE_AI * 180;
    private static final float HW_PLAYER = CARD_SCALE_PLAYER * 180;
    private static final float HH_TABLE = CARD_SCALE_TABLE * 270;
    private static final float HH_AI = CARD_SCALE_AI * 270;
    private static final float HH_PLAYER = CARD_SCALE_PLAYER * 270;

    private static final float[] DECK_POSITION = {60 - HW_TABLE, 240 - HH_TABLE};
    private static final float[] DISCARD_PILE_POSITION = {640 - HW_TABLE, 120 - HH_TABLE};
    private static final float[] PLAYER_POSITION = {240 - HW_PLAYER, 80 - HH_PLAYER};
    private static final float[] AI_POSITION = {60 - HW_AI, 400 - HH_AI};
    private static final float[] TABLE_POSITION = {200 - HW_TABLE, 280 - HH_TABLE};
    private static final float[] TABLE_DELTA = {10, -10};
    private static final float[] PLAYER_DELTA = {40, 0};
    private static final float[] AI_DELTA = {5, -5};
    private boolean[] outOfPlay = new boolean[PLAYER_COUNT];
    private ArrayList<Card> discardPile = new ArrayList<Card>();
    private GameState gameState = DRAWING, oldGameState = FINISHED;

    public GameScreen(OpenFoolGame game) {
        this.game = game;
        // Initialise the stage
        stage = new Stage(new FitViewport(800, 480));
        Gdx.input.setInputProcessor(stage);
        // Get background color
        backgroundColor = new Color(game.preferences.getInteger(SettingsScreen.BACKGROUND_COLOR, 0x33cc4dff));
        String deckStyle = game.preferences.getString(SettingsScreen.DECK, "rus");
        // Initialise groups
        tableGroup = new Group();
        stage.addActor(tableGroup);
        deckGroup = new Group();
        stage.addActor(deckGroup);
        discardPileGroup = new Group();
        stage.addActor(discardPileGroup);
        playerGroups = new Group[PLAYER_COUNT];
        for (int i = 0; i < PLAYER_COUNT; i++) {
            playerGroups[i] = new Group();
            stage.addActor(playerGroups[i]);
        }
        // Add done/take listener
        stage.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                for (CardActor cardActor : cardActors.values()) {
                    if (cardActor.getX() <= x &&
                            x <= cardActor.getX() + cardActor.getWidth() * cardActor.getScaleX() &&
                            cardActor.getY() <= y &&
                            y <= cardActor.getY() + cardActor.getHeight() * cardActor.getScaleY()) {
                        // We're clicked on a card
                        return true;
                    }
                }
                if (getCurrentThrower().getIndex() == 0 && attackCards[0] != null) {
                    getCurrentThrower().sayDone();
                    return true;
                }
                if (getCurrentDefender().getIndex() == 0) {
                    getCurrentDefender().sayTake();
                    return true;
                }
                return false;
            }
        });
        // Initialise card actors
        ArrayList<Card> cards = deck.getCards();
        InputListener listener = new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                if (event.getTarget() instanceof CardActor) {
                    CardActor cardActor = ((CardActor) event.getTarget());
                    System.out.printf("Trying to click %s\n", cardActor.getCard());
                    Card card = cardActor.getCard();
                    Player user = players[0];
                    if (!user.getHand().contains(card)) {
                        System.out.printf("%s is not a user's card\n", cardActor.getCard());
                        return true;
                    }
                    if (getCurrentThrower() == user) {
                        user.throwCard(card);
                        return true;
                    }
                    if (getCurrentDefender() == user) {
                        user.beatWithCard(card);
                        return true;
                    }
                    return false;
                }
                return super.touchDown(event, x, y, pointer, button);
            }
        };
        for (int i = 0; i < cards.size(); i++) {
            Card c = cards.get(i);
            CardActor cardActor = new CardActor(game, c, deckStyle);
            cardActors.put(c, cardActor);
            cardActor.addListener(this);
            cardActor.addListener(listener);
            cardActor.setTouchable(Touchable.enabled);
            deckGroup.addActor(cardActor);
            cardActor.setZIndex(i);
        }
        // Initialise players
        // TODO: Replace with settings
        String[] playerNames = {"South", "West", "North", "East"};
        players = new Player[PLAYER_COUNT];
        for (int i = 0; i < PLAYER_COUNT; i++) {
            players[i] = new Player(this, playerNames[i], i);
            players[i].addListener(this);
            stage.addActor(players[i]);
        }
        // Starting the game
        for (CardActor cardActor : cardActors.values()) {
            cardActor.setFaceUp(false);
            cardActor.setScale(CARD_SCALE_TABLE);
            cardActor.setPosition(DECK_POSITION[0], DECK_POSITION[1]);
            // cardActor.setDebug(true);
        }
        for (int i = 0; i < PLAYER_COUNT; i++) {
            drawCardsToPlayer(i, DEAL_LIMIT);
        }
        Card trumpCard = deck.getCards().get(0);
        final CardActor trump = cardActors.get(trumpCard);
        trump.setRotation(-90.0f);
        trump.setFaceUp(true);
        trump.moveBy(90 * CARD_SCALE_TABLE, 0);
        trumpSuit = trumpCard.getSuit();
        System.out.println(String.format("Trump suit is %s", trumpSuit.toString()));
        // Determine the first attacker and thrower
        Rank lowestTrump = Rank.ACE;
        Card lowestTrumpCard = new Card(Suit.SPADES, Rank.ACE);
        int firstAttacker = 0;
        for (Player p : players) {
            for (Card c : p.getHand()) {
                if (c.getSuit() == trumpSuit
                        && ((c.getRank() != Rank.ACE && c.getRank().getValue() < lowestTrump.getValue())
                        || lowestTrump == Rank.ACE)) {
                    firstAttacker = p.getIndex();
                    lowestTrump = c.getRank();
                    lowestTrumpCard = c;
                }
            }
        }

        if (firstAttacker != 0) {
            final CardActor showingTrump = cardActors.get(lowestTrumpCard);
            final int z = showingTrump.getZIndex();
            showingTrump.addAction(Actions.sequence(new Action() {
                @Override
                public boolean act(float delta) {
                    showingTrump.setFaceUp(true);
                    showingTrump.setZIndex(100);
                    return true;
                }
            }, Actions.delay(1.5f), new Action() {
                @Override
                public boolean act(float delta) {
                    showingTrump.setFaceUp(false);
                    showingTrump.setZIndex(z);
                    return true;
                }
            }));
        }
        System.out.println(String.format("%s (%s) has the lowest trump %s",
                players[firstAttacker].getName(), players[firstAttacker].getIndex(), lowestTrump));
        currentAttackerIndex = firstAttacker;
        currentThrowerIndex = firstAttacker;
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(backgroundColor.r, backgroundColor.g, backgroundColor.b, backgroundColor.a);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        // TODO: Actual game logic
        int opponents = (outOfPlay[currentAttackerIndex] ? 0 : 1)
                + (outOfPlay[(currentAttackerIndex + 2) % PLAYER_COUNT] ? 0 : 1);
        int throwLimit = Math.min(DEAL_LIMIT, getCurrentDefender().getHand().size());
        if (playersSaidDone == opponents && gameState != DRAWING
                && gameState != BEATING && gameState != THROWING) {
            gameState = FINISHED;
        }
        if (oldGameState != gameState) {
            System.out.printf("Game state is %s\n", gameState);
            oldGameState = gameState;
        }
        switch (gameState) {
            case READY:
                if (getCurrentAttacker().getIndex() != 0) {
                    getCurrentAttacker().startTurn();
                }
                break;
            case DRAWING:
                break;
            case THROWING:
                break;
            case THROWN:
                if (getCurrentDefender().getIndex() != 0) {
                    getCurrentDefender().tryBeat();
                }
                if (isPlayerTaking)
                    gameState = BEATEN;
                break;
            case BEATING:
                break;
            case BEATEN:
                if (getCurrentDefender().getHand().size() == 0
                        || attackCards[throwLimit - 1] != null) {
                    gameState = FINISHED;
                    break;
                }
                if (getCurrentThrower().getIndex() != 0) {
                    getCurrentThrower().throwOrDone();
                }
                break;
            case FINISHED:
                boolean playerTook = isPlayerTaking;
                endTurn(isPlayerTaking ? getCurrentDefender().getIndex() : -1);
                currentAttackerIndex += (playerTook ? 2 : 1);
                currentAttackerIndex %= PLAYER_COUNT;
                currentThrowerIndex = currentAttackerIndex;
                break;
        }
        // Draw stage
        stage.act(delta);
        stage.draw();
        // Draw player labels
        game.batch.begin();
        for (int i = 0; i < PLAYER_COUNT; i++) {
            float[] position = (i == 0 ? PLAYER_POSITION : AI_POSITION).clone();
            if (i > 0)
                position[0] += (i - 1) * 640 / (PLAYER_COUNT - 2);
            position[1] += 600 * (i == 0 ? CARD_SCALE_PLAYER : CARD_SCALE_AI);
            game.font.draw(game.batch, String.format("%s: %s", players[i].getName(), players[i].getHand().size()),
                    position[0], position[1]);

        }
        game.font.draw(game.batch, String.format("%s %s", trumpSuit, cardsRemaining()), 20, 160);
        game.batch.end();
        // Check if the game is over
        if (isGameOver()) {
            ResultScreen.Result gameResult = outOfPlay[0] ? WON : LOST;
            if (outOfPlay[1] && outOfPlay[3] && gameResult == WON)
                gameResult = outOfPlay[2] ? DRAW : PARTNER_LOST;
            game.setScreen(new ResultScreen(game, gameResult));
            dispose();
        }
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {

    }

    public void endTurn(int playerIndex) {
        playersSaidDone = 0;
        ArrayList<Card> tableCards = new ArrayList<Card>();
        for (int i = 0; i < attackCards.length; i++) {
            if (attackCards[i] != null) {
                tableCards.add(attackCards[i]);
                //attackCards[i] = null;
            }
            if (defenseCards[i] != null) {
                tableCards.add(defenseCards[i]);
                //defenseCards[i] = null;
            }
        }
        attackCards = new Card[DEAL_LIMIT];
        defenseCards = new Card[DEAL_LIMIT];
        if (playerIndex < 0) {
            for (Card card : tableCards) {
                CardActor cardActor = cardActors.get(card);
                discardPile.add(card);
                discardPileGroup.addActor(cardActor);
                cardActor.setFaceUp(false);
                cardActor.setZIndex(discardPile.size() - 1);
                cardActor.addAction(
                        Actions.moveTo(DISCARD_PILE_POSITION[0], DISCARD_PILE_POSITION[1], 0.6f));
            }
        } else {
            for (Card card : tableCards) {
                Player player = players[playerIndex];
                player.addCard(card);
                CardActor cardActor = cardActors.get(card);
                cardActor.setFaceUp(playerIndex == 0);
                float[] position = (playerIndex == 0 ? PLAYER_POSITION : AI_POSITION).clone();
                if (playerIndex > 0)
                    position[0] += (playerIndex - 1) * 640 / (PLAYER_COUNT - 2);
                float[] delta = (playerIndex == 0 ? PLAYER_DELTA : AI_DELTA).clone();
                int index = player.getHand().size() - 1;
                float posX = position[0] + index * delta[0];
                float posY = position[1] + index * delta[1];
                cardActor.addAction(Actions.moveTo(posX, posY, 0.4f));
                cardActor.setRotation(0.0f);
                cardActor.setScale(playerIndex == 0 ? CARD_SCALE_PLAYER : CARD_SCALE_AI);
                playerGroups[playerIndex].addActor(cardActor);
                cardActor.setZIndex(index);
            }
        }
        if (!deck.getCards().isEmpty()) {
            for (int i = 0; i < PLAYER_COUNT; i++) {
                int cardsToDraw = DEAL_LIMIT - players[i].getHand().size();
                if (cardsToDraw > 0) {
                    drawCardsToPlayer(i, cardsToDraw);
                }
                if (deck.getCards().isEmpty())
                    break;
            }
        }
        // Check if someone is out of play
        if (deck.getCards().isEmpty()) {
            for (int i = 0; i < PLAYER_COUNT; i++) {
                outOfPlay[i] = players[i].getHand().size() == 0;
            }
        }
        isPlayerTaking = false;
        gameState = READY;
    }

    public boolean isGameOver() {
        // TODO: Generalise
        return (outOfPlay[0] && outOfPlay[2]) || (outOfPlay[1] && outOfPlay[3]);
    }

    public Suit getTrumpSuit() {
        return trumpSuit;
    }

    public int cardsRemaining() {
        return deck.getCards().size();
    }

    public Player[] getPlayers() {
        return players;
    }

    @Override
    public boolean handle(Event event) {
        if (event instanceof Player.CardThrownEvent) {
            // TODO: Handle when card is thrown
            playersSaidDone = 0;
            playersSaidDone = 0;
            int throwIndex = 0;
            while (attackCards[throwIndex] != null) throwIndex++;
            Card throwCard = ((Player.CardThrownEvent) event).getCard();
            attackCards[throwIndex] = throwCard;
            CardActor throwCardActor = cardActors.get(throwCard);
            throwCardActor.setFaceUp(true);
            tableGroup.addActor(throwCardActor);
            throwCardActor.setZIndex(2 * throwIndex);
            throwCardActor.setScale(CARD_SCALE_TABLE);
            float[] throwPos = TABLE_POSITION.clone();
            throwPos[0] += 90 * throwIndex;
            throwCardActor.addAction(Actions.sequence(
                    new GameStateChangedAction(GameState.THROWING),
                    Actions.moveTo(throwPos[0], throwPos[1], 0.4f),
                    Actions.delay(0.2f),
                    new GameStateChangedAction(GameState.THROWN)));
            Player thrower = (Player) event.getTarget();
            System.out.printf("%s (%s) throws %s\n", thrower.getName(), thrower.getIndex(), throwCard);
            for (int i = 0; i < thrower.getHand().size(); i++) {
                CardActor cardActor = cardActors.get(thrower.getHand().get(i));
                float[] position = (thrower.getIndex() == 0 ? PLAYER_POSITION : AI_POSITION).clone();
                float[] delta = (thrower.getIndex() == 0 ? PLAYER_DELTA : AI_DELTA).clone();
                if (thrower.getIndex() > 0)
                    position[0] += (thrower.getIndex() - 1) * 640 / (PLAYER_COUNT - 2);
                float posX = position[0] + i * delta[0];
                float posY = position[1] + i * delta[1];
                //cardActor.addAction(Actions.moveTo(posX, posY, 0.1f));
                cardActor.setPosition(posX, posY);
                cardActor.setRotation(0.0f);
                cardActor.setScale(thrower.getIndex() == 0 ? CARD_SCALE_PLAYER : CARD_SCALE_AI);
                cardActor.setZIndex(i);
            }
            return true;
        }
        if (event instanceof Player.CardBeatenEvent) {
            // TODO: Handle when card is beaten
            playersSaidDone = 0;
            int beatIndex = 0;
            while (defenseCards[beatIndex] != null) beatIndex++;
            Card beatCard = ((Player.CardBeatenEvent) event).getCard();
            defenseCards[beatIndex] = beatCard;
            CardActor beatCardActor = cardActors.get(beatCard);
            beatCardActor.setFaceUp(true);
            tableGroup.addActor(beatCardActor);
            beatCardActor.setZIndex(2 * beatIndex + 1);
            beatCardActor.setScale(CARD_SCALE_TABLE);
            float[] beatPos = TABLE_POSITION.clone();
            beatPos[0] += 90 * beatIndex;
            beatCardActor.addAction(Actions.sequence(
                    new GameStateChangedAction(GameState.BEATING),
                    Actions.moveTo(beatPos[0] + TABLE_DELTA[0], beatPos[1] + TABLE_DELTA[1], 0.4f),
                    Actions.delay(0.2f),
                    new GameStateChangedAction(GameState.BEATEN)));
            Player beater = (Player) event.getTarget();
            System.out.printf("%s (%s) beats with %s\n", beater.getName(), beater.getIndex(), beatCard);
            for (int i = 0; i < beater.getHand().size(); i++) {
                CardActor cardActor = cardActors.get(beater.getHand().get(i));
                float[] position = (beater.getIndex() == 0 ? PLAYER_POSITION : AI_POSITION).clone();
                float[] delta = (beater.getIndex() == 0 ? PLAYER_DELTA : AI_DELTA).clone();
                if (beater.getIndex() > 0)
                    position[0] += (beater.getIndex() - 1) * 640 / (PLAYER_COUNT - 2);
                float posX = position[0] + i * delta[0];
                float posY = position[1] + i * delta[1];
                //cardActor.addAction(Actions.moveTo(posX, posY, 0.1f));
                cardActor.setPosition(posX, posY);
                cardActor.setRotation(0.0f);
                cardActor.setScale(beater.getIndex() == 0 ? CARD_SCALE_PLAYER : CARD_SCALE_AI);
                cardActor.setZIndex(i);
            }
            return true;
        }
        if (event instanceof Player.TakeEvent) {
            // TODO: Handle when player takes
            playersSaidDone = 0;
            isPlayerTaking = true;
            Player player = (Player) event.getTarget();
            System.out.printf("%s (%s) decides to take\n",
                    player.getName(), player.getIndex());
            return true;

        }
        if (event instanceof Player.DoneEvent) {
            // TODO: Handle when player says done
            playersSaidDone++;
            currentThrowerIndex += 2;
            currentThrowerIndex %= PLAYER_COUNT;
            Player player = (Player) event.getTarget();
            System.out.printf("%s (%s) says done\n",
                    player.getName(), player.getIndex());
            return true;
        }
        return false;
    }

    public Card[] getAttackCards() {
        return attackCards;
    }

    public Card[] getDefenseCards() {
        return defenseCards;
    }

    public void drawCardsToPlayer(int playerIndex, int cardCount) {
        Player player = players[playerIndex];
        player.addAction(Actions.sequence(
                new GameStateChangedAction(GameState.DRAWING),
                Actions.delay(0.39f),
                new GameStateChangedAction(READY))
        );
        for (int i = 0; i < cardCount; i++) {
            if (deck.getCards().isEmpty())
                break;
            Card card = deck.draw();
            player.addCard(card);
            CardActor cardActor = cardActors.get(card);
            cardActor.setFaceUp(playerIndex == 0);
            float[] position = (playerIndex == 0 ? PLAYER_POSITION : AI_POSITION).clone();
            if (playerIndex > 0)
                position[0] += (playerIndex - 1) * 640 / (PLAYER_COUNT - 2);
            float[] delta = (playerIndex == 0 ? PLAYER_DELTA : AI_DELTA).clone();
            int index = player.getHand().size() - 1;
            float posX = position[0] + index * delta[0];
            float posY = position[1] + index * delta[1];
            cardActor.addAction(Actions.moveTo(posX, posY, 0.4f));
            cardActor.setRotation(0.0f);
            cardActor.setScale(playerIndex == 0 ? CARD_SCALE_PLAYER : CARD_SCALE_AI);
            playerGroups[playerIndex].addActor(cardActor);
            cardActor.setZIndex(index);
        }
    }

    public Player getCurrentAttacker() {
        if (outOfPlay[currentAttackerIndex]) {
            return players[(currentAttackerIndex + 2) % PLAYER_COUNT];
        }
        return players[currentAttackerIndex];
    }

    public Player getCurrentDefender() {
        int currentDefender = (currentAttackerIndex + 1) % PLAYER_COUNT;
        if (outOfPlay[currentDefender]) {
            return players[(currentDefender + 2) % PLAYER_COUNT];
        }
        return players[currentDefender];
    }

    public Player getCurrentThrower() {
        if (outOfPlay[currentThrowerIndex]) {
            return players[(currentThrowerIndex + 2) % PLAYER_COUNT];
        }
        return players[currentThrowerIndex];
    }
}
