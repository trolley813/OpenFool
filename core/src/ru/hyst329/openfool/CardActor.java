package ru.hyst329.openfool;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.scenes.scene2d.Actor;

/**
 * Created by main on 13.03.2017.
 */

public class CardActor extends Actor {
    private OpenFoolGame game;
    private boolean faceUp;
    private Card card;
    private Sprite face, back;

    public CardActor(OpenFoolGame game, Card card, String deckStyle) {
        this.game = game;
        this.card = card;
        this.face = new Sprite(game.assetManager.get(String.format("%s/%s.png", deckStyle, card), Texture.class));
        this.back = new Sprite(game.assetManager.get(String.format("%s/back.png", deckStyle), Texture.class));
    }

    public boolean isFaceUp() {
        return faceUp;
    }

    public void setFaceUp(boolean faceUp) {
        this.faceUp = faceUp;
    }

    @Override
    public void draw(Batch batch, float alpha) {
        (faceUp ? face : back).draw(batch);
    }

}
