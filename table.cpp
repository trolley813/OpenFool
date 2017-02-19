#include "table.h"
#include "carditem.h"
#include "player.h"
#include <QParallelAnimationGroup>
#include <QGraphicsSimpleTextItem>
#include <QSequentialAnimationGroup>
#include <QPropertyAnimation>
#include <QGraphicsView>
#include <QDebug>
#include <QThread>
#include <QPushButton>
#include <QGraphicsProxyWidget>
#include <QEventLoop>
#include <QMessageBox>
#include <QApplication>

const int CARD_WIDTH = 360, CARD_HEIGHT = 540;
const QPointF TALON_LOCATION(-4 * CARD_WIDTH, 0);
const QPointF DISCARD_PILE_LOCATION(4 * CARD_WIDTH, 0);
const int PLAYERS_COUNT
    = 4; // in future may be adapted to other number of players
const int DEAL_LIMIT = 6;
const QPointF PLAYER_LOCATIONS[PLAYERS_COUNT]
    = {{0, 2 * CARD_HEIGHT},
       {-3 * CARD_WIDTH, -1.5 * CARD_HEIGHT},
       {0, -2.5 * CARD_HEIGHT},
       {3 * CARD_WIDTH, -1.5 * CARD_HEIGHT}};
const QPointF BUTTON_LOCATION = {-4 * CARD_WIDTH, 2 * CARD_HEIGHT};
const QString PLAYER_NAMES[PLAYERS_COUNT] = {"South", "West", "North", "East"};
const QPointF DELTA_HUMAN(0.25 * CARD_WIDTH, 0),
    DELTA_AI(0.05 * CARD_WIDTH, 0.05 * CARD_WIDTH);
const QPointF CARD_FIELD_LOCATIONS[DEAL_LIMIT]
    = {{-1 * CARD_WIDTH, -1 * CARD_HEIGHT},
       {0.5 * CARD_WIDTH, -1 * CARD_HEIGHT},
       {2 * CARD_WIDTH, -1 * CARD_HEIGHT},
       {-1 * CARD_WIDTH, 0.5 * CARD_HEIGHT},
       {0.5 * CARD_WIDTH, 0.5 * CARD_HEIGHT},
       {2 * CARD_WIDTH, 0.5 * CARD_HEIGHT}};

Table::Table(QSettings *settings, QObject *parent) : QGraphicsScene(parent)
{
    _deck = new CardDeck(this);
    setBackgroundBrush(
        QBrush(QColor(86, 156, 30, 230), Qt::BrushStyle::SolidPattern));
    setSceneRect(QRectF(QPointF(-5 * CARD_WIDTH, -3 * CARD_HEIGHT),
                        QPointF(+5 * CARD_WIDTH, +3 * CARD_HEIGHT)));
    for (Card c : _deck->cards()) {
        CardItem *ci
            = new CardItem(c, settings->value("cards/deck", "rus").toString());
        ci->setPos(TALON_LOCATION);
        ci->hide();
        QObject::connect(ci, SIGNAL(cardClicked(Card)), this,
                         SLOT(onCardClick(Card)));
        this->addItem(ci);
        _cardItems[c] = ci;
    }
    QFont mainFont("Times", CARD_HEIGHT / 10, QFont::Bold);
    for (int i = 0; i < PLAYERS_COUNT; i++) {
        _playerNames << settings->value(QString("players/name%1").arg(i + 1),
                                        PLAYER_NAMES[i]).toString();
        _players << new Player(this, i, _playerNames[i]);
        QGraphicsSimpleTextItem *nameItem
            = new QGraphicsSimpleTextItem(_playerNames[i]);
        nameItem->setPos(PLAYER_LOCATIONS[i] - QPointF(0, 0.15 * CARD_HEIGHT));
        this->addItem(nameItem);
        _nameItems << nameItem;
        nameItem->setFont(mainFont);
        nameItem->hide();
        PlayerBubbleItem *bubble = new PlayerBubbleItem(
            PLAYER_LOCATIONS[i] - QPointF(0, 0.5 * CARD_HEIGHT));
        this->addItem(bubble);
        _playerBubbles << bubble;
        bubble->hide();
        _outOfPlay << false;
    }
    _deckCardsItem
        = new QGraphicsSimpleTextItem(QString::number(_deck->cards().length()));
    _deckCardsItem->setPos(TALON_LOCATION - QPointF(0, 0.15 * CARD_HEIGHT));
    _deckCardsItem->setFont(mainFont);
    _deckCardsItem->hide();
    this->addItem(_deckCardsItem);

    // Add action button
    _actionButton = new QPushButton(tr("Action"));
    _actionButton->setFont(mainFont);
    QGraphicsProxyWidget *proxy = this->addWidget(_actionButton);
    proxy->setGeometry(
        QRectF(BUTTON_LOCATION, QSizeF(CARD_WIDTH, 0.3 * CARD_HEIGHT)));

    // Connect signals
    connect(_deck, SIGNAL(cardDrawn()), this, SLOT(_updateDeckCardsLabel()));
    for (int i = 0; i < PLAYERS_COUNT; i++) {
        connect(_players[i], &Player::done, [=]() { this->onPlayerDone(i); });
        connect(_players[i], &Player::cardThrown,
                [=](Card c) { this->onPlayerThrows(i, c); });
        connect(_players[i], &Player::cardBeaten,
                [=](Card c) { this->onPlayerBeats(i, c); });
        connect(_players[i], &Player::take, [=]() { this->onPlayerTakes(i); });
    }
}

