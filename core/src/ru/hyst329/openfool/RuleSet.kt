package ru.hyst329.openfool

/**
 * Created by trolley813 on 20.06.17.
 * Licensed under MIT License.
 */

import com.badlogic.gdx.Preferences

class RuleSet(var deuceBeatsAce: Boolean = false,
              var loweredFirstDiscardLimit: Boolean = false,
              var allowPass: Boolean = false,
              var playerCount: Int = 4) {
    constructor(preferences: Preferences) : this() {
        deuceBeatsAce = preferences.getBoolean("Rules/DeuceBeatsAce", false)
        loweredFirstDiscardLimit = preferences.getBoolean("Rules/LoweredFirstDiscardLimit", false)
        allowPass = preferences.getBoolean("Rules/AllowPass", false)
        playerCount = preferences.getInteger("Rules/PlayerCount", 4)
    }
}