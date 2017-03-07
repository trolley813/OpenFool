#include "playerbubbleitem.h"
#include <QBrush>
#include <QFont>
#include <QPainter>
#include <QPen>

const static int BUBBLE_WIDTH = 500, BUBBLE_HEIGHT = 120;
const static QFont BUBBLE_FONT("Times", BUBBLE_HEIGHT / 3, QFont::Bold);

PlayerBubbleItem::PlayerBubbleItem(QPointF pos, QGraphicsItem *parent)
    : QGraphicsEllipseItem(QRectF(pos, QSizeF(BUBBLE_WIDTH, BUBBLE_HEIGHT)),
                           parent)
{
    _textItem = new QGraphicsSimpleTextItem(this);
    _pos = pos;
    _textItem->setPos(
        pos
        + QPointF((BUBBLE_WIDTH - _textItem->boundingRect().width()) / 2,
                  (BUBBLE_HEIGHT + -_textItem->boundingRect().height()) / 2));
    _textItem->setFont(BUBBLE_FONT);
    _textItem->setBrush(
        QBrush(QColor(10, 10, 10, 255), Qt::BrushStyle::SolidPattern));
    _textItem->show();
    setPen(
        QPen(QBrush(QColor(10, 10, 10, 255), Qt::BrushStyle::SolidPattern), 4));
    setBrush(QBrush(QColor(245, 245, 245, 255), Qt::BrushStyle::SolidPattern));
}

QString PlayerBubbleItem::text() const { return _text; }

void PlayerBubbleItem::setText(const QString &text)
{
    _text = text;
    _textItem->setText(text);
    _textItem->setPos(
        _pos
        + QPointF((BUBBLE_WIDTH - _textItem->boundingRect().width()) / 2,
                  (BUBBLE_HEIGHT + -_textItem->boundingRect().height()) / 2));
}

void PlayerBubbleItem::paint(QPainter *painter,
                             const QStyleOptionGraphicsItem *option,
                             QWidget *widget)
{
    painter->setRenderHint(QPainter::Antialiasing);
    QGraphicsEllipseItem::paint(painter, option, widget);
    painter->setRenderHint(QPainter::Antialiasing, false);
}
