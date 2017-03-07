#include "carditem.h"
#include <QSvgRenderer>

CardItem::CardItem(Card card, QString deckDesign, QGraphicsItem *parent)
    : QGraphicsSvgItem(parent), _card(card)
{
    face = new QSvgRenderer(QString(":/decks/%1/res/%1/%2.svg")
                                .arg(deckDesign)
                                .arg(card.fileName()));
    back = new QSvgRenderer(
        QString(":/decks/%1/res/%1/back.svg").arg(deckDesign));
    setFaceUp(false);
}

bool CardItem::faceUp() { return _faceUp; }

void CardItem::setFaceUp(bool value)
{
    _faceUp = value;
    setSharedRenderer(value ? face : back);
}

void CardItem::mousePressEvent(QGraphicsSceneMouseEvent *event)
{
    // setFaceUp(!_faceUp);
    emit cardClicked(_card);
}
