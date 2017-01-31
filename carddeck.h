#ifndef CARDDECK_H
#define CARDDECK_H

#include "carditem.h"
#include <QList>

class CardDeck : public QObject
{
    Q_OBJECT

public:
    CardDeck(QObject *parent = 0, bool pristine = false,
             Rank lowestRank = RANK_TWO, int jokers = 0);
    void shuffle();
    void reset();
    Card draw();
    bool empty();
    QList<Card> cards() const;
signals:
    void cardDrawn();

protected:
    QList<Card> _cards;
    Rank _lowestRank;
    int _jokers;
};

#endif // CARDDECK_H
