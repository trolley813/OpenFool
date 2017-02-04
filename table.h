#ifndef TABLE_H
#define TABLE_H

#include <QObject>
#include <QGraphicsScene>
#include <QGraphicsSimpleTextItem>
#include <QMap>
#include <QPushButton>
#include <QSettings>
#include "carddeck.h"

class Player;

class Table : public QGraphicsScene
{
    Q_OBJECT
public:
    Table(QSettings *settings, QObject *parent = nullptr);
    void newGame();
    void endTurn(int playerIdx);
    int cardsRemaining() { return _deck->cards().length(); }
    QList<Player *> players() const;
    Suit trumpSuit() const;
    Player *currentAttacker();
    Player *currentDefender();
    bool beats(Card playerCard, Card tableCard);
    Player *currentThrower();
    QList<Card> attackCards() const;
    QList<Card> defenseCards() const;
    bool isGameOver();
    void updateNameLabels();

signals:
    void setGameStatusText(QString);

public slots:
    void onCardClick(Card c);
    void onPlayerDone(int playerIdx);
    void onPlayerThrows(int playerIdx, Card c);
    void onPlayerBeats(int playerIdx, Card c);
    void onPlayerTakes(int playerIdx);

protected:
    CardDeck *_deck;
    QMap<Card, CardItem *> _cardItems;
    Suit _trumpSuit;
    QList<Player *> _players;
    QList<QGraphicsSimpleTextItem *> _nameItems;
    QGraphicsSimpleTextItem *_deckCardsItem;
    void drawCardsFromDeck(int playerIdx, int cardCount);
    int _currentAttacker;
    int _currentThrower;
    bool _isPlayerTaking;
    int _playersSaidDone;
    QList<bool> _outOfPlay;
    QList<Card> _discardPile;
    QList<Card> _attackCards;
    QList<Card> _defenseCards;
    QPushButton *_actionButton;
    QList<QString> _playerNames;

protected slots:
    void _updateDeckCardsLabel();
};

#endif // TABLE_H
