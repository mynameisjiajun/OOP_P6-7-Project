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
    private MovementManager movementManager;
    private CollisionManager collisionManager;
    private SceneManager sceneManager;
    private AudioManager audioManager;
    private InputMovement inputMovement;
    private IOManager ioManager;
    
    // Persistent settings file
    private static final String SETTINGS_FILE = "settings.txt";


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
        this.movementManager = new MovementManager();
        this.audioManager = new AudioManager();
        this.collisionManager = new CollisionManager(entityManager, audioManager);
        this.inputMovement = new InputMovement();
        Gdx.input.setInputProcessor(this.inputMovement);
        this.ioManager = new IOManager();
        
        // IOManager integration: load persistent data
        if (ioManager.localFileExists("settings.txt")) {
            String settings = ioManager.readLocalFile("settings.txt");
            System.out.println("Loaded settings:\n" + settings);
        } else {
            System.out.println("No settings found (first run).");
            ioManager.writeLocalFile("settings.txt", "volume=1.0\nmuted=false");
        }

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
     * Each scene is responsible for updating its own game logic.
     */
    @Override
    public void render() {
        // LibGDX's Game class automatically calls getScreen().render(delta)
        // The active scene controls what systems update and render
        super.render();
    }
    
    /**
     * Called when the game is disposed.
     * Clean up all resources.
     */
    @Override
    public void dispose() {

        // IOManager integration: save persistent data
        if (ioManager != null) {
            ioManager.writeLocalFile(SETTINGS_FILE, "volume=1.0\nmuted=false");
        }

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

