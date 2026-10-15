package com.marioexample.desktop;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.marioexample.core.MainGame;

import java.io.File;

public class DesktopLauncher {
    public static void main(String[] arg) {
        // If we can't find assets in the current folder, but we see an 'assets' folder,
        // automatically switch the working directory to 'assets'.
        // This fixes the "File not found" error when running from the project root.
        File assetsFolder = new File("assets");
        if (!new File("background.jpg").exists() && assetsFolder.exists() && assetsFolder.isDirectory()) {
            System.setProperty("user.dir", assetsFolder.getAbsolutePath());
        }

        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Mini Platformer");
        config.setWindowedMode(1280, 720);
        config.useVsync(true);
        config.setForegroundFPS(60);
        new Lwjgl3Application(new MainGame(), config);
    }
}


