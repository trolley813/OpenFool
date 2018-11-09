# OpenFool
![Travis](https://img.shields.io/travis/hyst329/OpenFool.svg)
![Github All Releases](https://img.shields.io/github/downloads/hyst329/OpenFool/total.svg)
![Github Releases](https://img.shields.io/github/downloads/hyst329/OpenFool/latest/total.svg)
![GitHub release](https://img.shields.io/github/release/hyst329/OpenFool.svg)
[![Gitter](https://img.shields.io/gitter/room/OpenFoolCommunity/Lobby.svg)](https://gitter.im/OpenFoolCommunity/Lobby)

OpenFool - free and open source (MIT licensed) Fool (Durak) card game implementation for desktop and Android.

[<img src="https://gitlab.com/fdroid/artwork/raw/master/badge/get-it-on.png"
     alt="Get it on F-Droid"
     height="80">](https://f-droid.org/packages/ru.hyst329.openfool/)
     
## Features
### Implemented
- 4-players partnership game (2 vs 2), individual game for 2-5 players 
- 52-card decks: Standard Russian (designed by A. Charlemagne in 19th century, public domain - from Wikimedia),
    international (by Chris Aguilar - LGPL v3), French deck (by David Bellot - LGPL v3), stripped deck variants (24, 32 and 36 cards)
- Standard rules for throwing in and passing (the latter is optional)
- Some conventions may be customised

### Planned
- Individual and partnership play for 6 players (3 vs 3)
- Customisable player names (both AI and human)
- Statistics
- Online play (via custom server)
- More deck designs
- More customisable rules (e.g. Japanese fool or spade-on-spade)

## How to build
It's a Gradle project. Run
```bash
./gradlew :desktop:run
```
to run the desktop version
