# OpenFool

OpenFool - free and open source (GPL v3) Fool (Durak) card game implementation.

## Features
### Implemented
- 4-players partnership game
- Standard Russian 52-card deck
- Standard rules for throwing in

### Planned
- International deck
- Individual game, arbitrary number of players (2-6)
- Player names
- Statistics
- Ability to change conventions

## How to build from sources
### Prerequisites
- Qt 5.7 or later (probably _should_ work with 5.6)
- Qt Creator (optional)
- C++ compiler (GCC/Clang/Visual C++ etc.)

*Note: the easiest way on Windows is to download the Qt Creator bundle.*

### Build process
1. Clone the repository

```bash
git clone https://github.com/hyst329/OpenFool.git
```

2. Either open the project in Qt Creator, or build via the command line:

```bash
mkdir build # Alongside with source dir, not inside it
cd build
qmake ../OpenFool
# On Windows, you will probably use `mingw32-make` instead of `make`
make release # or `make debug`
```
