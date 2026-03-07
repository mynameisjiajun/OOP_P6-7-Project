package io.github.Project.engine.managers;

import io.github.Project.engine.main.GameMaster;
import io.github.Project.engine.scenes.Scene;

/**
 * Manages game scenes/screens using LibGDX's built-in screen system.
 * Provides convenient helper methods for scene transitions.
 * Tracks previous scene to allow resuming without losing state.
 */
public class SceneManager {
    private GameMaster gameMaster;
    private Scene previousScene;  // Track scene before pause/menu
    
    /**
     * Creates a new SceneManager.
     */
    public SceneManager(GameMaster gameMaster) {
        this.gameMaster = gameMaster;
        this.previousScene = null;
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
     * Pauses the current scene and switches to a new scene (like pause menu).
     * Saves the current scene so it can be resumed later without losing state.
     * @param pauseScene The pause/overlay scene to show
     */
    public void pauseAndSetState(Scene pauseScene) {
        this.previousScene = getCurrentScene();
        gameMaster.setScreen(pauseScene);
    }
    
    /**
     * Resumes the previously paused scene.
     * Returns to the scene that was active before pause was called.
     * If no previous scene exists, does nothing.
     */
    public void resumePreviousScene() {
        if (previousScene != null) {
            gameMaster.setScreen(previousScene);
            previousScene = null;  // Clear after resuming
        }
    }
    
    /**
     * Gets the current active scene.
     * @return The current scene
     */
    public Scene getCurrentScene() {
        return (Scene) gameMaster.getScreen();
    }
    
    /**
     * Gets the previously paused scene (if any).
     * @return The previous scene or null
     */
    public Scene getPreviousScene() {
        return previousScene;
    }
}
