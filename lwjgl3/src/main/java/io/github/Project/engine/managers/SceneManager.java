package io.github.Project.engine.managers;

import io.github.Project.engine.core.GameMaster;
import io.github.Project.engine.core.Scene;

// handles scene switching and pause/resume flow
public class SceneManager {

    private final GameMaster gameMaster;

    // stores scene before pause (used for resume)
    private Scene previousScene;

    public SceneManager(GameMaster gameMaster) {
        this.gameMaster = gameMaster;
        this.previousScene = null;
    }

    public GameMaster getGameMaster() {
        return gameMaster;
    }

    // switch to a new scene
    public void setState(Scene scene) {
        gameMaster.setScreen(scene);
    }

    // save current scene and switch (used for pause/menu)
    public void pauseAndSetState(Scene pauseScene) {
        this.previousScene = getCurrentScene();
        gameMaster.setScreen(pauseScene);
    }

    // return to previously paused scene
    public void resumePreviousScene() {
        if (previousScene != null) {
            gameMaster.setScreen(previousScene);
            previousScene = null;
        }
    }

    // get current active scene
    public Scene getCurrentScene() {
        return (Scene) gameMaster.getScreen();
    }

    // get stored previous scene (if any)
    public Scene getPreviousScene() {
        return previousScene;
    }
}