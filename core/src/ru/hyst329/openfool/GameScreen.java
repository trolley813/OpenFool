package ru.hyst329.openfool;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Created by main on 13.03.2017.
 * Licensed under MIT License.
 */

public class GameScreen implements Screen, EventListener {
    private Stage stage;
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

    private static final float[] DECK_POSITION = {60, 240};
    private static final float[] DISCARD_PILE_POSITION = {640, 120};
    private static final float[] PLAYER_POSITION = {240, 80};
    private static final float[] AI_POSITION = {60, 400};
    private static final float[] TABLE_POSITION = {200, 280};
    private static final float[] TABLE_DELTA = {10, -10};
    private static final float[] PLAYER_DELTA = {60, 0};
    private static final float[] AI_DELTA = {5, -5};
    private boolean[] outOfPlay = new boolean[PLAYER_COUNT];
    private ArrayList<Card> discardPile = new ArrayList<Card>();

    public GameScreen(OpenFoolGame game) {
        this.game = game;
        // Initialise the stage
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);
        // Initialise card actors
        ArrayList<Card> cards = deck.getCards();
        for (int i = 0; i < cards.size(); i++) {
            Card c = cards.get(i);
            CardActor cardActor = new CardActor(game, c, "rus");
            cardActors.put(c, cardActor);
            stage.addActor(cardActor);
            cardActor.setZIndex(i);
        }
        // Initialise players
        // TODO: Replace with settings
        String[] playerNames = {"South", "West", "North", "East"};
        players = new Player[PLAYER_COUNT];
        for (int i = 0; i < PLAYER_COUNT; i++) {
            players[i] = new Player(this, playerNames[i], i);
            stage.addActor(players[i]);
        }
        // Starting the game
        for (CardActor cardActor : cardActors.values()) {
            cardActor.setFaceUp(false);
            cardActor.setScale(CARD_SCALE_TABLE);
            cardActor.setPosition(DECK_POSITION[0], DECK_POSITION[1]);
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
        System.out.println(String.format("%s (%d) has the lowest trump %s",
                players[firstAttacker].getName(), players[firstAttacker].getIndex(), lowestTrump));
        currentAttackerIndex = firstAttacker;
        currentThrowerIndex = firstAttacker;
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.5f, 1, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        // TODO: Actual game logic
        int opponents = (outOfPlay[currentAttackerIndex] ? 0 : 1)
                + (outOfPlay[(currentAttackerIndex + 2) % PLAYER_COUNT] ? 0 : 1);
        int throwLimit = Math.min(DEAL_LIMIT, getCurrentDefender().getHand().size());
        System.out.println(opponents + " opponents, " + playersSaidDone + " said done");
        while (playersSaidDone < opponents) {
            int currentPlayersDone = playersSaidDone;
            if (getCurrentAttacker().getIndex() != 0) {
                getCurrentAttacker().startTurn();
            }
            if (!isPlayerTaking) {
                if (getCurrentDefender().getIndex() != 0) {
                    getCurrentDefender().tryBeat();
                }
            }
            if (getCurrentDefender().getHand().size() == 0
                    || attackCards[throwLimit - 1] != null)
                break;
            if (getCurrentThrower().getIndex() != 0) {
                getCurrentThrower().throwOrDone();
            }
            if (playersSaidDone > currentPlayersDone) {
                currentThrowerIndex += 2;
                currentThrowerIndex %= PLAYER_COUNT;
            }
        }
        boolean playerTook = isPlayerTaking;
        endTurn(isPlayerTaking ? getCurrentDefender().getIndex() : -1);
        currentAttackerIndex += (playerTook ? 2 : 1);
        currentAttackerIndex %= PLAYER_COUNT;
        currentThrowerIndex = currentAttackerIndex;
        // Draw stage
        stage.act(delta);
        stage.draw();
        // Draw player labels
        game.batch.begin();
        for (int i = 0; i < PLAYER_COUNT; i++) {
            float[] position = (i == 0 ? PLAYER_POSITION : AI_POSITION).clone();
            if (i > 0)
                position[0] += (i - 1) * 640 / (PLAYER_COUNT - 2);
            position[1] += 320 * (i == 0 ? CARD_SCALE_PLAYER : CARD_SCALE_AI);
            game.font.draw(game.batch, String.format("%s: %s", players[i].getName(), players[i].getHand().size()),
                    position[0], position[1]);

        }
        // Check if the game is over
        if (isGameOver()) {
            game.setScreen(new MainMenuScreen(game));
            dispose();
        }
        game.batch.end();
    }

