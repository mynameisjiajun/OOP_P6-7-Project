package io.github.Project.engine.core;

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
 * Central coordinator for all game systems. Extends LibGDX Game.
 *
 * PATTERN: Singleton — one instance owns all managers and shared GPU resources.
 * The private constructor prevents accidental second instances.
 */
public class GameMaster extends Game {

    private static GameMaster instance;

    public static GameMaster getInstance() {
        if (instance == null) {
            instance = new GameMaster();
        }
        return instance;
    }

    private EntityManager entityManager;
    private IOManager ioManager;
    private MovementManager movementManager;
    private CollisionManager collisionManager;
    private SceneManager sceneManager;
    private AudioManager audioManager;
    private InputMovement inputMovement;

    // shared renderers — one instance for the whole game
    private SpriteBatch sharedBatch;
    private ShapeRenderer sharedShapeRenderer;

    private GameMaster() {}

    @Override
    public void create() {
        this.entityManager    = new EntityManager();
        this.ioManager        = new IOManager(this);
        this.movementManager  = new MovementManager();
        this.audioManager     = new AudioManager();
        this.collisionManager = new CollisionManager(audioManager);
        this.inputMovement    = new InputMovement();
        Gdx.input.setInputProcessor(this.inputMovement);

        this.sharedBatch         = new SpriteBatch();
        this.sharedShapeRenderer = new ShapeRenderer();

        this.sceneManager = new SceneManager(this);
        setScreen(new MainMenuScene(this));
    }

    @Override
    public void render() {
        super.render();
    }

    @Override
    public void dispose() {
        super.dispose();
        if (audioManager != null) audioManager.dispose();
        if (sharedBatch  != null) sharedBatch.dispose();
        if (sharedShapeRenderer != null) sharedShapeRenderer.dispose();
        instance = null;
    }

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