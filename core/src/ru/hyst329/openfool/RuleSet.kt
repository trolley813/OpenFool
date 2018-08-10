package ru.hyst329.openfool

/**
 * Created by trolley813 on 20.06.17.
 * Licensed under MIT License.
 */

import com.badlogic.gdx.Preferences
import kotlin.math.min

class RuleSet(var deuceBeatsAce: Boolean = false,
              var loweredFirstDiscardLimit: Boolean = false,
              var allowPass: Boolean = false,
              playerCount: Int = 4,
              var teamPlay: Boolean = true,
              var cardCount: Int = 52) {
    var playerCount: Int = playerCount
    set(value) {
        field = value
        teamPlay = teamPlay && (playerCount > 2 && playerCount % 2 == 0)
    }
    constructor(preferences: Preferences) : this() {
        deuceBeatsAce = preferences.getBoolean("Rules/DeuceBeatsAce", false)
        loweredFirstDiscardLimit = preferences.getBoolean("Rules/LoweredFirstDiscardLimit", false)
        allowPass = preferences.getBoolean("Rules/AllowPass", false)
        playerCount = preferences.getInteger("Rules/PlayerCount", 4)
        cardCount = preferences.getInteger("Rules/CardCount", 52)
        teamPlay = preferences.getBoolean("Rules/TeamPlay", true)
        // Team play only for even number of players (4 or 6 actually)
        teamPlay = teamPlay && (playerCount > 2 && playerCount % 2 == 0)
        // Card count must be a multiple of 4 and not less than 6 * cardCount
        cardCount = min(6 * playerCount, (cardCount + 3) / 4 * 4)
    }

    fun save(preferences: Preferences) {
        preferences.putBoolean("Rules/DeuceBeatsAce", deuceBeatsAce)
        preferences.putBoolean("Rules/LoweredFirstDiscardLimit", loweredFirstDiscardLimit)
        preferences.putBoolean("Rules/AllowPass", allowPass)
        preferences.putInteger("Rules/PlayerCount", playerCount)
        preferences.putInteger("Rules/CardCount", cardCount)
        preferences.putBoolean("Rules/TeamPlay", teamPlay)
        preferences.flush()
    }
}