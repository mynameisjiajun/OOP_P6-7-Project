package io.github.Project.engine.main;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import io.github.Project.engine.managers.EntityManager;
import io.github.Project.engine.managers.IOManager;
import io.github.Project.engine.input.InputMovement;
import io.github.Project.engine.managers.MovementManager;
import io.github.Project.engine.managers.CollisionManager;
import io.github.Project.engine.managers.SceneManager;
import io.github.Project.engine.managers.AudioManager;
import io.github.Project.game.scenes.MainMenuScene;

/**
 * Central coordinator for all game systems.
 * Main entry point that extends LibGDX Game class.
 * Composition pattern: contains all managers and orchestrates them.
 * 
 * FIXED: 
 * - Removed rendering from GameMaster (now in scenes)
 * - Removed update logic from GameMaster (now in scenes)
 * - GameMaster only provides shared resources
 */
public class GameMaster extends Game {
    private EntityManager entityManager;
    private IOManager ioManager;
    private MovementManager movementManager;
    private CollisionManager collisionManager;
    private SceneManager sceneManager;
    private AudioManager audioManager;
    private InputMovement inputMovement;

    // Shared renderers - ONE for the whole game (GPU-efficient)
    private SpriteBatch sharedBatch;
    private ShapeRenderer sharedShapeRenderer;

    public GameMaster() {
    }
    
    /**
     * Initializes all managers.
     * FIXED: CollisionManager no longer receives EntityManager
     */
    @Override
    public void create() {
        this.entityManager = new EntityManager();
        this.ioManager = new IOManager(this);
        this.movementManager = new MovementManager();
        this.audioManager = new AudioManager();
        this.collisionManager = new CollisionManager(audioManager); // FIXED: No EntityManager
        this.inputMovement = new InputMovement();
        Gdx.input.setInputProcessor(this.inputMovement);

        // Create ONE shared renderer for all entities
        this.sharedBatch = new SpriteBatch();
        this.sharedShapeRenderer = new ShapeRenderer();
        
        this.sceneManager = new SceneManager(this);
        
        // Initialize with main menu
        setScreen(new MainMenuScene(this));
    }
    
    // Main game loop - called every frame. LibGDX automatically delegates to current Scene's render method.
// scene updates/renders entities 
  
    @Override
    public void render() {
        // LibGDX's Game class automatically calls getScreen().render(delta)
        super.render();
    }
    
    // Called when the game is disposed.
    // Clean up all resources.
    
    @Override
    public void dispose() {
        super.dispose();
        if (audioManager != null) audioManager.dispose();
        if (sharedBatch != null) sharedBatch.dispose();
        if (sharedShapeRenderer != null) sharedShapeRenderer.dispose();
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
    
    public CollisionManager getCollisionManager() {
        return collisionManager;
    }

    public SpriteBatch getSharedBatch() {
        return sharedBatch;
    }

    public ShapeRenderer getSharedShapeRenderer() {
        return sharedShapeRenderer;
    }

    public InputMovement getInputMovement() {
        return inputMovement;
    }
}