package ru.hyst329.openfool;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by main on 13.03.2017.
 * Licensed under MIT License.
 */

public class Player extends Actor {
    private ArrayList<Card> hand = new ArrayList<Card>();
    private GameScreen gameScreen;
    private String name;
    private int index;

    private static final int RANK_MULTIPLIER = 100;
    private static final int UNBALANCED_HAND_PENALTY = 200;
    private static final int MANY_CARDS_PENALTY = 600;
    private static final int OUT_OF_PLAY = 30000;

    class CardThrownEvent extends Event {
        private Card card;

        public CardThrownEvent(Card card) {
            this.card = card;
        }

        public Card getCard() {
            return card;
        }
    }

    class CardBeatenEvent extends Event {
        private Card card;

        public CardBeatenEvent(Card card) {
            this.card = card;
        }

        public Card getCard() {
            return card;
        }
    }

    class DoneEvent extends Event {
    }

    class TakeEvent extends Event {
    }

    public Player(GameScreen gameScreen, String name, int index) {
        this.gameScreen = gameScreen;
        this.name = name;
        this.index = index;
    }

    public int handValue(ArrayList<Card> hand) {

        if (gameScreen.cardsRemaining() == 0 && hand.size() == 0) {
            return OUT_OF_PLAY;
        }
        double[] bonuses = {0, 0, 0.5, 0.75, 1.25}; // for cards of same rank
        int res = 0;
        int[] countsByRank = new int[13];
        int[] countsBySuit = new int[4];
        for (Card c : hand) {
            Rank r = c.getRank();
            Suit s = c.getSuit();
            res += (r == Rank.ACE ? 6 : r.getValue() - 8) * RANK_MULTIPLIER;
            if (s == gameScreen.getTrumpSuit())
                res += 13 * RANK_MULTIPLIER;
            countsByRank[r.getValue() - 1]++;
            countsBySuit[s.getValue()]++;
        }
        for (int i = 1; i <= 13; i++) {
            res += Math.max(i == 1 ? 6 : i - 8, 1)
                    * bonuses[countsByRank[i - 1]];
        }
        double avgSuit = 0;
        for (Card c : hand) {
            if (c.getSuit() != gameScreen.getTrumpSuit())
                avgSuit++;
        }
        avgSuit /= 3;
        for (Suit s : Suit.values()) {
            if (s != gameScreen.getTrumpSuit()) {
                double dev = Math.abs((countsBySuit[s.getValue()] - avgSuit) / avgSuit);
                res -= UNBALANCED_HAND_PENALTY * dev;
            }
        }
        int cardsInPlay = gameScreen.cardsRemaining();
        for (Player p : gameScreen.getPlayers())
            cardsInPlay += p.hand.size();
        cardsInPlay -= hand.size();
        double cardRatio = cardsInPlay != 0 ? hand.size() / cardsInPlay : 10.0;
        res += (0.25 - cardRatio) * MANY_CARDS_PENALTY;
        return res;
    }

