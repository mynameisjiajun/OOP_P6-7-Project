package io.github.Project.engine.main;

import com.badlogic.gdx.Game;
import io.github.Project.engine.managers.SceneManager;

/**
 * Main entry point for the game engine.
 * Extends LibGDX Game class and manages the game loop.
 */
public class BaseEngine extends Game {
    private GameMaster gameMaster;
    
    @Override
    public void create() {
        gameMaster = new GameMaster();
        // Initialize with first scene
        // gameMaster.getSceneManager().setState(new SimScene(gameMaster.getSceneManager()));
    }
    
    @Override
    public void render() {
        float deltaTime = com.badlogic.gdx.Gdx.graphics.getDeltaTime();
        gameMaster.update(deltaTime);
        gameMaster.render();
    }
    
    @Override
    public void dispose() {
        super.dispose();
    }
    
    public GameMaster getGameMaster() {
        return gameMaster;
    }
}
