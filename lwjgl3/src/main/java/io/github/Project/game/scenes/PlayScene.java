package io.github.Project.game.scenes;

import io.github.Project.engine.scenes.Scene;
import io.github.Project.engine.main.GameMaster;

/**
 * Main play/game scene.
 * This is where the actual gameplay happens.
 */
public class PlayScene extends Scene {
    
    /**
     * Creates a new PlayScene.
     * @param gameMaster Reference to the game master
     */
    public PlayScene(GameMaster gameMaster) {
        super(gameMaster);
    }
    
    @Override
    public void show() {
        // Called when scene becomes active
        gameMaster.getAudioManager()
                  .startDefaultBackgroundMusic();
    }

    @Override
    public void render(float delta) {
        // Update simulation logic
        // TODO: Update game state, physics, AI, etc.
        
        // Render simulation
        // TODO: Draw entities, background, UI, etc.
    }
    
    @Override
    public void pause() {
        // Called when game is paused
        // TODO: Switch to PauseScene
        // gameMaster.getSceneManager().setState(new PauseScene(gameMaster));
    }
    
    @Override
    public void dispose() {
        // Clean up resources
        // TODO: Dispose textures, sounds, etc.
    }
}