void Table::newGame()
{
    // Show player names
    for (auto nameItem : _nameItems)
        nameItem->show();
    _deckCardsItem->show();

    // Prepare the deck
    for (auto ci : _cardItems.values()) {
        ci->setFaceUp(false);
        ci->show();
        ci->setPos(TALON_LOCATION);
        ci->setRotation(0);
    }
    _deck->reset();
    _deck->shuffle();
    for (int i = 0; i < _deck->cards().length(); i++) {
        _cardItems[_deck->cards()[i]]->setZValue(i);
    }

    for (Player *p : _players) {
        p->clearHand();
        _outOfPlay[p->index()] = false;
    }

    // Determine the trump
    Card trump = _deck->cards()[0];
    CardItem *trumpItem = _cardItems[trump];
    trumpItem->setFaceUp(true);
    _trumpSuit = trump.suit();
    QParallelAnimationGroup *animGroup = new QParallelAnimationGroup();
    QPropertyAnimation *trumpRot
        = new QPropertyAnimation(trumpItem, "rotation");
    trumpRot->setDuration(100);
    trumpRot->setKeyValueAt(0, 0);
    trumpRot->setKeyValueAt(1, 90);
    animGroup->addAnimation(trumpRot);
    QPropertyAnimation *trumpPos = new QPropertyAnimation(trumpItem, "pos");
    trumpPos->setDuration(100);
    trumpPos->setKeyValueAt(0, trumpItem->pos());
    trumpPos->setKeyValueAt(1, trumpItem->pos()
                                   + QPointF(CARD_HEIGHT, CARD_WIDTH / 4));
    animGroup->addAnimation(trumpPos);
    animGroup->start(QAbstractAnimation::DeleteWhenStopped);

    // Deal cards to players
    for (int i = 0; i < PLAYERS_COUNT; i++)
        drawCardsFromDeck(i, DEAL_LIMIT);

    // Determine first attacker
    Rank lowestTrump = RANK_ACE;
    int firstAttacker = 0;
    for (Player *p : _players) {
        for (Card c : p->hand()) {
            if (c.suit() == _trumpSuit
                && ((c.rank() != RANK_ACE && c.rank() < lowestTrump)
                    || lowestTrump == RANK_ACE)) {
                firstAttacker = p->index();
                lowestTrump = c.rank();
            }
        }
    }
    _currentAttacker = firstAttacker;
    _currentThrower = firstAttacker;

    // Set game status
    emit setGameStatusText(
        tr("%1's turn").arg(_players[firstAttacker]->name()));

    // If it's an AI, show the trump to the public
    if (firstAttacker) {
        Card cd(_trumpSuit, lowestTrump);
        qDebug() << QString("Lowest trump is %1, which has %2")
                        .arg(cd.fileName())
                        .arg(firstAttacker);
        CardItem *lowestTrumpItem = _cardItems[cd];
        QPropertyAnimation *trumpShow
            = new QPropertyAnimation(lowestTrumpItem, "faceUp");
        trumpShow->setDuration(2000);
        trumpShow->setKeyValueAt(0, 1);
        trumpShow->setKeyValueAt(0.9, 1);
        trumpShow->setKeyValueAt(1, 0);
        trumpShow->start(QAbstractAnimation::DeleteWhenStopped);
    }

    // Temp: show players' hand values
    for (int j = 0; j < PLAYERS_COUNT; j++) {
        Player *p = _players[j];
        QString handDesc = "";
        for (int i = 0; i < p->hand().length(); i++) {
            handDesc += p->hand()[i].fileName();
            if (i < p->hand().length() - 1)
                handDesc += ", ";
        }
        qDebug() << QString("Player %1 has cards: %2. Value is %3")
                        .arg(p->index())
                        .arg(handDesc)
                        .arg(p->currentHandValue());
    }

    _isPlayerTaking = false;
    _playersSaidDone = 0;

    // Adjust buttons for first attack
    if (!currentDefender()->index()) {
        _actionButton->setText(tr("Take"));
        _actionButton->setEnabled(true);
        disconnect(_actionButton, 0, 0, 0);
        connect(_actionButton, &QPushButton::pressed, _players[0],
                &Player::take);
    } else {
        _actionButton->setText(tr("Done"));
        _actionButton->setEnabled(currentDefender()->index() != 2);
        disconnect(_actionButton, 0, 0, 0);
        connect(_actionButton, &QPushButton::pressed, _players[0],
                &Player::done);
    }

    // Actual game
    QEventLoop playerWaitingLoop;
    while (!isGameOver()) {
        int opponents = !_outOfPlay[_currentAttacker]
                        + !_outOfPlay[(_currentAttacker + 2) % PLAYERS_COUNT];
        if (currentAttacker()->index()) {
            currentAttacker()->startTurn();
        } else {
            for (Player *p : _players) {
                disconnect(p, SIGNAL(cardThrown(Card)), &playerWaitingLoop,
                           SLOT(quit()));
            }
            connect(currentAttacker(), SIGNAL(cardThrown(Card)),
                    &playerWaitingLoop, SLOT(quit()));
            playerWaitingLoop.exec();
        }
        int throwLimit = qMin(DEAL_LIMIT, currentDefender()->hand().length());
        while (_playersSaidDone < opponents) {
            int currentPlayersDone = _playersSaidDone;
            if (!_isPlayerTaking
                && _attackCards.length() > _defenseCards.length()) {
                if (currentDefender()->index()) {
                    currentDefender()->tryBeat();
                } else {
                    connect(currentDefender(), SIGNAL(cardBeaten(Card)),
                            &playerWaitingLoop, SLOT(quit()));
                    connect(currentDefender(), SIGNAL(take()),
                            &playerWaitingLoop, SLOT(quit()));
                    playerWaitingLoop.exec();
                }
            }
            if (currentDefender()->hand().length() == 0
                || _attackCards.length() == throwLimit)
                break;
            if (currentThrower()->index()) {
                currentThrower()->throwOrDone();
            } else {
                connect(currentThrower(), SIGNAL(cardThrown(Card)),
                        &playerWaitingLoop, SLOT(quit()));
                connect(currentThrower(), SIGNAL(done()), &playerWaitingLoop,
                        SLOT(quit()));
                playerWaitingLoop.exec();
            }
            if (_playersSaidDone > currentPlayersDone) {
                _currentThrower += 2;
                _currentThrower %= PLAYERS_COUNT;
            }
        }
        QThread::msleep(400);
        bool playerTook = _isPlayerTaking;
        endTurn(_isPlayerTaking ? currentDefender()->index() : -1);
        _currentAttacker += (1 + playerTook);
        _currentAttacker %= PLAYERS_COUNT;
        _currentThrower = _currentAttacker;
        emit setGameStatusText(tr("%1's turn").arg(currentAttacker()->name()));
        if (!currentDefender()->index()) {
            _actionButton->setText(tr("Take"));
            _actionButton->setEnabled(true);
            disconnect(_actionButton, 0, 0, 0);
            connect(_actionButton, &QPushButton::pressed, _players[0],
                    &Player::take);
        } else {
            _actionButton->setText(tr("Done"));
            _actionButton->setEnabled(currentDefender()->index() != 2);
            disconnect(_actionButton, 0, 0, 0);
            connect(_actionButton, &QPushButton::pressed, _players[0],
                    &Player::done);
        }
    }
    // TODO: Generalise
    bool youWon = _outOfPlay[0] && _outOfPlay[2];
    bool opponentsWon = _outOfPlay[1] && _outOfPlay[3];
    QString messages[]
        = {tr("Game is not over"), tr("You win"), tr("You lose"), tr("Draw")};
    QMessageBox::information(nullptr, "Game Over",
                             messages[youWon + 2 * opponentsWon]);
    // Disable the action button after finishing the game
    _actionButton->setEnabled(false);
}

