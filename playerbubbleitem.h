#ifndef PLAYERBUBBLEITEM_H
#define PLAYERBUBBLEITEM_H

#include <QObject>
#include <QGraphicsEllipseItem>
#include <QGraphicsSimpleTextItem>

class PlayerBubbleItem : public QObject, public QGraphicsEllipseItem
{
    Q_OBJECT

public:
    PlayerBubbleItem(QPointF pos, QGraphicsItem *parent = nullptr);

    QString text() const;
    void setText(const QString &text);

protected:
    QGraphicsSimpleTextItem *_textItem;
    QString _text;
};

#endif // PLAYERBUBBLEITEM_H
