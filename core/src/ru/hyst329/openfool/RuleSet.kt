package ru.hyst329.openfool

/**
 * Created by trolley813 on 20.06.17.
 * Licensed under MIT License.
 */

import com.badlogic.gdx.Preferences

class RuleSet(var deuceBeatsAce: Boolean = false,
              var loweredFirstDiscardLimit: Boolean = false,
              var allowPass: Boolean = false,
              var playerCount: Int = 4,
              var teamPlay: Boolean = true) {
    constructor(preferences: Preferences) : this() {
        deuceBeatsAce = preferences.getBoolean("Rules/DeuceBeatsAce", false)
        loweredFirstDiscardLimit = preferences.getBoolean("Rules/LoweredFirstDiscardLimit", false)
        allowPass = preferences.getBoolean("Rules/AllowPass", false)
        playerCount = preferences.getInteger("Rules/PlayerCount", 4)
        teamPlay = preferences.getBoolean("Rules/TeamPlay", true)
        // Team play only for even number of players (4 or 6 actually)
        teamPlay  = teamPlay && (playerCount > 2 && playerCount % 2 == 0)
    }

    fun save(preferences: Preferences): Unit {
        preferences.putBoolean("Rules/DeuceBeatsAce", deuceBeatsAce)
        preferences.putBoolean("Rules/LoweredFirstDiscardLimit", loweredFirstDiscardLimit)
        preferences.putBoolean("Rules/AllowPass", allowPass)
        preferences.putInteger("Rules/PlayerCount", playerCount)
        preferences.putBoolean("Rules/TeamPlay", teamPlay)
        preferences.flush()
    }
}