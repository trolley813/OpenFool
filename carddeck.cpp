#include "carddeck.h"
#ifdef WIN32
#include <windows.h>
#include <wincrypt.h>
#endif
#include <QDebug>

static std::mt19937 rng;
static std::seed_seq seq;
static std::array<uint8_t, 2496> seedData;

CardDeck::CardDeck(QObject *parent, bool pristine, Rank lowestRank, int jokers)
    : _lowestRank(lowestRank), _jokers(jokers), QObject(parent)
{
    reset();

#ifdef WIN32
    HCRYPTPROV p = 0;
    CryptAcquireContextW(&p, 0, 0, PROV_RSA_FULL,
                         CRYPT_VERIFYCONTEXT | CRYPT_SILENT);
    CryptGenRandom(p, seedData.size(), seedData.data());
    CryptReleaseContext(p, 0);
#else
    std::random_device rd;
    std::generate_n(seedData.data(), seedData.size(), std::ref(rd));
#endif
    seq = std::seed_seq(std::begin(seedData), std::end(seedData));
    rng = std::mt19937(seq);
    if (!pristine) {
        shuffle();
    }
}

void CardDeck::shuffle() { std::shuffle(_cards.begin(), _cards.end(), rng); }

void CardDeck::reset()
{
    _cards.clear();
    for (int s = 0; s < 4; s++) {
        for (int r = _lowestRank; r <= RANK_KING; r++) {
            _cards << Card(Suit(s), Rank(r));
        }
        _cards << Card(Suit(s), RANK_ACE);
    }
    for (int j = 0; j < _jokers; j++) {
        _cards << Card(SUIT_SPADES, RANK_JOKER);
    }
}

Card CardDeck::draw()
{
    Card c = _cards.back();
    _cards.pop_back();
    emit cardDrawn();
    return c;
}

bool CardDeck::empty() { return _cards.isEmpty(); }

QList<Card> CardDeck::cards() const { return _cards; }
