package ru.hyst329.openfool.desktop;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

import ru.hyst329.openfool.OpenFoolGame;

public class DesktopLauncher {
    public static void main(String[] arg) {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.title = "OpenFool";
        config.width = 800;
        config.height = 480;
        config.addIcon("logos/logo_128.png", Files.FileType.Internal);
        config.addIcon("logos/logo_32.png", Files.FileType.Internal);
        config.addIcon("logos/logo_16.png", Files.FileType.Internal);
        new LwjglApplication(new OpenFoolGame(), config);
    }
}
