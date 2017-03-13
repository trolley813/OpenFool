package ru.hyst329.openfool;

/**
 * Created by main on 13.03.2017.
 * Licensed under MIT License.
 */

public enum Suit {
    SPADES(0),
    DIAMONDS(1),
    CLUBS(2),
    HEARTS(3);
    private final int value;

    Suit(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