void Table::endTurn(int playerIdx)
{
    QEventLoop loop;
    QList<Card> allCards = _attackCards + _defenseCards;
    if (playerIdx < 0) {
        // Move all cards to discard pile
        QParallelAnimationGroup *animGroup = new QParallelAnimationGroup();
        CardItem *ci;
        for (Card c : allCards) {
            ci = _cardItems[c];
            ci->setFaceUp(false);
            _discardPile.append(c);
            ci->setZValue(_discardPile.length() - 1);
            QPropertyAnimation *cardPos = new QPropertyAnimation(ci, "pos");
            cardPos->setDuration(600);
            cardPos->setKeyValueAt(0, ci->pos());
            cardPos->setKeyValueAt(1, DISCARD_PILE_LOCATION);
            animGroup->addAnimation(cardPos);
        }
        connect(animGroup, SIGNAL(finished()), &loop, SLOT(quit()));
        animGroup->start();

    } else {
        // Give all the cards to the taking player
        QParallelAnimationGroup *animGroup = new QParallelAnimationGroup();
        CardItem *ci;
        for (Card c : allCards) {
            ci = _cardItems[c];
            ci->setFaceUp(playerIdx == 0);
            _players[playerIdx]->addCard(c);
            int ind = _players[playerIdx]->hand().length() - 1;
            ci->setZValue(ind);
            QPropertyAnimation *cardPos = new QPropertyAnimation(ci, "pos");
            QPointF delta = playerIdx ? DELTA_AI : DELTA_HUMAN;
            cardPos->setDuration(600);
            cardPos->setKeyValueAt(0, ci->pos());
            cardPos->setKeyValueAt(1,
                                   PLAYER_LOCATIONS[playerIdx] + ind * delta);
            animGroup->addAnimation(cardPos);
        }
        connect(animGroup, SIGNAL(finished()), &loop, SLOT(quit()));
        animGroup->start();
    }
    loop.exec();
    _attackCards.clear();
    _defenseCards.clear();
    // Draw cards to players
    if (!_deck->empty()) {
        for (int i = 0; i < PLAYERS_COUNT; i++) {
            int cardsToDraw = DEAL_LIMIT - _players[i]->hand().length();
            if (cardsToDraw > 0) {
                drawCardsFromDeck(i, cardsToDraw);
            }
            if (_deck->empty())
                break;
        }
    }
    // Check if someone is out of play
    if (_deck->empty()) {
        for (int i = 0; i < PLAYERS_COUNT; i++) {
            _outOfPlay[i] = _players[i]->hand().length() == 0;
        }
    }
    _isPlayerTaking = false;
    updateNameLabels();
    for (PlayerBubbleItem *bubble : _playerBubbles)
        bubble->hide();
}

