#ifndef CARDITEM_H
#define CARDITEM_H

#include <QObject>
#include <QGraphicsSvgItem>
#include "card.h"

class CardItem : public QGraphicsSvgItem
{
    Q_OBJECT
    Q_PROPERTY(bool faceUp READ faceUp WRITE setFaceUp)

public:
    CardItem(Card card, QString deckDesign, QGraphicsItem *parent = Q_NULLPTR);
    bool faceUp();
    void setFaceUp(bool value);
    void mousePressEvent(QGraphicsSceneMouseEvent *event) override;

protected:
    QSvgRenderer *face;
    QSvgRenderer *back;
    bool _faceUp;
    Card _card;
signals:
    void cardClicked(Card c);
};

#endif // CARDITEM_H
