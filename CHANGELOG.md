# Changelog
## 0.2.7 (2018-09-22)
- Cards which cannot be used now are greyed out (highly experimental)
- Stripped decks (24, 32 or 36 cards) cas be used along with 52-card deck
- Fixed issue #34 (team play checkbox was inaccessible when using Russian)
## 0.2.6 (2018-08-11)
- Fixed issue #32 when individual play could not finish sometimes
- Added information about places taken by the players onto the result screen.
## 0.2.5 (2017-12-31)
- Fixed issue #31 with improper UI elements positioning on Android devices (on desktop, there's now a fixed-size window).
## 0.2.4 (2017-12-24)
- Christmas Edition: Santa Hat and blue background during Christmas and New Year holidays (24/12 to 9/1).
## 0.2.3 (2017-11-07)
- Revolution Edition: Hammer and Sickle logo and red background during October Revolution holidays (5/11 to 10/11).
## 0.2.2 (2017-08-30/2017-09-09)
- Some minor bug fixes.
- Added iOS build (for jailbroken devices only).
## 0.2.1 (2017-07-27)
- Fixed issue #10, throwing rules up to 5 players are properly implemented.
## 0.2.0 (2017-07-19)
- Fixed issue #28, 4-player individual mode is now possible. 
- Maximum player count increased to 5.
- Result screen text are different for individual and team modes.
## 0.1.9 (2017-07-13)
- Added individual mode for 2 and 3 players (experimental).
- Trump suit name on the game screen replaced with symbol.
- Application icon added.
- The rules are customisable in some extent.
## 0.1.8 (2017-06-12)
- Added Underdog font as main UI font (until new skin).
- Fixed Czech localisation display.
## 0.1.7 (2017-05-25)
- Added Czech localisation.
- Code cleanup.
## 0.1.6 (2017-04-09)
- Fixed issue #23, when throw limit checking (and possibly other things) was broken.
- Added turn information display (who attacks/defends)
- Added player information (when someone takes or says _Done_).
- Fixed issue #25, when the game crashed if the background was not set.
## 0.1.5 (2017-04-06)
- The game is rewritten to Kotlin.
- Added background selection (2 backgrounds now available).
- VisUI updated to 1.3.0.
## 0.1.4 (2017-03-30)
- Fixed issue with card throwing (#23).
- Tweaked discard pile cards placement.
- Back button is now functional.
## 0.1.3 (2017-03-26)
- Added background (currently checkerboard).
- Added card sorting (by suit or rank, ascending or descending).
- Cards in the discard pile are now placed unevenly.
## 0.1.2 (2017-03-19)
- Fixed issue #17 (throwing more cards then allowed).
- Added localisation.
- Fixed issue #22 (a problem running on devices with aspect ratio other than 5:3).

## 0.1.1 (2017-03-18)
- Re-added settings (background color and deck selection)
- Added result screen (won/lost/drawn).

## 0.1.0 (2017-03-16)
- Full rewrite to Java and libGDX. Some features still missing.

## 0.0.9 (2017-03-01)
- Added French card deck (by David Bellot)
- Added card sorting

## 0.0.8 (2017-02-23)
- Fixed bug: app now properly exits if quit during the game
- Fixed bug when cards went to the same place when playing too fast
- Added background color selection

## 0.0.7 (2017-02-19)
- application icon
- language can be selected from settings
- fixed bug with card throw limit

## 0.0.6 (2017-02-17)
- Android version (VERY EXPERIMENTAL)
- Now binaries ship with Qt 5.8

## 0.0.5 (2017-02-16)
- Improved AI
- (Probably) fixed bug, when the game could not finish

## 0.0.4 (2017-02-08)
- Added player bubbles with info text such as "I take", "Done"

## 0.0.3 (2017-02-05)
- Fixed bug with rendering on Windows
- Added settings dialog (deck, rendering settings and player names)
- Added international deck

## 0.0.2 (2017-02-01)
- Fixed bug with AI (mainly higher cards were thrown, tending to beat with higher cards).
- Added locale support, translated to Russian

## 0.0.1 (2017-01-31)
- First public release
