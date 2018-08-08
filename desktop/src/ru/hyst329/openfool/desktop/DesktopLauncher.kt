package ru.hyst329.openfool.desktop

import com.badlogic.gdx.Files
import com.badlogic.gdx.backends.lwjgl.LwjglApplication
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration

import ru.hyst329.openfool.OpenFoolGame

object DesktopLauncher {
    @JvmStatic
    fun main(arg: Array<String>) {
        if (arg.contains("--test-players")) {
            println("Testing players")
            System.exit(5)
        }
        val config = LwjglApplicationConfiguration()
        config.title = "OpenFool"
        config.width = 800
        config.height = 480
        config.resizable = false
        config.addIcon("logos/logo_128.png", Files.FileType.Internal)
        config.addIcon("logos/logo_32.png", Files.FileType.Internal)
        config.addIcon("logos/logo_16.png", Files.FileType.Internal)
        LwjglApplication(OpenFoolGame(), config)
    }
}
