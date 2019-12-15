package ru.hyst329.openfool.desktop

import com.badlogic.gdx.Files
import com.badlogic.gdx.backends.lwjgl.LwjglApplication
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration

import ru.hyst329.openfool.OpenFoolGame
import ru.hyst329.openfool.RuleSet
import ru.hyst329.openfool.PlayerTesting
import java.io.PrintWriter

object DesktopLauncher {
    @JvmStatic
    fun main(arg: Array<String>) {
        if (arg.isNotEmpty() && arg[0]=="--test-players") {
            val players = arg[1].toInt()
            val games = arg[2].toInt()
            println("Testing players")
            for (c in listOf(24, 32, 36, 52)) {
                val ruleSet = RuleSet(false, false, false, players, false, c)
                val rank = ruleSet.lowestRank
                val log = PrintWriter("out_${players}_${ruleSet.cardCount}.csv")
                for (i in 1..games) {
                    println("Running game $i")
                    val (initialHands, places) = PlayerTesting(i, ruleSet, rank).runGame()
                    for (p in 0 until players) {
                        log.println("${initialHands[p]},${players + 1 - places[p]}")
                    }
                }
                log.close()
            }
            System.exit(0)
        }
        val config = LwjglApplicationConfiguration()
        config.title = "OpenFool"
        config.width = 800
        config.height = 480
        //config.resizable = false
        config.addIcon("logos/logo_128.png", Files.FileType.Internal)
        config.addIcon("logos/logo_32.png", Files.FileType.Internal)
        config.addIcon("logos/logo_16.png", Files.FileType.Internal)
        LwjglApplication(OpenFoolGame(DesktopOrientationHelper()), config)
    }
}