QList<Player *> Table::players() const { return _players; }

Suit Table::trumpSuit() const { return _trumpSuit; }

Player *Table::currentAttacker()
{
    // if target player is out of play, then his/her partner attacks
    if (_outOfPlay[_currentAttacker]) {
        return _players[(_currentAttacker + 2) % PLAYERS_COUNT];
    }
    return _players[_currentAttacker];
}

Player *Table::currentDefender()
{
    int currentDefender = (_currentAttacker + 1) % PLAYERS_COUNT;
    // if target player is out of play, then his/her partner defends
    if (_outOfPlay[currentDefender])
        currentDefender = (currentDefender + 2) % PLAYERS_COUNT;
    return _players[currentDefender];
}

bool Table::beats(Card playerCard, Card tableCard)
{
    return (((tableCard.rank() != RANK_ACE && playerCard > tableCard)
             || (playerCard.rank() == RANK_ACE))
            && (playerCard.suit() == tableCard.suit()))
           || (playerCard.suit() == _trumpSuit
               && tableCard.suit() != _trumpSuit);
}

void Table::onCardClick(Card c)
{
    // qDebug() << QString("Card %1 clicked").arg(c.fileName());
    // If it's not human player's card
    if (!_players[0]->hand().contains(c))
        return;
    if (!currentThrower()->index()) {
        currentThrower()->throwCard(c);
    }
    if (!currentDefender()->index()) {
        currentDefender()->beatWithCard(c);
    }
}

