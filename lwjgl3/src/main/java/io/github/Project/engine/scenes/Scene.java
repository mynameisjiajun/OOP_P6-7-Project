package io.github.Project.engine.scenes;

import io.github.Project.engine.managers.SceneManager;

/**
 * Abstract base class for all game scenes/screens.
 * Provides common scene functionality and lifecycle methods.
 */
public abstract class Scene {
    protected SceneManager sceneManager;
    protected int width;
    protected int height;
    
    /**
     * Creates a new scene.
     * @param sceneManager Reference to the scene manager
     */
    public Scene(SceneManager sceneManager) {
        this.sceneManager = sceneManager;
    }
    
    /**
     * Called when the scene is first created.
     */
    public abstract void create();
    
    /**
     * Called when the scene becomes active.
     */
    public abstract void show();
    
    /**
     * Updates the scene.
     * @param deltaTime Time elapsed since last update
     */
    public abstract void update(float deltaTime);
    
    /**
     * Renders the scene.
     */
    public abstract void render();
    
    /**
     * Called when the scene is paused.
     */
    public abstract void pause();
    
    /**
     * Called when the scene is resumed from pause.
     */
    public abstract void resume();
    
    /**
     * Called when the scene is hidden.
     */
    public abstract void hide();
    
    /**
     * Called when the scene is being destroyed.
     */
    public abstract void dispose();
    
    /**
     * Called when the window is resized.
     * @param width New width
     * @param height New height
     */
    public void resize(int width, int height) {
        this.width = width;
        this.height = height;
    }
    
    // Getters
    public SceneManager getSceneManager() {
        return sceneManager;
    }
    
    public int getWidth() {
        return width;
    }
    
    public int getHeight() {
        return height;
    }
}
