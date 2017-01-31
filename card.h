#ifndef CARD_H
#define CARD_H

#include <QString>

enum Suit {
    SUIT_SPADES = 0,
    SUIT_CLUBS = 1,
    SUIT_DIAMONDS = 2,
    SUIT_HEARTS = 3
};

enum Rank {
    RANK_JOKER = 0,
    RANK_ACE = 1,
    RANK_TWO = 2,
    RANK_THREE = 3,
    RANK_FOUR = 4,
    RANK_FIVE = 5,
    RANK_SIX = 6,
    RANK_SEVEN = 7,
    RANK_EIGHT = 8,
    RANK_NINE = 9,
    RANK_TEN = 10,
    RANK_JACK = 11,
    RANK_QUEEN = 12,
    RANK_KING = 13
};

class Card
{
public:
    Card(Suit suit, Rank rank);
    Suit suit();
    Rank rank();
    void setSuit(Suit suit);
    void setRank(Rank rank);
    QString fileName();
    friend bool operator<(const Card &c1, const Card &c2);
    friend bool operator<=(const Card &c1, const Card &c2);
    friend bool operator>(const Card &c1, const Card &c2);
    friend bool operator>=(const Card &c1, const Card &c2);
    friend bool operator==(const Card &c1, const Card &c2);
    friend bool operator!=(const Card &c1, const Card &c2);

protected:
    Suit _suit;
    Rank _rank;
};

#endif // CARD_H
