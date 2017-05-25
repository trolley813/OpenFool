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
    private val face: Sprite = Sprite(game.assetManager.get("decks/$deckStyle/$card.png", Texture::class.java))
    private val back: Sprite = Sprite(game.assetManager.get("decks/$deckStyle/back.png", Texture::class.java))

    init {
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
