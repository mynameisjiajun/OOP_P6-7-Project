package io.github.Project.game.scenes;

import io.github.Project.engine.scenes.Scene;
import io.github.Project.engine.main.GameMaster;
import com.badlogic.gdx.Input;

/**
 * Pause menu scene.
 * Displayed when the game is paused.
 */
public class PauseScene extends Scene {
    
    /**
     * Creates a new PauseScene.
     * @param gameMaster Reference to the game master
     */
    public PauseScene(GameMaster gameMaster) {
        super(gameMaster);
    }
    
    @Override
    public void show() {
        // Called when pause menu is shown
        // TODO: Load menu UI elements
    }
    
    @Override
    public void render(float delta) {
        // Update pause menu logic
        // Example: Press R to resume
        
        if (gameMaster.getIoManager()
                      .isKeyPressed(Input.Keys.R)) {

            gameMaster.getAudioManager()
                      .playUIClick();

            gameMaster.getSceneManager()
                      .setState(new PlayScene(gameMaster));
        }
        
        // Render pause menu
        // TODO: Draw menu background, buttons, text
    }
    
    @Override
    public void resume() {
        // Return to previous scene
        gameMaster.getSceneManager().setState(new PlayScene(gameMaster));
    }
    
    @Override
    public void dispose() {
        // Clean up pause menu resources
    }
}
