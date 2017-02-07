#include "playerbubbleitem.h"
#include <QFont>

const static int BUBBLE_WIDTH = 500, BUBBLE_HEIGHT = 120;
const static QFont BUBBLE_FONT("Times", BUBBLE_HEIGHT / 3, QFont::Bold);

PlayerBubbleItem::PlayerBubbleItem(QPointF pos, QGraphicsItem *parent)
    : QGraphicsEllipseItem(QRectF(pos, QSizeF(BUBBLE_WIDTH, BUBBLE_HEIGHT)),
                           parent)
{
    _textItem = new QGraphicsSimpleTextItem();
    _textItem->setFont(BUBBLE_FONT);
}

QString PlayerBubbleItem::text() const { return _text; }

void PlayerBubbleItem::setText(const QString &text)
{
    _text = text;
    _textItem->setText(text);
}
