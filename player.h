#ifndef PLAYER_H
#define PLAYER_H

#include <QObject>
#include "table.h"

class Player : public QObject
{
    Q_OBJECT
public:
    explicit Player(Table *table, int index, QString name = "");
    int handValue(QList<Card> hand);
    int currentHandValue();
    QString name() const;
    void setName(const QString &name);
    int index() const;
    QList<Card> hand() const;
    void addCard(Card c);
    void startTurn();
    void throwOrDone();
    void tryBeat();
    void clearHand();
    void throwCard(Card c);
    void beatWithCard(Card c);
signals:
    void done();
    void cardThrown(Card);
    void cardBeaten(Card);
    void take();
public slots:
protected:
    QList<Card> _hand;
    Table *_table;
    QString _name;
    int _index;
};

#endif // PLAYER_H
