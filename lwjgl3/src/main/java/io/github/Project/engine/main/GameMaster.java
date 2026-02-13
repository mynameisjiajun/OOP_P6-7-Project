package io.github.Project.engine.main;

import com.badlogic.gdx.Game;
import io.github.Project.engine.managers.EntityManager;
import io.github.Project.engine.managers.IOManager;
import io.github.Project.engine.managers.MovementManager;
import io.github.Project.engine.managers.SceneManager;
import io.github.Project.engine.managers.AudioManager;
import io.github.Project.game.scenes.MainMenuScene;

/**
 * GameMaster - Central coordinator for all game systems.
 * Main entry point that extends LibGDX Game class.
 * Composition pattern: contains all managers and orchestrates them.
 */
public class GameMaster extends Game {
    private EntityManager entityManager;
    private IOManager ioManager;
    private MovementManager movementManager;
    private SceneManager sceneManager;
    private AudioManager audioManager;

    public GameMaster() {
        // DO NOT load managers here!
    }
    
    /**
     * Called when the game is created.
     * Initializes all managers.
     */
    @Override
    public void create() {
        this.entityManager = new EntityManager();
        this.ioManager = new IOManager();
        this.movementManager = new MovementManager();
        this.audioManager = new AudioManager();
        
        this.sceneManager = new SceneManager(this);
        
        // Initialize with main menu
        setScreen(new MainMenuScene(this));
    }
    
    /**
     * Main game loop - called every frame.
     * LibGDX automatically delegates to the current Screen's render method.
     * We also update other managers here.
     */
    @Override
    public void render() {
        float deltaTime = com.badlogic.gdx.Gdx.graphics.getDeltaTime();
        
        // Update game systems
        entityManager.update(deltaTime);
        movementManager.updateMovements(deltaTime);
        
        // LibGDX's Game class automatically calls getScreen().render(delta)
        super.render();
        
        // Render entities after screen
        entityManager.render();
    }
    
    /**
     * Called when the game is disposed.
     * Clean up all resources.
     */
    @Override
    public void dispose() {
        super.dispose();
        audioManager.dispose();
    }
    
    // Getters for all managers
    public EntityManager getEntityManager() {
        return entityManager;
    }
    
    public IOManager getIoManager() {
        return ioManager;
    }
    
    public MovementManager getMovementManager() {
        return movementManager;
    }
    
    public SceneManager getSceneManager() {
        return sceneManager;
    }
    public AudioManager getAudioManager() {
        return audioManager;
    }
}
