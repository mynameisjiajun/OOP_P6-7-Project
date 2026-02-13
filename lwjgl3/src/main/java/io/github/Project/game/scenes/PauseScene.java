package io.github.Project.game.scenes;

import io.github.Project.engine.scenes.Scene;
import io.github.Project.engine.managers.SceneManager;
import com.badlogic.gdx.Input;

/**
 * Pause menu scene.
 * Displayed when the game is paused.
 */
public class PauseScene extends Scene {
    
    /**
     * Creates a new PauseScene.
     * @param sceneManager Reference to the scene manager
     */
    public PauseScene(SceneManager sceneManager) {
        super(sceneManager);
    }
    
    @Override
    public void create() {
        // Initialize pause menu resources
        // TODO: Load menu UI elements
    }
    
    @Override
    public void show() {
        // Called when pause menu is shown
    			sceneManager.getGameMaster()
					.getAudioManager()
					.playUIClick();
    }
    
    @Override
    public void update(float deltaTime) {
        // Update pause menu logic
        // TODO: Handle resume, quit, settings buttons
    	@Override
    	public void update(float deltaTime) {
    	    // Example: Press R to resume
    		
    	    if (sceneManager.getGameMaster()
    	                    .getIOManager()
    	                    .isKeyPressed(Input.Keys.R)) {

    	        sceneManager.getGameMaster()
    	                    .getAudioManager()
    	                    .playUIClick();

    	        sceneManager.setState(new SimScene(sceneManager));
    	    }
    	}

    
    @Override
    public void render() {
        // Render pause menu
        // TODO: Draw menu background, buttons, text
    }
    
    @Override
    public void pause() {
        // Already paused
    }
    
    @Override
    public void resume() {
        // Return to previous scene
        // TODO: Switch back to SimScene
    }
    
    @Override
    public void hide() {
        // Called when pause menu is hidden
    }
    
    @Override
    public void dispose() {
        // Clean up pause menu resources
    }
}