    @Override
    public void resize(int width, int height) {

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
        ArrayList<Card> tableCards = new ArrayList<Card>();
        for (int i = 0; i < attackCards.length; i++) {
            if (attackCards[i] != null) {
                tableCards.add(attackCards[i]);
                attackCards[i] = null;
            }
            if (defenseCards[i] != null) {
                tableCards.add(defenseCards[i]);
                defenseCards[i] = null;
            }
        }
        if (playerIndex < 0) {
            for (Card card : tableCards) {
                CardActor cardActor = cardActors.get(card);
                discardPile.add(card);
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
    }

    public boolean isGameOver() {
        // TODO: Generalise
        return (outOfPlay[0] && outOfPlay[2]) || (outOfPlay[1] && outOfPlay[3]);
    }

    public Suit getTrumpSuit() {
        return trumpSuit;
    }

    public int cardsRemaining() {
        return 0;
    }

    public Player[] getPlayers() {
        return players;
    }

    @Override
    public boolean handle(Event event) {
        if (event.getClass().isAssignableFrom(Player.CardThrownEvent.class)) {
            // TODO: Handle when card is thrown
            playersSaidDone = 0;
            playersSaidDone = 0;
            int throwIndex = 0;
            while (defenseCards[throwIndex] != null) throwIndex++;
            Card throwCard = ((Player.CardBeatenEvent) event).getCard();
            CardActor throwCardActor = cardActors.get(throwCard);
            throwCardActor.setFaceUp(true);
            throwCardActor.setZIndex(1);
            float[] throwPos = TABLE_POSITION.clone();
            throwPos[0] += 90 * throwIndex;
            throwCardActor.addAction(Actions.moveTo(throwPos[0] + TABLE_DELTA[0], throwPos[1] + TABLE_DELTA[1]));
            Player thrower = (Player) event.getTarget();
            System.out.printf(String.format("%s (%d) throws %s",
                    thrower.getName(), thrower.getIndex(), throwCard));
            for (int i = 0; i < thrower.getHand().size(); i++) {
                CardActor cardActor = cardActors.get(thrower.getHand().get(i));
                float[] position = (thrower.getIndex() == 0 ? PLAYER_POSITION : AI_POSITION).clone();
                float[] delta = (thrower.getIndex() == 0 ? PLAYER_DELTA : AI_DELTA).clone();
                float posX = position[0] + i * delta[0];
                float posY = position[1] + i * delta[1];
                cardActor.addAction(Actions.moveTo(posX, posY, 0.4f));
                cardActor.setRotation(0.0f);
                cardActor.setScale(thrower.getIndex() == 0 ? CARD_SCALE_PLAYER : CARD_SCALE_AI);
                cardActor.setZIndex(i);
            }
        }
        if (event.getClass().isAssignableFrom(Player.CardBeatenEvent.class)) {
            // TODO: Handle when card is beaten
            playersSaidDone = 0;
            int index = 0;
            while (defenseCards[index] != null) index++;
            Card beatCard = ((Player.CardBeatenEvent) event).getCard();
            CardActor beatCardActor = cardActors.get(beatCard);
            beatCardActor.setFaceUp(true);
            beatCardActor.setZIndex(1);
            float[] beatPos = TABLE_POSITION.clone();
            beatPos[0] += 90 * index;
            beatCardActor.addAction(Actions.moveTo(beatPos[0] + TABLE_DELTA[0], beatPos[1] + TABLE_DELTA[1]));
            Player beater = (Player) event.getTarget();
            System.out.printf(String.format("%s (%d) beats with %s",
                    beater.getName(), beater.getIndex(), beatCard));
            for (int i = 0; i < beater.getHand().size(); i++) {
                CardActor cardActor = cardActors.get(beater.getHand().get(i));
                float[] position = (beater.getIndex() == 0 ? PLAYER_POSITION : AI_POSITION).clone();
                float[] delta = (beater.getIndex() == 0 ? PLAYER_DELTA : AI_DELTA).clone();
                float posX = position[0] + i * delta[0];
                float posY = position[1] + i * delta[1];
                cardActor.addAction(Actions.moveTo(posX, posY, 0.4f));
                cardActor.setRotation(0.0f);
                cardActor.setScale(beater.getIndex() == 0 ? CARD_SCALE_PLAYER : CARD_SCALE_AI);
                cardActor.setZIndex(i);
            }
        }
        if (event.getClass().isAssignableFrom(Player.TakeEvent.class)) {
            // TODO: Handle when player takes
            playersSaidDone = 0;
            isPlayerTaking = true;
            Player player = (Player) event.getTarget();
            System.out.printf(String.format("%s (%d) decides to take",
                    player.getName(), player.getIndex()));

        }
        if (event.getClass().isAssignableFrom(Player.DoneEvent.class)) {
            // TODO: Handle when player says done
            playersSaidDone++;
            Player player = (Player) event.getTarget();
            System.out.printf(String.format("%s (%d) says done",
                    player.getName(), player.getIndex()));
        }
        return true;
    }

    public Card[] getAttackCards() {
        return attackCards;
    }

    public Card[] getDefenseCards() {
        return defenseCards;
    }

    public void drawCardsToPlayer(int playerIndex, int cardCount) {
        for (int i = 0; i < cardCount; i++) {
            if (deck.getCards().isEmpty())
                break;
            Card card = deck.draw();
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