void Table::onPlayerDone(int playerIdx)
{
    if (_attackCards.length() == 0)
        return;
    qDebug() << QString("Player %1 says done").arg(playerIdx);
    _playersSaidDone++;
    _playerBubbles[playerIdx]->setText(tr("Done"));
    _playerBubbles[playerIdx]->show();
}

void Table::onPlayerThrows(int playerIdx, Card c)
{
    qDebug() << QString("Player %1 throws %2").arg(playerIdx).arg(c.fileName());
    _playersSaidDone = 0;
    for (PlayerBubbleItem *bubble : _playerBubbles)
        bubble->hide();
    _attackCards.append(c);
    // Reposition throwed card
    CardItem *ci = _cardItems[c];
    ci->setFaceUp(true);
    // ci->setPos(CARD_FIELD_LOCATIONS[_attackCards.length() - 1]);
    ci->setZValue(0);
    QEventLoop loop;
    QPropertyAnimation *cardPos = new QPropertyAnimation(ci, "pos");
    cardPos->setDuration(400);
    cardPos->setKeyValueAt(0, ci->pos());
    cardPos->setKeyValueAt(1, CARD_FIELD_LOCATIONS[_attackCards.length() - 1]);
    connect(cardPos, SIGNAL(finished()), &loop, SLOT(quit()));
    cardPos->start();
    loop.exec();
    // Reposition all cards of a player
    QPointF delta = playerIdx ? DELTA_AI : DELTA_HUMAN;
    for (int i = 0; i < _players[playerIdx]->hand().length(); i++) {
        CardItem *cia = _cardItems[_players[playerIdx]->hand()[i]];
        cia->setPos(PLAYER_LOCATIONS[playerIdx] + i * delta);
        cia->setZValue(i);
    }
    updateNameLabels();
}

