package ru.hyst329.openfool;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by main on 13.03.2017.
 */

public class Deck {
    ArrayList<Card> cards;

    public Deck() {
        this.reset();
        this.shuffle();
    }

    public void reset() {
        this.cards.clear();
        this.cards = new ArrayList<Card>();
        for (Rank r: Rank.values())
            for (Suit s: Suit.values())
                cards.add(new Card(s, r));
    }

    public void shuffle() {
        SecureRandom random = new SecureRandom();
        Collections.shuffle(cards, random);
    }

    public Card draw() {
        try {
            return cards.remove(cards.size() - 1);
        }
        catch (IndexOutOfBoundsException e) {
            return null;
        }
    }
}
