package io.github.Project.game.scenes;

import io.github.Project.engine.scenes.Scene;
import io.github.Project.engine.main.GameMaster;
import com.badlogic.gdx.Input;

/**
 * Options/Settings menu scene.
 * Allows players to configure game settings.
 */
public class OptionsScene extends Scene {
    
    /**
     * Creates a new OptionsScene.
     * @param gameMaster Reference to the game master
     */
    public OptionsScene(GameMaster gameMaster) {
        super(gameMaster);
    }
    
    @Override
    public void show() {
        // Called when options menu is shown
        // TODO: Load settings UI elements
    }
    
    @Override
    public void render(float delta) {
        // Update options menu logic
        // Example: Press ESC to go back
        
        if (gameMaster.getIoManager()
                      .isKeyPressed(Input.Keys.ESCAPE)) {
            // Return to previous scene (could be main menu or pause)
            gameMaster.getSceneManager()
                      .setState(new PauseScene(gameMaster));
        }
        
        // Render options menu
        // TODO: Draw settings controls, sliders, buttons
    }
    
    @Override
    public void dispose() {
        // Clean up options menu resources
    }
}