void Table::onPlayerBeats(int playerIdx, Card c)
{
    qDebug() << QString("Player %1 beats with %2")
                    .arg(playerIdx)
                    .arg(c.fileName());
    _playersSaidDone = 0;
    for (PlayerBubbleItem *bubble : _playerBubbles)
        bubble->hide();
    _defenseCards.append(c);
    // Reposition throwed card
    CardItem *ci = _cardItems[c];
    ci->setFaceUp(true);
    ci->setZValue(1);
    // ci->setPos(CARD_FIELD_LOCATIONS[_attackCards.length() - 1] + DELTA_AI
    // *
    // 2);
    QEventLoop loop;
    QPropertyAnimation *cardPos = new QPropertyAnimation(ci, "pos");
    cardPos->setDuration(400);
    cardPos->setKeyValueAt(0, ci->pos());
    cardPos->setKeyValueAt(1, CARD_FIELD_LOCATIONS[_attackCards.length() - 1]
                                  + DELTA_AI * 2);
    cardPos->start();
    connect(cardPos, SIGNAL(finished()), &loop, SLOT(quit()));
    loop.exec();
    // Reposition all cards of a player
    QPointF delta = playerIdx ? DELTA_AI : DELTA_HUMAN;
    for (int i = 0; i < _players[playerIdx]->hand().length(); i++) {
        CardItem *cia = _cardItems[_players[playerIdx]->hand()[i]];
        cia->setPos(PLAYER_LOCATIONS[playerIdx] + i * delta);
        cia->setZValue(i);
    }
    updateNameLabels();
}

void Table::onPlayerTakes(int playerIdx)
{
    qDebug() << QString("Player %1 decides to take").arg(playerIdx);
    _playersSaidDone = 0;
    _isPlayerTaking = true;
    updateNameLabels();
    _playerBubbles[playerIdx]->setText(tr("I take"));
    _playerBubbles[playerIdx]->show();
}

void Table::drawCardsFromDeck(int playerIdx, int cardCount)
{
    QEventLoop loop;
    QPointF delta = playerIdx ? DELTA_AI : DELTA_HUMAN;
    QSequentialAnimationGroup *animGroup = new QSequentialAnimationGroup();
    for (int i = 0; i < cardCount; i++) {
        if (_deck->empty())
            break;
        Card c = _deck->draw();
        // qDebug() << _deck->cards().length();
        _players[playerIdx]->addCard(c);
        CardItem *cardItem = _cardItems[c];
        // qDebug() << c.fileName();
        int ind = _players[playerIdx]->hand().length() - 1;
        cardItem->setZValue(ind);
        cardItem->setRotation(0);
        QPropertyAnimation *cardPos = new QPropertyAnimation(cardItem, "pos");
        cardPos->setDuration(200);
        cardPos->setKeyValueAt(0, cardItem->pos());
        cardPos->setKeyValueAt(1, PLAYER_LOCATIONS[playerIdx] + ind * delta);
        animGroup->addAnimation(cardPos);
        cardItem->setFaceUp(!playerIdx);
    }
    animGroup->start(QAbstractAnimation::DeleteWhenStopped);
    connect(animGroup, SIGNAL(finished()), &loop, SLOT(quit()));
    loop.exec();
}

QList<Card> Table::defenseCards() const { return _defenseCards; }

bool Table::isGameOver()
{
    // TODO: Generalise
    return (_outOfPlay[0] && _outOfPlay[2]) || (_outOfPlay[1] && _outOfPlay[3]);
}

void Table::updateNameLabels()
{
    for (int i = 0; i < PLAYERS_COUNT; i++) {
        _nameItems[i]->setText(QString("%1 (%2)%3")
                                   .arg(_players[i]->name())
                                   .arg(_players[i]->hand().length())
                                   .arg(_outOfPlay[i] ? "+" : ""));
    }
}

QList<Card> Table::attackCards() const { return _attackCards; }

Player *Table::currentThrower()
{
    // if target player is out of play, then his/her partner throws
    if (_outOfPlay[_currentThrower]) {
        return _players[(_currentThrower + 2) % PLAYERS_COUNT];
    }
    return _players[_currentThrower];
}

void Table::_updateDeckCardsLabel()
{
    int remaining = _deck->cards().length();
    if (remaining) {
        _deckCardsItem->setText(QString::number(remaining));
    } else {
        QString suitNames[]
            = {tr("Spades"), tr("Clubs"), tr("Diamonds"), tr("Hearts")};
        _deckCardsItem->setText(
            QString(tr("Trump: %1")).arg(suitNames[_trumpSuit]));
    }
}