    public int currentHandValue() {
        return handValue(hand);
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getIndex() {
        return index;
    }

    public ArrayList<Card> getHand() {
        return hand;
    }

    public void addCard(Card c) {
        hand.add(c);
    }

    public void startTurn() {
        double[] bonuses = {0, 0, 1, 1.5, 2.5};
        int[] countsByRank = new int[13];
        for (Card c : hand) {
            countsByRank[c.getRank().getValue() - 1]++;
        }
        int maxVal = Integer.MIN_VALUE;
        int cardIdx = -1;
        for (int i = 0; i < hand.size(); i++) {
            ArrayList<Card> newHand = new ArrayList<Card>(hand);
            Card c = hand.get(i);
            newHand.remove(i);
            Rank r = c.getRank();
            int newVal = handValue(newHand)
                    + (int) Math.round(bonuses[countsByRank[r.getValue() - 1]]
                    * (r == Rank.ACE ? 6 : r.getValue() - 8) * RANK_MULTIPLIER);
            if (newVal > maxVal) {
                maxVal = newVal;
                cardIdx = i;
            }
        }
        Card c = hand.get(cardIdx);
        hand.remove(cardIdx);
        fire(new CardThrownEvent(c));
    }

    public void throwOrDone() {
        boolean[] ranksPresent = new boolean[13];
        for (Card c : gameScreen.getAttackCards()) {
            ranksPresent[c.getRank().getValue() - 1] = true;
        }
        for (Card c : gameScreen.getDefenseCards()) {
            ranksPresent[c.getRank().getValue() - 1] = true;
        }
        // TODO: Remove duplication
        double[] bonuses = {0, 0, 1, 1.5, 2.5};
        int[] countsByRank = new int[13];
        for (Card c : hand) {
            countsByRank[c.getRank().getValue() - 1]++;
        }
        int maxVal = Integer.MIN_VALUE;
        int cardIdx = -1;
        for (int i = 0; i < hand.size(); i++) {
            ArrayList<Card> newHand = new ArrayList<Card>(hand);
            Card c = hand.get(i);
            Rank r = c.getRank();
            if (!ranksPresent[r.getValue() - 1])
                continue;
            newHand.remove(i);
            int newVal = handValue(newHand)
                    + (int) Math.round(bonuses[countsByRank[r.getValue() - 1]]
                    * (r == Rank.ACE ? 6 : r.getValue() - 8) * RANK_MULTIPLIER);
            if (newVal > maxVal) {
                maxVal = newVal;
                cardIdx = i;
            }
        }
        int PENALTY_BASE = 1200, PENALTY_DELTA = 50;
        if (currentHandValue() - maxVal
                < PENALTY_BASE - PENALTY_DELTA * gameScreen.cardsRemaining()
                && cardIdx >= 0) {
            Card c = hand.get(cardIdx);
            hand.remove(cardIdx);
            fire(new CardThrownEvent(c));
        } else {
            fire(new DoneEvent());
        }
    }


    public void tryBeat() {
        int RANK_PRESENT_BONUS = 300;
        boolean[] ranksPresent = new boolean[13];
        ArrayList<Card> handIfTake = new ArrayList<Card>(hand);
        for (Card c : gameScreen.getAttackCards()) {
            ranksPresent[c.getRank().getValue() - 1] = true;
            handIfTake.add(c);
        }
        for (Card c : gameScreen.getDefenseCards()) {
            ranksPresent[c.getRank().getValue() - 1] = true;
            handIfTake.add(c);
        }
        int maxVal = Integer.MIN_VALUE;
        int cardIdx = -1;
        Card attack = gameScreen.getAttackCards()[Arrays.asList(gameScreen.getDefenseCards()).indexOf(null)];
        for (int i = 0; i < hand.size(); i++) {
            Card c = hand.get(i);
            if (c.beats(attack, gameScreen.getTrumpSuit())) {
                Rank r = c.getRank();
                ArrayList<Card> newHand = new ArrayList<Card>(hand);
                newHand.remove(i);
                int newVal = handValue(newHand)
                        + RANK_PRESENT_BONUS * (ranksPresent[r.getValue() - 1] ? 1 : 0);
                if (newVal > maxVal) {
                    maxVal = newVal;
                    cardIdx = i;
                }
            }
        }
        int PENALTY = 800, TAKE_PENALTY_BASE = 2000, TAKE_PENALTY_DELTA = 40;
        if (((currentHandValue() - maxVal < PENALTY)
                || (handValue(handIfTake) - maxVal
                < TAKE_PENALTY_BASE
                - TAKE_PENALTY_DELTA * gameScreen.cardsRemaining()
                || gameScreen.cardsRemaining() == 0)) && cardIdx >= 0) {
            Card c = hand.get(cardIdx);
            hand.remove(cardIdx);
            fire(new CardBeatenEvent(c));
        } else {
            fire(new TakeEvent());
        }
    }
}
