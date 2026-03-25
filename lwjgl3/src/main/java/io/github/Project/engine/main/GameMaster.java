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
 *
 * PATTERN: Singleton
 * Only one GameMaster instance should ever exist for the lifetime of the
 * application — it owns the single SpriteBatch, ShapeRenderer, InputMovement,
 * and all managers. The private constructor + getInstance() enforce this,
 * removing the possibility of accidentally constructing a second instance
 * (which would create duplicate GPU resources and managers).
 *
 * Usage:  GameMaster gm = GameMaster.getInstance();
 */
public class GameMaster extends Game {

    // ── Singleton instance ──────────────────────────────────────────────────
    private static GameMaster instance;

    /**
     * Returns the single GameMaster instance.
     * Creates it on the first call; returns the cached reference thereafter.
     */
    public static GameMaster getInstance() {
        if (instance == null) {
            instance = new GameMaster();
        }
        return instance;
    }

    // ── Managers ────────────────────────────────────────────────────────────
    private EntityManager entityManager;
    private IOManager ioManager;
    private MovementManager movementManager;
    private CollisionManager collisionManager;
    private SceneManager sceneManager;
    private AudioManager audioManager;
    private InputMovement inputMovement;

    // Shared renderers — ONE for the whole game (GPU-efficient)
    private SpriteBatch sharedBatch;
    private ShapeRenderer sharedShapeRenderer;

    // ── Private constructor — Singleton pattern ──────────────────────────────
    private GameMaster() {
        // Intentionally empty: LibGDX calls create() for initialisation
    }

    /**
     * Initialises all managers and shared resources.
     * Called once by LibGDX after the OpenGL context is ready.
     */
    @Override
    public void create() {
        this.entityManager    = new EntityManager();
        this.ioManager        = new IOManager(this);
        this.movementManager  = new MovementManager();
        this.audioManager     = new AudioManager();
        this.collisionManager = new CollisionManager(audioManager);
        this.inputMovement    = new InputMovement();
        Gdx.input.setInputProcessor(this.inputMovement);

        // Create ONE shared renderer for all entities
        this.sharedBatch        = new SpriteBatch();
        this.sharedShapeRenderer = new ShapeRenderer();

        this.sceneManager = new SceneManager(this);

        // Start on the main menu
        setScreen(new MainMenuScene(this));
    }

    /**
     * Main game loop — LibGDX automatically delegates to the current
     * Scene's render() method each frame.
     */
    @Override
    public void render() {
        super.render();
    }

    /** Releases all shared GPU resources on shutdown. */
    @Override
    public void dispose() {
        super.dispose();
        if (audioManager != null) audioManager.dispose();
        if (sharedBatch  != null) sharedBatch.dispose();
        if (sharedShapeRenderer != null) sharedShapeRenderer.dispose();
        // Reset singleton so the JVM can GC the instance in tests / hot-reload
        instance = null;
    }

    // ── Getters ─────────────────────────────────────────────────────────────

    public EntityManager    getEntityManager()      { return entityManager; }
    public IOManager        getIoManager()          { return ioManager; }
    public MovementManager  getMovementManager()    { return movementManager; }
    public SceneManager     getSceneManager()       { return sceneManager; }
    public AudioManager     getAudioManager()       { return audioManager; }
    public CollisionManager getCollisionManager()   { return collisionManager; }
    public SpriteBatch      getSharedBatch()        { return sharedBatch; }
    public ShapeRenderer    getSharedShapeRenderer(){ return sharedShapeRenderer; }
    public InputMovement    getInputMovement()      { return inputMovement; }
}