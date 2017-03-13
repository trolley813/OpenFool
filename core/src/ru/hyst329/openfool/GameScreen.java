package ru.hyst329.openfool;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;

import java.util.ArrayList;

/**
 * Created by main on 13.03.2017.
 * Licensed under MIT License.
 */

public class GameScreen implements Screen, EventListener {
    private Suit trumpSuit;
    private Player[] players;
    private Card[] attackCards, defenseCards;

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {

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
        Class eventClass = event.getClass();
        if(eventClass.isAssignableFrom(Player.CardThrownEvent.class)) {
            // TODO: Handle when card is thrown
        }
        return true;
    }

    public Card[] getAttackCards() {
        return attackCards;
    }
    public Card[] getDefenseCards() {
        return defenseCards;
    }
}
