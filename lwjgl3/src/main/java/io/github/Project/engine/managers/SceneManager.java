package io.github.Project.engine.managers;

import io.github.Project.engine.main.GameMaster;
import io.github.Project.engine.scenes.Scene;

/**
 * Manages game scenes/screens using LibGDX's built-in screen system.
 * Provides convenient helper methods for scene transitions.
 */
public class SceneManager {
    private GameMaster gameMaster;
    
    /**
     * Creates a new SceneManager.
     */
    public SceneManager(GameMaster gameMaster) {
        this.gameMaster = gameMaster;
    }
    
    public GameMaster getGameMaster() {   
        return gameMaster;
    }
    
    /**
     * Sets the current active scene using LibGDX's screen system.
     * @param scene The scene to activate
     */
    public void setState(Scene scene) {
        gameMaster.setScreen(scene);
    }
    
    /**
     * Gets the current active scene.
     * @return The current scene
     */
    public Scene getCurrentScene() {
        return (Scene) gameMaster.getScreen();
    }
}
