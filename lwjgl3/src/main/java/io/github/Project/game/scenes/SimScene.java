package io.github.Project.game.scenes;

import io.github.Project.engine.scenes.Scene;
import io.github.Project.engine.managers.SceneManager;

/**
 * Main simulation/game scene.
 * This is where the actual gameplay happens.
 */
public class SimScene extends Scene {
    
    /**
     * Creates a new SimScene.
     * @param sceneManager Reference to the scene manager
     */
    public SimScene(SceneManager sceneManager) {
        super(sceneManager);
    }
    
    @Override
    public void create() {
        // Initialize simulation resources
        // TODO: Load textures, create entities, etc.
    }
    
    @Override
    public void show() {
        // Called when scene becomes active
        sceneManager.getGameMaster()
                    .getAudioManager()
                    .startDefaultBackgroundMusic();
    }

    
    @Override
    public void update(float deltaTime) {
        // Update simulation logic
        // TODO: Update game state, physics, AI, etc.
    }
    
    @Override
    public void render() {
        // Render simulation
        // TODO: Draw entities, background, UI, etc.
    }
    
    @Override
    public void pause() {
        // Called when game is paused
        // TODO: Switch to PauseScene
    }
    
    @Override
    public void resume() {
        // Called when game is resumed
    }
    
    @Override
    public void hide() {
        // Called when scene is being hidden
    }
    
    @Override
    public void dispose() {
        // Clean up resources
        // TODO: Dispose textures, sounds, etc.
    }
}
