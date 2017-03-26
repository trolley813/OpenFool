package ru.hyst329.openfool;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by main on 13.03.2017.
 * Licensed under MIT License.
 */

public class Player extends Actor {
    private final ArrayList<Card> hand = new ArrayList<>();
    private final GameScreen gameScreen;
    private String name;
    private final int index;

    private static final int RANK_MULTIPLIER = 100;
    private static final int UNBALANCED_HAND_PENALTY = 200;
    private static final int MANY_CARDS_PENALTY = 600;
    private static final int OUT_OF_PLAY = 30000;

    class CardThrownEvent extends Event {
        private final Card card;

        public CardThrownEvent(Card card) {
            this.card = card;
        }

        public Card getCard() {
            return card;
        }
    }

    class CardBeatenEvent extends Event {
        private final Card card;

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

    enum SortingMode {
        UNSORTED(0),
        SUIT_ASCENDING(1),
        SUIT_DESCENDING(2),
        RANK_ASCENDING(3),
        RANK_DESCENDING(4);

        private final int value;

        SortingMode(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public static SortingMode fromInt(int value) {
            for (SortingMode type : SortingMode.values()) {
                if (type.getValue() == value) {
                    return type;
                }
            }
            return null;
        }
    }

    Player(GameScreen gameScreen, String name, int index) {
        this.gameScreen = gameScreen;
        this.name = name;
        this.index = index;
    }

    private int handValue(ArrayList<Card> hand) {

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

    private int currentHandValue() {
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
            ArrayList<Card> newHand = new ArrayList<>(hand);
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
            if (c != null)
                ranksPresent[c.getRank().getValue() - 1] = true;
        }
        for (Card c : gameScreen.getDefenseCards()) {
            if (c != null)
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
            Card c = hand.get(i);
            Rank r = c.getRank();
            if (!ranksPresent[r.getValue() - 1])
                continue;
            ArrayList<Card> newHand = new ArrayList<>(hand);
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
        ArrayList<Card> handIfTake = new ArrayList<>(hand);
        for (Card c : gameScreen.getAttackCards()) {
            if (c != null) {
                ranksPresent[c.getRank().getValue() - 1] = true;
                handIfTake.add(c);
            }
        }
        for (Card c : gameScreen.getDefenseCards()) {
            if (c != null) {
                ranksPresent[c.getRank().getValue() - 1] = true;
                handIfTake.add(c);
            }
        }
        int maxVal = Integer.MIN_VALUE;
        int cardIdx = -1;
        System.out.print("Attack cards: ");
        for (int i = 0; i < gameScreen.getAttackCards().length; i++) {
            Card card = gameScreen.getAttackCards()[i];
            System.out.printf("%s ", card == null ? "null" : card);
        }
        System.out.println();
        int index = Arrays.asList(gameScreen.getDefenseCards()).indexOf(null);
        Card attack = gameScreen.getAttackCards()[index];
        System.out.printf("Index = %s attack is %s\n", index, attack == null ? "null" : attack);
        for (int i = 0; i < hand.size(); i++) {
            Card c = hand.get(i);
            if (c.beats(attack, gameScreen.getTrumpSuit())) {
                Rank r = c.getRank();
                ArrayList<Card> newHand = new ArrayList<>(hand);
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

    public void clearHand() {
        hand.clear();
    }

    public void throwCard(Card c) {
        boolean[] ranksPresent = new boolean[13];
        for (Card card : gameScreen.getAttackCards()) {
            if (card != null)
                ranksPresent[card.getRank().getValue() - 1] = true;
        }
        for (Card card : gameScreen.getDefenseCards()) {
            if (card != null)
                ranksPresent[card.getRank().getValue() - 1] = true;
        }
        if (hand.contains(c)
                && (ranksPresent[c.getRank().getValue() - 1] ||
                Arrays.equals(gameScreen.getAttackCards(), new Card[6]))) {
            hand.remove(c);
            fire(new CardThrownEvent(c));
        }
    }

    public void beatWithCard(Card c) {
        Card attack = gameScreen.getAttackCards()[Arrays.asList(gameScreen.getDefenseCards()).indexOf(null)];
        if (hand.contains(c) && c.beats(attack, gameScreen.getTrumpSuit())) {
            hand.remove(c);
            fire(new CardBeatenEvent(c));
        }
    }

    public void sayDone() {
        fire(new DoneEvent());
    }

    public void sayTake() {
        fire(new TakeEvent());
    }

    public void sortCards(final SortingMode sortingMode) {
        if (sortingMode == SortingMode.UNSORTED) {
            return;
        }
        Collections.sort(hand, new Comparator<Card>() {
            @Override
            public int compare(Card c1, Card c2) {
                if (c1 == c2) {
                    return 0;
                } else {
                    int v1 = (c1.getSuit().getValue() + (3 - gameScreen.getTrumpSuit().getValue())) % 4;
                    int v2 = (c2.getSuit().getValue() + (3 - gameScreen.getTrumpSuit().getValue())) % 4;
                    int r1 = (c1.getRank().getValue() + 11) % 13;
                    int r2 = (c2.getRank().getValue() + 11) % 13;
                    switch (sortingMode) {
                        case SUIT_ASCENDING:
                            if (v1 < v2) return -1;
                            if (v1 > v2) return 1;
                            return (r1 < r2 ? -1 : 1);
                        case SUIT_DESCENDING:
                            if (v2 < v1) return -1;
                            if (v2 > v1) return 1;
                            return (r2 < r1 ? -1 : 1);
                        case RANK_ASCENDING:
                            if (r1 < r2) return -1;
                            if (r1 > r2) return 1;
                            return (v1 < v2 ? -1 : 1);
                        case RANK_DESCENDING:
                            if (r2 < r1) return -1;
                            if (r2 > r1) return 1;
                            return (v2 < v1 ? -1 : 1);
                    }
                }
                return 0;
            }
        });
    }

}
