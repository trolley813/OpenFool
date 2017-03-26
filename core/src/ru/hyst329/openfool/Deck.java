package ru.hyst329.openfool;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by main on 13.03.2017.
 * Licensed under MIT License.
 */

class Deck {
    private ArrayList<Card> cards;

    Deck() {
        this.cards = new ArrayList<>();
        this.reset();
        this.shuffle();
    }

    private void reset() {
        this.cards.clear();
        this.cards = new ArrayList<>();
        for (Rank r : Rank.values())
            for (Suit s : Suit.values())
                cards.add(new Card(s, r));
    }

    private void shuffle() {
        SecureRandom random = new SecureRandom();
        Collections.shuffle(cards, random);
    }

    public Card draw() {
        try {
            return cards.remove(cards.size() - 1);
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
    }

    public ArrayList<Card> getCards() {
        return cards;
    }
}
