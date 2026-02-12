package io.github.Project.engine.main;

import io.github.Project.engine.managers.CollisionManager;
import io.github.Project.engine.managers.EntityManager;
import io.github.Project.engine.managers.IOManager;
import io.github.Project.engine.managers.MovementManager;
import io.github.Project.engine.managers.SceneManager;

/**
 * GameMaster - Central coordinator for all game systems.
 * Composition pattern: contains all managers and orchestrates them.
 */
public class GameMaster {
    private EntityManager entityManager;
    private IOManager ioManager;
    private MovementManager movementManager;
    private CollisionManager collisionManager;
    private SceneManager sceneManager;
    private AudioManager audioManager;

    
    /**
     * Creates a new GameMaster instance.
     */
    public GameMaster() {
        this.entityManager = new EntityManager();
        this.ioManager = new IOManager();
        this.movementManager = new MovementManager();
        this.collisionManager = new CollisionManager(entityManager);
        this.sceneManager = new SceneManager();
        this.audioManager = new AudioManager();
    }
    
    /**
     * Updates all game systems.
     * @param deltaTime Time elapsed since last update
     */
    public void update(float deltaTime) {
        sceneManager.update(deltaTime);
        entityManager.update(deltaTime);
        movementManager.updateMovements(deltaTime);
        collisionManager.checkCollisions();
    }
    
    /**
     * Renders all game systems.
     */
    public void render() {
        sceneManager.render();
        entityManager.render();
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
    
    public CollisionManager getCollisionManager() {
        return collisionManager;
    }
    
    public SceneManager getSceneManager() {
        return sceneManager;
    }
    public AudioManager getAudioManager() {
        return audioManager;
    }
}
