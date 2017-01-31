#include "card.h"

Card::Card(Suit suit, Rank rank) : _suit(suit), _rank(rank) {}

Suit Card::suit() { return _suit; }
Rank Card::rank() { return _rank; }
void Card::setSuit(Suit suit) { _suit = suit; }
void Card::setRank(Rank rank) { _rank = rank; }

QString Card::fileName()
{
    QString res;
    res.setNum(_rank);
    switch (_suit) {
    case SUIT_SPADES:
        res += "s";
        break;
    case SUIT_CLUBS:
        res += "c";
        break;
    case SUIT_DIAMONDS:
        res += "d";
        break;
    case SUIT_HEARTS:
        res += "h";
        break;
    }
    return res;
}

bool operator<(const Card &c1, const Card &c2)
{
    return (c1._rank < c2._rank)
           || (c1._rank == c2._rank && c1._suit < c2._suit);
}

bool operator>(const Card &c1, const Card &c2)
{
    return (c1._rank > c2._rank)
           || (c1._rank == c2._rank && c1._suit > c2._suit);
}

bool operator<=(const Card &c1, const Card &c2)
{
    return (c1 < c2) || (c1 == c2);
}

bool operator>=(const Card &c1, const Card &c2)
{
    return (c1 > c2) || (c1 == c2);
}

bool operator==(const Card &c1, const Card &c2)
{
    return c1._rank == c2._rank && c1._suit == c2._suit;
}

bool operator!=(const Card &c1, const Card &c2) { return !(c1 == c2); }
