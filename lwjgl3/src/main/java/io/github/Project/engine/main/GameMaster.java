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
 * GameMaster - Central coordinator for all game systems.
 * Main entry point that extends LibGDX Game class.
 * Composition pattern: contains all managers and orchestrates them.
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
        this.collisionManager = new CollisionManager(entityManager, audioManager);
        this.inputMovement = new InputMovement();
        Gdx.input.setInputProcessor(this.inputMovement);

        // Create ONE shared renderer for all entities
        this.sharedBatch = new SpriteBatch();
        this.sharedShapeRenderer = new ShapeRenderer();
        
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
        float deltaTime = Gdx.graphics.getDeltaTime();
        
        // Update game systems (MovementManager already calls entity.update())
        // DO NOT call entityManager.update() here - that would double-update!
        movementManager.updateMovements(deltaTime);
        collisionManager.checkCollisions();
        
        // LibGDX's Game class automatically calls getScreen().render(delta)
        super.render();
        
        // Render all entities using shared renderers
        entityManager.render(sharedBatch, sharedShapeRenderer);
    }
    
    /**
     * Called when the game is disposed.
     * Clean up all resources.
     */
    @Override
    public void dispose() {
        super.dispose();
        audioManager.dispose();
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

