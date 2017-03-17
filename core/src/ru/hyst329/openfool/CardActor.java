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
        setSize(face.getWidth(), face.getHeight());
        face.setOrigin(face.getWidth() / 2, face.getHeight() / 2);
        back.setOrigin(back.getWidth() / 2, back.getHeight() / 2);
    }

    @Override
    protected void positionChanged() {
        face.setCenter(getX() + getWidth() * getScaleX() / 2,
                getY() + getHeight() * getScaleY() / 2);
        back.setCenter(getX() + getWidth() * getScaleX() / 2,
                getY() + getHeight() * getScaleY() / 2);
        super.positionChanged();
    }

    @Override
    protected void rotationChanged() {
        face.setRotation(getRotation());
        back.setRotation(getRotation());
        super.rotationChanged();
    }

    @Override
    protected void sizeChanged() {
        face.setSize(getWidth(), getHeight());
        back.setSize(getWidth(), getHeight());
        super.sizeChanged();
    }

    public boolean isFaceUp() {
        return faceUp;
    }

    public void setFaceUp(boolean faceUp) {
        this.faceUp = faceUp;
    }

    @Override
    public void draw(Batch batch, float alpha) {
        super.draw(batch, alpha);
        face.setScale(this.getScaleX(), this.getScaleY());
        back.setScale(this.getScaleX(), this.getScaleY());
        // System.out.printf("actor %s %s %s %s\n", getX(), getY(), getWidth(), getHeight());
        (faceUp ? face : back).draw(batch);
    }

    public Card getCard() {
        return card;
    }
}