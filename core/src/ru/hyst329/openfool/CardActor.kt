package ru.hyst329.openfool

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.scenes.scene2d.Actor

/**
 * Created by main on 13.03.2017.
 * Licensed under MIT License.
 */

internal class CardActor(game: OpenFoolGame, val card: Card, deckStyle: String) : Actor() {
    var isFaceUp: Boolean = false
    private val face: Sprite
    private val back: Sprite

    init {
        this.face = Sprite(game.assetManager.get(String.format("decks/%s/%s.png", deckStyle, card), Texture::class.java))
        this.back = Sprite(game.assetManager.get(String.format("decks/%s/back.png", deckStyle), Texture::class.java))
        setSize(face.width, face.height)
        face.setOrigin(face.width / 2, face.height / 2)
        back.setOrigin(back.width / 2, back.height / 2)
    }

    override fun positionChanged() {
        face.setCenter(x + width * scaleX / 2,
                y + height * scaleY / 2)
        back.setCenter(x + width * scaleX / 2,
                y + height * scaleY / 2)
        super.positionChanged()
    }

    override fun rotationChanged() {
        face.rotation = rotation
        back.rotation = rotation
        super.rotationChanged()
    }

    override fun sizeChanged() {
        face.setSize(width, height)
        back.setSize(width, height)
        super.sizeChanged()
    }

    override fun draw(batch: Batch?, alpha: Float) {
        super.draw(batch, alpha)
        face.setScale(this.scaleX, this.scaleY)
        back.setScale(this.scaleX, this.scaleY)
        // System.out.printf("actor %s %s %s %s\n", getX(), getY(), getWidth(), getHeight());
        (if (isFaceUp) face else back).draw(batch!!)
    }
}
